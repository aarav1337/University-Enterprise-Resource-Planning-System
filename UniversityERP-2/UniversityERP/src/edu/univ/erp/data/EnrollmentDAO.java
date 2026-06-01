package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EnrollmentDAO {
    private static final Logger logger = Logger.getLogger(EnrollmentDAO.class.getName());
    
    public List<Enrollment> getEnrollmentsByStudentId(int studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM university_erp.enrollments WHERE student_id = ? AND status = 'REGISTERED'";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                enrollments.add(new Enrollment(
                    rs.getInt("enrollment_id"),
                    rs.getInt("student_id"),
                    rs.getInt("section_id"),
                    EnrollmentStatus.valueOf(rs.getString("status")),
                    rs.getString("enrollment_date")
                ));
            }
        } catch (SQLException e) {
            logger.severe("Error getting enrollments: " + e.getMessage());
        }
        return enrollments;
    }
    
    
    public boolean registerStudentForSection(int studentId, int sectionId) {
        String checkSql = "SELECT COUNT(*) FROM university_erp.enrollments WHERE student_id = ? AND section_id = ? AND status = 'REGISTERED'";
        String insertSql = "INSERT INTO university_erp.enrollments (student_id, section_id, status) VALUES (?, ?, 'REGISTERED')";
        String updateSectionSql = "UPDATE university_erp.sections SET enrolled_count = enrolled_count + 1 WHERE section_id = ? AND enrolled_count < capacity";
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, sectionId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    connection.rollback();
                    return false; 
                }
            }
            
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSectionSql)) {
                updateStmt.setInt(1, sectionId);
                int updated = updateStmt.executeUpdate();
                if (updated == 0) {
                    connection.rollback();
                    return false; 
                }
            }
            
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setInt(1, studentId);
                insertStmt.setInt(2, sectionId);
                insertStmt.executeUpdate();
            }
            
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            logger.severe("Error registering for section: " + e.getMessage());
            return false;
        }
    }
    
    public boolean dropEnrollment(int userId, int sectionId) {
        String updateEnrollmentSql = "UPDATE university_erp.enrollments SET status = 'DROPPED' WHERE student_id = ? AND section_id = ?";
        String updateSectionSql = "UPDATE university_erp.sections SET enrolled_count = enrolled_count - 1 WHERE section_id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            
            try (PreparedStatement updateEnrollmentStmt = connection.prepareStatement(updateEnrollmentSql)) {
                updateEnrollmentStmt.setInt(1, userId);
                updateEnrollmentStmt.setInt(2, sectionId);
                int updated = updateEnrollmentStmt.executeUpdate();
                if (updated == 0) {
                    connection.rollback();
                    return false;
                }
            }
            
            try (PreparedStatement updateSectionStmt = connection.prepareStatement(updateSectionSql)) {
                updateSectionStmt.setInt(1, sectionId);
                updateSectionStmt.executeUpdate();
            }
            
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            logger.severe("Error dropping enrollment: " + e.getMessage());
            return false;
        }
    }

    public List<Enrollment> getEnrollmentsBySection(int sectionId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, e.student_id, e.section_id, e.status, e.enrollment_date,
                   s.roll_no, s.program, s.year
            FROM university_erp.enrollments e
            JOIN university_erp.students s ON e.student_id = s.user_id
            WHERE e.section_id = ? AND e.status = 'REGISTERED'
            ORDER BY s.roll_no
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Enrollment enrollment = new Enrollment(
                    rs.getInt("enrollment_id"),
                    rs.getInt("student_id"),
                    rs.getInt("section_id"),
                    EnrollmentStatus.valueOf(rs.getString("status")),
                    rs.getString("enrollment_date")
                );
                enrollments.add(enrollment);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting enrollments by section: " + e.getMessage());
            e.printStackTrace();
        }
        
        return enrollments;
    }
    
    public boolean isStudentEnrolledInSection(int studentId, int sectionId) {
        String sql = "SELECT COUNT(*) FROM university_erp.enrollments WHERE student_id = ? AND section_id = ? AND status = 'REGISTERED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.severe("Error checking student enrollment: " + e.getMessage());
        }
        return false;
    }
    
    public boolean isStudentInInstructorSection(int studentId, int instructorId) {
        String sql = """
            SELECT COUNT(*) 
            FROM university_erp.enrollments e
            JOIN university_erp.sections s ON e.section_id = s.section_id
            WHERE e.student_id = ? AND s.instructor_id = ? AND e.status = 'REGISTERED'
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, instructorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.severe("Error checking student-instructor relationship: " + e.getMessage());
        }
        return false;
    }
    
    
    public List<Grade> getGradesByEnrollment(int enrollmentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT enrollment_id, component, score, final_grade FROM university_erp.grades WHERE enrollment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
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
            logger.severe("Error getting grades: " + e.getMessage());
        }
        return grades;
    }
}