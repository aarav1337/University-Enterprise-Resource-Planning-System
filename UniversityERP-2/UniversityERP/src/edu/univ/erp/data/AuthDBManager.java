package edu.univ.erp.data;

import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.domain.UserStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AuthDBManager {
    private static final Logger logger = Logger.getLogger(AuthDBManager.class.getName());
    
    public AuthDBManager() {
    }
    
    public User authenticateUser(String username, String passwordHash) {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login, failed_login_attempts " +
                    "FROM university_auth.users_auth WHERE username = ? AND status = 'ACTIVE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (storedHash.equals(passwordHash)) {
                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        UserRole.valueOf(rs.getString("role")),
                        UserStatus.valueOf(rs.getString("status")),
                        rs.getString("last_login"),
                        rs.getInt("failed_login_attempts")
                    );
                    
                    updateLastLogin(user.getUserId());
                    resetFailedAttempts(user.getUserId());
                    
                    return user;
                } else {
                    incrementFailedAttempts(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Authentication error: " + e.getMessage());
        }
        return null;
    }
    
    public User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, role, status, last_login, failed_login_attempts " +
                    "FROM university_auth.users_auth WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    UserRole.valueOf(rs.getString("role")),
                    UserStatus.valueOf(rs.getString("status")),
                    rs.getString("last_login"),
                    rs.getInt("failed_login_attempts")
                );
            }
        } catch (SQLException e) {
            logger.severe("Error getting user by username: " + e.getMessage());
        }
        return null;
    }
    
    public boolean createUser(String username, String passwordHash, UserRole role) {
        String sql = "INSERT INTO university_auth.users_auth (username, role, password_hash) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, role.name());
            pstmt.setString(3, passwordHash);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE university_auth.users_auth SET password_hash = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status, last_login, failed_login_attempts " +
                    "FROM university_auth.users_auth ORDER BY user_id";
        
        try (Connection conn =DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    UserRole.valueOf(rs.getString("role")),
                    UserStatus.valueOf(rs.getString("status")),
                    rs.getString("last_login"),
                    rs.getInt("failed_login_attempts")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            logger.severe("Error getting all users: " + e.getMessage());
        }
        return users;
    }
    
    public void updateLastLogin(int userId) {
        String sql = "UPDATE university_auth.users_auth SET last_login = NOW() WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.warning("Error updating last login: " + e.getMessage());
        }
    }
    
    public void incrementFailedAttempts(int userId) {
        String sql = "UPDATE university_auth.users_auth SET failed_login_attempts = failed_login_attempts + 1 WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.warning("Error incrementing failed attempts: " + e.getMessage());
        }
    }
    
    public void resetFailedAttempts(int userId) {
        String sql = "UPDATE university_auth.users_auth SET failed_login_attempts = 0 WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.warning("Error resetting failed attempts: " + e.getMessage());
        }
    }
    
    public boolean lockUser(int userId) {
        String sql = "UPDATE university_auth.users_auth SET status = 'LOCKED' WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error locking user: " + e.getMessage());
            return false;
        }
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