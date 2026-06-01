package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.domain.UserStatus;

public class InstructorDAO {
    private static final Logger logger = Logger.getLogger(InstructorDAO.class.getName());
    
    public Instructor getInstructorByUserId(int userId) {
        String sql = """
            SELECT i.user_id, i.department,
                   u.username, u.role, u.status, u.last_login, u.failed_login_attempts
            FROM university_erp.instructors i
            JOIN university_auth.users_auth u ON i.user_id = u.user_id
            WHERE i.user_id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                UserRole role = UserRole.valueOf(rs.getString("role"));
                UserStatus status = UserStatus.valueOf(rs.getString("status"));
                
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    role,
                    status,
                    rs.getString("last_login"),
                    rs.getInt("failed_login_attempts")
                );
            
                Instructor instructor = new Instructor(
                    rs.getInt("user_id"),
                    rs.getString("department")
                   
                );
                instructor.setUser(user);
                
                return instructor;
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting instructor by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public String getInstructorUsername(int instructorId) {
        String sql = "SELECT username FROM university_auth.users_auth WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("username");
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting instructor username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean instructorProfileExists(int userId) {
        String sql = "SELECT COUNT(*) FROM university_erp.instructors WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            logger.severe("Error checking instructor profile existence: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    

    public boolean createInstructorProfile(int userId, String department) {
        String sql = "INSERT INTO university_erp.instructors (user_id, department) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, department);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Instructor profile created successfully for user ID: " + userId + ", Department: " + department);
                return true;
            } else {
                logger.warning("Failed to create instructor profile for user ID: " + userId);
                return false;
            }
            
        } catch (SQLException e) {
            logger.severe("Error creating instructor profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    public List<Instructor> getAllInstructors() {
        List<Instructor> instructors = new ArrayList<>();
        String sql = """
            SELECT i.user_id, i.department, u.username, u.role, u.status
            FROM university_erp.instructors i
            JOIN university_auth.users_auth u ON i.user_id = u.user_id
            ORDER BY i.user_id
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                UserRole role = UserRole.valueOf(rs.getString("role"));
                UserStatus status = UserStatus.valueOf(rs.getString("status"));
                
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    role,
                    status,
                    null, // last_login
                    0     // failed_login_attempts
                );
                
                Instructor instructor = new Instructor(
                    rs.getInt("user_id"),
                    rs.getString("department")
                );
                instructor.setUser(user);
                
                instructors.add(instructor);
            }
            
            logger.info("Retrieved " + instructors.size() + " instructors from database");
            
        } catch (SQLException e) {
            logger.severe("Error getting all instructors: " + e.getMessage());
            e.printStackTrace();
        }
        
        return instructors;
    }
    
    
}