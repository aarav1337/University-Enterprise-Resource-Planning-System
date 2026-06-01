package edu.univ.erp.data;

import edu.univ.erp.domain.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CourseDAO {
    private static final Logger logger = Logger.getLogger(CourseDAO.class.getName());
    
    public Course getCourseByCode(String courseCode) {
    	 String sql = "SELECT code, title, credits FROM university_erp.courses WHERE code = ?";
         
           
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, courseCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Course(
                    rs.getString("code"),
                    rs.getString("title"), 
                    rs.getInt("credits")
                );
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting course by code: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT code, title, credits FROM university_erp.courses ORDER BY code";
             
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Course course = new Course(
                    rs.getString("code"),
                    rs.getString("title"),
                    rs.getInt("credits")
                );
                courses.add(course);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting all courses: " + e.getMessage());
            e.printStackTrace();
        }
        
        return courses;
    }

	
    public boolean createCourse(String code, String title, int credits) {
        String sql = "INSERT INTO university_erp.courses (code, title, credits) VALUES (?, ?, ?)";
        if (code == null || code.trim().isEmpty()) {
            logger.warning("Course code cannot be null or empty");
            return false;
        }
        
        if (title == null || title.trim().isEmpty()) {
            logger.warning("Course title cannot be null or empty");
            return false;
        }
        
        if (credits <= 0) {
            logger.warning("Course credits must be greater than 0");
            return false;
        }
        
        if (getCourseByCode(code) != null) {
            logger.warning("Course with code " + code + " already exists");
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code.toUpperCase()); 
            pstmt.setString(2, title);
            pstmt.setInt(3, credits);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Course created successfully: " + code + " - " + title + " (" + credits + " credits)");
                return true;
            } else {
                logger.warning("Failed to create course: " + code);
                return false;
            }
            
        } catch (SQLException e) {
            logger.severe("Error creating course: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
       
    
    
}