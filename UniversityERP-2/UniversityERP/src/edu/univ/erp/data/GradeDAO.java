package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GradeDAO {
    private static final Logger logger = Logger.getLogger(GradeDAO.class.getName());
    
    public List<Grade> getGradesByStudentId(int studentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = """
            SELECT g.enrollment_id, g.component, g.score, g.final_grade
            FROM university_erp.grades g
            JOIN university_erp.enrollments e ON g.enrollment_id = e.enrollment_id
            WHERE e.student_id = ? AND e.status = 'REGISTERED'
            """;
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                grades.add(new Grade(
                    rs.getInt("enrollment_id"),
                    GradeComponent.valueOf(rs.getString("component")),
                    rs.getDouble("score"),
                    rs.getObject("final_grade", Double.class)
                ));
            }
        } catch (SQLException e) {
            logger.severe("Error getting grades: " + e.getMessage());
        }
        return grades;
    }

    public boolean saveGrade(int enrollmentId, GradeComponent component, double score) {
        String sql = """
            INSERT INTO university_erp.grades (enrollment_id, component, score)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE score = VALUES(score)
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, enrollmentId);
            pstmt.setString(2, component.name());
            pstmt.setDouble(3, score);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error saving grade: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Grade> getGradesByEnrollment(int enrollmentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = """
            SELECT enrollment_id, component, score, final_grade
            FROM university_erp.grades
            WHERE enrollment_id = ?
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Grade grade = new Grade(
                    rs.getInt("enrollment_id"),
                    GradeComponent.valueOf(rs.getString("component")),
                    rs.getDouble("score"),
                    rs.getObject("final_grade", Double.class)
                );
                grades.add(grade);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting grades by enrollment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return grades;
    }
    
    public boolean updateFinalGrade(int enrollmentId, double finalGrade) {
        String sql = """
            UPDATE university_erp.grades 
            SET final_grade = ?
            WHERE enrollment_id = ? AND component = 'END_SEM'
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, finalGrade);
            pstmt.setInt(2, enrollmentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error updating final grade: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Grade> getGradesBySection(int sectionId) {
        List<Grade> grades = new ArrayList<>();
        String sql = """
            SELECT g.enrollment_id, g.component, g.score, g.final_grade
            FROM university_erp.grades g
            JOIN university_erp.enrollments e ON g.enrollment_id = e.enrollment_id
            WHERE e.section_id = ? AND e.status = 'REGISTERED'
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Grade grade = new Grade(
                    rs.getInt("enrollment_id"),
                    GradeComponent.valueOf(rs.getString("component")),
                    rs.getDouble("score"),
                    rs.getObject("final_grade", Double.class)
                );
                grades.add(grade);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting grades by section: " + e.getMessage());
            e.printStackTrace();
        }
        
        return grades;
    }

    public Double getFinalGrade(int enrollmentId) {
        String sql = """
            SELECT final_grade 
            FROM university_erp.grades 
            WHERE enrollment_id = ? AND component = 'END_SEM' AND final_grade IS NOT NULL
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("final_grade");
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting final grade: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    
 // Add these methods to your GradeDAO class
    public Double getGradeByComponent(int enrollmentId, GradeComponent component) {
        String sql = """
            SELECT score 
            FROM university_erp.grades 
            WHERE enrollment_id = ? AND component = ?
            """; // REMOVED: AND final_grade IS NULL
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, enrollmentId);
            pstmt.setString(2, component.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("score");
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting grade by component: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    public boolean checkIfFinalGradeExists(int enrollmentId) {
        String sql = """
            SELECT COUNT(*) as count 
            FROM university_erp.grades 
            WHERE enrollment_id = ? AND component = 'END_SEM' AND final_grade IS NOT NULL
            """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            logger.severe("Error checking if final grade exists: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    
    
       
    
}