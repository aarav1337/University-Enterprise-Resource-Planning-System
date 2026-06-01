package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.domain.UserStatus;

import java.sql.*;
import java.util.logging.Logger;

public class StudentDAO {
    private static final Logger logger = Logger.getLogger(StudentDAO.class.getName());
    
    public Student getStudentByUserId(int userId) {
        String sql = """
            SELECT s.user_id, s.roll_no, s.program, s.year, 
                   u.username, u.role, u.status, u.last_login, u.failed_login_attempts
            FROM university_erp.students s
            JOIN university_auth.users_auth u ON s.user_id = u.user_id
            WHERE s.user_id = ?
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
                
                Student student = new Student(
                    rs.getInt("user_id"),
                    rs.getString("roll_no"),
                    rs.getString("program"),
                    rs.getInt("year")
                );
                student.setUser(user);
                
                logger.info("✅ Successfully loaded student: " + user.getUsername() + 
                           " | Roll No: " + student.getRollNo() + 
                           " | Program: " + student.getProgram());
                
                return student;
            } else {
                logger.warning("❌ No student profile found in university_erp.students for user_id: " + userId);
                debugCheckUserExists(userId);
            }
            
        } catch (SQLException e) {
            logger.severe("🚨 Database error getting student by user ID: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.severe("🚨 Error converting role/status to enum: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Debug method to check what's in the database
    private void debugCheckUserExists(int userId) {
        String sql = "SELECT user_id, username, role, status FROM university_auth.users_auth WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                logger.info("🔍 Debug - User exists in auth table: " + 
                           "user_id=" + rs.getInt("user_id") + 
                           ", username=" + rs.getString("username") + 
                           ", role=" + rs.getString("role") +
                           ", status=" + rs.getString("status"));
            } else {
                logger.warning("🔍 Debug - User NOT found in auth table for user_id: " + userId);
            }
            
        } catch (SQLException e) {
            logger.severe("Debug check failed: " + e.getMessage());
        }
    }
    
    public Student getStudentByRollNo(String rollNo) {
        String sql = """
            SELECT s.user_id, s.roll_no, s.program, s.year, 
                   u.username, u.role, u.status, u.last_login, u.failed_login_attempts
            FROM university_erp.students s
            JOIN university_auth.users_auth u ON s.user_id = u.user_id
            WHERE s.roll_no = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rollNo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Convert database strings to enums
                UserRole role = UserRole.valueOf(rs.getString("role"));
                UserStatus status = UserStatus.valueOf(rs.getString("status"));
                
                // Create User object using the correct constructor
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    role,
                    status,
                    rs.getString("last_login"),
                    rs.getInt("failed_login_attempts")
                );
                
                // Create Student object
                Student student = new Student(
                    rs.getInt("user_id"),
                    rs.getString("roll_no"),
                    rs.getString("program"),
                    rs.getInt("year")
                );
                student.setUser(user);
                
                return student;
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting student by roll number: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.severe("Error converting role/status to enum: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    
    public boolean studentProfileExists(int userId) {
        String sql = "SELECT COUNT(*) FROM university_erp.students WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            logger.severe("Error checking student profile existence: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean createStudentProfile(int userId, String rollNo, String program, int year) {
    	
    	System.out.println(userId+" "+rollNo+" "+program+" "+year);
        String sql = "INSERT INTO university_erp.students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, rollNo);
            pstmt.setString(3, program);
            pstmt.setInt(4, year);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Student profile created successfully for user ID: " + userId + ", Roll No: " + rollNo);
                return true;
            } else {
                logger.warning("Failed to create student profile for user ID: " + userId);
                return false;
            }
            
        } catch (SQLException e) {
            logger.severe("Error creating student profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}