package edu.univ.erp.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import edu.univ.erp.data.AuthDBManager;
import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;

public class AuthManager {
    private static final Logger logger = Logger.getLogger(AuthManager.class.getName());
    
    private final AuthDBManager authDBManager;
    private final StudentDAO studentDAO;
    private final InstructorDAO instructorDAO;
    private final SessionManager sessionManager;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    
    
    public AuthManager() {
        this.authDBManager = new AuthDBManager();
        this.studentDAO = new StudentDAO();
        this.instructorDAO = new InstructorDAO();
        this.sessionManager = SessionManager.getInstance();
    }
    
    
    public boolean authenticate(String username, String plainPassword) {
        if (username == null || plainPassword == null) {
            return false;
        }
        
        User user = authDBManager.getUserByUsername(username);
        if (user == null) {
            logger.warning("Authentication failed: User not found - " + username);
            return false;
        }
        
        if (user.isLocked()) {
            logger.warning("Authentication failed: User account locked - " + username);
            return false;
        }
        
        if (!user.isActive()) {
            logger.warning("Authentication failed: User account inactive - " + username);
            return false;
        }
        
        String storedHash = getPasswordHash(username);
        if (storedHash == null) {
            return false;
        }
        
        if (PasswordHasher.verifyPassword(plainPassword, storedHash)) {
            sessionManager.login(user);
            logger.info("User authenticated successfully: " + username);
            
            authDBManager.resetFailedAttempts(user.getUserId());
            authDBManager.updateLastLogin(user.getUserId());
            
            return true;
        } else {
            authDBManager.incrementFailedAttempts(user.getUserId());
            
            if (user.getFailedLoginAttempts() + 1 >= MAX_LOGIN_ATTEMPTS) {
                authDBManager.lockUser(user.getUserId());
                logger.warning("User account locked due to too many failed attempts: " + username);
            }
            return false;
        }
    }
    
    
    
    
    
    
    
   
    public String getPasswordHash(String username) {
        String sql = "SELECT password_hash FROM university_auth.users_auth WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            logger.severe("Error getting password hash: " + e.getMessage());
        }
        return null;
    }
    
    
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (!sessionManager.isLoggedIn() || sessionManager.getCurrentUserId() != userId) {
            return false;
        }
        
        User user = authDBManager.getUserByUsername(sessionManager.getCurrentUser().getUsername());
        if (user == null) {
            return false;
        }
        
        String currentHash = getPasswordHash(user.getUsername());
        if (!PasswordHasher.verifyPassword(currentPassword, currentHash)) {
            return false;
        }
        
        if (!PasswordHasher.isPasswordStrong(newPassword)) {
            return false;
        }
        
        String newPasswordHash = PasswordHasher.hashPassword(newPassword);
        return authDBManager.updatePassword(userId, newPasswordHash);
    }
    
    
    public void logout() {
        if (sessionManager.isLoggedIn()) {
            logger.info("User logged out: " + sessionManager.getCurrentUser().getUsername());
            sessionManager.logout();
        }
    }
    
    
    
    public Student getCurrentStudentProfile() {
        if (!sessionManager.isLoggedIn()) {
            return null;
        }
        
        int userId = sessionManager.getCurrentUserId();
        return studentDAO.getStudentByUserId(userId);
    }
    
    public Instructor getCurrentInstructorProfile() {
        if (!sessionManager.isLoggedIn()) {
            return null;
        }
        
        int userId = sessionManager.getCurrentUserId();
        return instructorDAO.getInstructorByUserId(userId);
    }
    
    
    public boolean isUserLocked(String username) {
        User user = authDBManager.getUserByUsername(username);
        return user != null && user.isLocked();
    }
    
    public int getRemainingLoginAttempts(String username) {
        User user = authDBManager.getUserByUsername(username);
        if (user == null) {
            return 0;
        }
        return Math.max(0, MAX_LOGIN_ATTEMPTS - user.getFailedLoginAttempts());
    }
    
    
    public boolean unlockUser(int userId) {
        String sql = "UPDATE university_auth.users_auth SET status = 'ACTIVE', failed_login_attempts = 0 WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error unlocking user: " + e.getMessage());
            return false;
        }
    }
    
    
    
}