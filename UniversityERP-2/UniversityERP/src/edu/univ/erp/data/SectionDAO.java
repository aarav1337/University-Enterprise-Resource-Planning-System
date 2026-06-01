package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.DayOfWeek;
import edu.univ.erp.domain.Semester;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SectionDAO {
    private static final Logger logger = Logger.getLogger(SectionDAO.class.getName());
    
    public Section getSectionById(int sectionId) {
        String sql = "SELECT * FROM university_erp.sections WHERE section_id = ?";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Section(
                    rs.getInt("section_id"),
                    rs.getString("course_code"),
                    rs.getObject("instructor_id", Integer.class),
                    DayOfWeek.valueOf(rs.getString("day")),
                    rs.getString("time"),
                    rs.getString("room"),
                    rs.getInt("capacity"),
                    Semester.valueOf(rs.getString("semester")),
                    rs.getInt("year"),
                    rs.getInt("enrolled_count")
                );
            }
        } catch (SQLException e) {
            logger.severe("Error getting section by ID: " + e.getMessage());
        }
        return null;
    }
    
    public List<Section> getAvailableSections() {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT * FROM university_erp.sections WHERE enrolled_count < capacity";
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sections.add(new Section(
                    rs.getInt("section_id"),
                    rs.getString("course_code"),
                    rs.getObject("instructor_id", Integer.class),
                    DayOfWeek.valueOf(rs.getString("day")),
                    rs.getString("time"),
                    rs.getString("room"),
                    rs.getInt("capacity"),
                    Semester.valueOf(rs.getString("semester")),
                    rs.getInt("year"),
                    rs.getInt("enrolled_count")
                ));
            }
        } catch (SQLException e) {
            logger.severe("Error getting available sections: " + e.getMessage());
        }
        return sections;
    }

    public List<Section> getSectionsByInstructor(int instructorId) {
        List<Section> sections = new ArrayList<>();
        String sql = """
            SELECT section_id, course_code, instructor_id, day, time, room, 
                   capacity, semester, year, enrolled_count
            FROM university_erp.sections 
            WHERE instructor_id = ? AND semester = 'FALL' AND year = 2024
            ORDER BY day, time
            """;
        
        System.out.println("DEBUG: Executing SQL for instructor: " + instructorId);
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("DEBUG: Query executed, processing results...");
            
            while (rs.next()) {
                Section section = new Section(
                    rs.getInt("section_id"),
                    rs.getString("course_code"),
                    rs.getInt("instructor_id"),
                    DayOfWeek.valueOf(rs.getString("day")),
                    rs.getString("time"),
                    rs.getString("room"),
                    rs.getInt("capacity"),
                    Semester.valueOf(rs.getString("semester")),
                    rs.getInt("year"),
                    rs.getInt("enrolled_count")
                );
                sections.add(section);
                System.out.println("DEBUG: Added section: " + section.getSectionId() + " - " + section.getCourseCode());
            }
            
            System.out.println("DEBUG: Total sections found: " + sections.size());
            
        } catch (SQLException e) {
            System.out.println("DEBUG: SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sections;
    }

    public boolean createSection(String courseCode, Integer instructorId, DayOfWeek day, String time, String room,
            int capacity, Semester semester, int year) {
        
        String sql = """
            INSERT INTO university_erp.sections 
            (course_code, instructor_id, day, time, room, capacity, semester, year, enrolled_count) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
            """;
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, courseCode);
                
                if (instructorId != null) {
                    pstmt.setInt(2, instructorId);
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                
                pstmt.setString(3, day.name());
                pstmt.setString(4, time);
                pstmt.setString(5, room);
                pstmt.setInt(6, capacity);
                pstmt.setString(7, semester.name());
                pstmt.setInt(8, year);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    logger.info("Section created successfully: " + courseCode + " - " + day + " " + time);
                    return true;
                } else {
                    conn.rollback();
                    logger.warning("Failed to create section: No rows affected");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.severe("Error rolling back section creation: " + ex.getMessage());
                }
            }
            logger.severe("Error creating section: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    public boolean assignInstructor(int sectionId, int instructorId) {
        String sql = "UPDATE university_erp.sections SET instructor_id = ? WHERE section_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // First verify the section exists
            String checkSectionSql = "SELECT COUNT(*) FROM university_erp.sections WHERE section_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSectionSql)) {
                checkStmt.setInt(1, sectionId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    logger.warning("Section not found: " + sectionId);
                    conn.rollback();
                    return false;
                }
            }
            
            // Verify instructor exists (optional but recommended)
            String checkInstructorSql = "SELECT COUNT(*) FROM university_erp.instructors WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkInstructorSql)) {
                checkStmt.setInt(1, instructorId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    logger.warning("Instructor not found: " + instructorId);
                    conn.rollback();
                    return false;
                }
            }
            
            // Assign instructor to section
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, instructorId);
                pstmt.setInt(2, sectionId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    logger.info("Instructor " + instructorId + " assigned to section " + sectionId);
                    return true;
                } else {
                    conn.rollback();
                    logger.warning("Failed to assign instructor: No rows affected");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.severe("Error rolling back instructor assignment: " + ex.getMessage());
                }
            }
            logger.severe("Error assigning instructor to section: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    
	public List<Section> getAllSections() {
	    List<Section> sections = new ArrayList<>();
	    String sql = """
	        SELECT s.section_id, s.course_code, s.instructor_id, s.day, s.time, s.room, 
	               s.capacity, s.semester, s.year, s.enrolled_count,
	               c.title as course_title,
	               u.username as instructor_name
	        FROM university_erp.sections s
	        JOIN university_erp.courses c ON s.course_code = c.code
	        LEFT JOIN university_auth.users_auth u ON s.instructor_id = u.user_id
	        ORDER BY s.course_code, s.section_id
	        """;
	    
	    try (Connection conn = DatabaseConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {
	        
	        while (rs.next()) {
	            Section section = new Section(
	                rs.getInt("section_id"),
	                rs.getString("course_code"),
	                rs.getObject("instructor_id", Integer.class),
	                DayOfWeek.valueOf(rs.getString("day")),
	                rs.getString("time"),
	                rs.getString("room"),
	                rs.getInt("capacity"),
	                Semester.valueOf(rs.getString("semester")),
	                rs.getInt("year"),
	                rs.getInt("enrolled_count")
	            );
	            sections.add(section);
	        }
	        
	        logger.info("Retrieved " + sections.size() + " sections from database");
	        
	    } catch (SQLException e) {
	        logger.severe("Error getting all sections: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return sections;
	}
	
	
	public Integer getSectionInstructorId(int sectionId) {
	    String sql = "SELECT instructor_id FROM university_erp.sections WHERE section_id = ?";
	    
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        
	        pstmt.setInt(1, sectionId);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getObject("instructor_id", Integer.class);
	        }
	    } catch (SQLException e) {
	        logger.severe("Error getting section instructor: " + e.getMessage());
	    }
	    return null;
	}
	
	
	public boolean enrollStudentInSection(int studentId, int sectionId) {
        String checkSql = "SELECT COUNT(*) FROM university_erp.enrollments WHERE student_id = ? AND section_id = ?";
        String enrollSql = "INSERT INTO university_erp.enrollments (student_id, section_id) VALUES (?, ?)";
        String updateCountSql = "UPDATE university_erp.sections SET enrolled_count = enrolled_count + 1 WHERE section_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Check if already enrolled
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, sectionId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Student already enrolled in this section");
                }
            }
            
            // Enroll student
            try (PreparedStatement enrollStmt = conn.prepareStatement(enrollSql)) {
                enrollStmt.setInt(1, studentId);
                enrollStmt.setInt(2, sectionId);
                enrollStmt.executeUpdate();
            }
            
            // Update enrolled count
            try (PreparedStatement updateStmt = conn.prepareStatement(updateCountSql)) {
                updateStmt.setInt(1, sectionId);
                updateStmt.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.severe("Error rolling back enrollment: " + ex.getMessage());
                }
            }
            logger.severe("Error enrolling student: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
	
}