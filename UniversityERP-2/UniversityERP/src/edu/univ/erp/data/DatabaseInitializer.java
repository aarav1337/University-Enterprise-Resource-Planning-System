package edu.univ.erp.data;

import java.sql.*;
import java.util.Map;
import java.util.logging.Logger;

import edu.univ.erp.auth.PasswordHasher;

public class DatabaseInitializer {
    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());
    
    private static final String CREATE_AUTH_DB_SQL = "CREATE DATABASE IF NOT EXISTS university_auth";
    private static final String CREATE_ERP_DB_SQL = "CREATE DATABASE IF NOT EXISTS university_erp";
    
    private static final String CREATE_USERS_AUTH_TABLE = """
        CREATE TABLE IF NOT EXISTS university_auth.users_auth (
            user_id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            role ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
            password_hash VARCHAR(255) NOT NULL,
            status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
            last_login DATETIME,
            failed_login_attempts INT DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;
    
    private static final String CREATE_STUDENTS_TABLE = """
        CREATE TABLE IF NOT EXISTS university_erp.students (
            user_id INT PRIMARY KEY,
            roll_no VARCHAR(20) UNIQUE NOT NULL,
            program VARCHAR(100) NOT NULL,
            year INT NOT NULL,
            FOREIGN KEY (user_id) REFERENCES university_auth.users_auth(user_id)
        )
        """;
    
    private static final String CREATE_INSTRUCTORS_TABLE = """
        CREATE TABLE IF NOT EXISTS university_erp.instructors (
            user_id INT PRIMARY KEY,
            department VARCHAR(100) NOT NULL,
            FOREIGN KEY (user_id) REFERENCES university_auth.users_auth(user_id)
        )
        """;
    
    private static final String CREATE_COURSES_TABLE = """
        CREATE TABLE IF NOT EXISTS university_erp.courses (
            code VARCHAR(20) PRIMARY KEY,
            title VARCHAR(200) NOT NULL,
            credits INT NOT NULL CHECK (credits > 0)
        )
        """;
    
    private static final String CREATE_SECTIONS_TABLE = """
        CREATE TABLE IF NOT EXISTS university_erp.sections (
            section_id INT AUTO_INCREMENT PRIMARY KEY,
            course_code VARCHAR(20) NOT NULL,
            instructor_id INT,
            day ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
            time VARCHAR(20) NOT NULL,
            room VARCHAR(50) NOT NULL,
            capacity INT NOT NULL CHECK (capacity > 0),
            semester ENUM('FALL', 'SPRING', 'SUMMER') NOT NULL,
            year INT NOT NULL,
            enrolled_count INT DEFAULT 0,
            FOREIGN KEY (course_code) REFERENCES university_erp.courses(code),
            FOREIGN KEY (instructor_id) REFERENCES university_erp.instructors(user_id),
            CHECK (enrolled_count <= capacity)
        )
        """;
    
    private static final String CREATE_ENROLLMENTS_TABLE = """
        CREATE TABLE IF NOT EXISTS university_erp.enrollments (
            enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
            student_id INT NOT NULL,
            section_id INT NOT NULL,
            status ENUM('REGISTERED', 'DROPPED', 'COMPLETED') DEFAULT 'REGISTERED',
            enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UNIQUE KEY unique_student_section (student_id, section_id),
            FOREIGN KEY (student_id) REFERENCES university_erp.students(user_id),
            FOREIGN KEY (section_id) REFERENCES university_erp.sections(section_id)
        )
        """;
    
    private static final String CREATE_GRADES_TABLE = """
        CREATE TABLE IF NOT EXISTS university_erp.grades (
            enrollment_id INT NOT NULL,
            component ENUM('QUIZ', 'MIDTERM', 'END_SEM') NOT NULL,
            score DECIMAL(5,2) CHECK (score >= 0 AND score <= 100),
            final_grade DECIMAL(5,2) CHECK (final_grade >= 0 AND final_grade <= 100),
            PRIMARY KEY (enrollment_id, component),
            FOREIGN KEY (enrollment_id) REFERENCES university_erp.enrollments(enrollment_id)
        )
        """;
    
    private static final String CREATE_SETTINGS_TABLE = """
        CREATE TABLE IF NOT EXISTS university_erp.settings (
            setting_key VARCHAR(50) PRIMARY KEY,
            setting_value VARCHAR(255) NOT NULL
        )
        """;
    
    public static void initializeDatabases() {
        logger.info("Initializing databases...");
        
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.execute(CREATE_AUTH_DB_SQL);
            stmt.execute(CREATE_ERP_DB_SQL);
            logger.info("Databases created successfully");
            
             stmt.execute("USE university_auth");
            stmt.execute(CREATE_USERS_AUTH_TABLE);
            logger.info("Auth tables created successfully");
            
            stmt.execute("USE university_erp");
            stmt.execute(CREATE_STUDENTS_TABLE);
            stmt.execute(CREATE_INSTRUCTORS_TABLE);
            stmt.execute(CREATE_COURSES_TABLE);
            stmt.execute(CREATE_SECTIONS_TABLE);
            stmt.execute(CREATE_ENROLLMENTS_TABLE);
            stmt.execute(CREATE_GRADES_TABLE);
            stmt.execute(CREATE_SETTINGS_TABLE);
            logger.info("ERP tables created successfully");
            
            insertDefaultData(connection);
            
            logger.info("Database initialization completed successfully");
            
        } catch (SQLException e) {
            logger.severe("Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private static void insertDefaultData(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE university_erp");

            String insertMaintenanceSetting = """
                INSERT IGNORE INTO university_erp.settings (setting_key, setting_value)
                VALUES ('maintenance_mode', 'false')
                """;
            stmt.execute(insertMaintenanceSetting);

            stmt.execute("USE university_auth");

            Map<String, String> defaultUsers = Map.of(
                "admin1", "admin123",
                "inst1",  "inst123",
                "stu1",   "stu123",
                "stu2",   "stu123"
            );

            Map<String, String> roleMap = Map.of(
                "admin1", "ADMIN",
                "inst1",  "INSTRUCTOR",
                "stu1",   "STUDENT",
                "stu2",   "STUDENT"
            );

            for (Map.Entry<String, String> entry : defaultUsers.entrySet()) {
                String username = entry.getKey();
                String plainPassword = entry.getValue();
                String role = roleMap.get(username);

                String hashedPassword;
                try {
                    hashedPassword = PasswordHasher.hashPassword(plainPassword);
                } catch (IllegalArgumentException e) {
                    logger.warning("Failed to hash password for " + username + ": " + e.getMessage());
                    continue; 
                }

                String insertUserSql = """
                    INSERT IGNORE INTO university_auth.users_auth (username, role, password_hash)
                    VALUES (?, ?, ?)
                    """;

                try (PreparedStatement ps = connection.prepareStatement(insertUserSql)) {
                    ps.setString(1, username);
                    ps.setString(2, role);
                    ps.setString(3, hashedPassword);
                    ps.executeUpdate();
                }
            }

            insertUserProfiles(connection);

            logger.info("Default users inserted successfully with dynamically hashed passwords.");

        } catch (SQLException e) {
            logger.warning("Default data insertion had issues: " + e.getMessage());
            throw e;
        }
    }  
    private static void insertUserProfiles(Connection connection) throws SQLException {
        String getAdminId = "SELECT user_id FROM university_auth.users_auth WHERE username = 'admin1'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(getAdminId)) {
            
            if (rs.next()) {
                int adminId = rs.getInt("user_id");
                String insertAdminProfile = """
                    INSERT IGNORE INTO university_erp.instructors (user_id, department) 
                    VALUES (?, 'Administration')
                    """;
                try (PreparedStatement pstmt = connection.prepareStatement(insertAdminProfile)) {
                    pstmt.setInt(1, adminId);
                    pstmt.execute();
                }
            }
        }
        
        String getInstId = "SELECT user_id FROM university_auth.users_auth WHERE username = 'inst1'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(getInstId)) {
            
            if (rs.next()) {
                int instId = rs.getInt("user_id");
                String insertInstProfile = """
                    INSERT IGNORE INTO university_erp.instructors (user_id, department) 
                    VALUES (?, 'Computer Science')
                    """;
                try (PreparedStatement pstmt = connection.prepareStatement(insertInstProfile)) {
                    pstmt.setInt(1, instId);
                    pstmt.execute();
                }
            }
        }
        
        insertStudentProfile(connection, "stu1", "CS001", "Computer Science", 2);
        insertStudentProfile(connection, "stu2", "CS002", "Computer Science", 2);
        
        insertSampleCoursesAndSections(connection);
    }
    
    private static void insertStudentProfile(Connection connection, String username, 
                                           String rollNo, String program, int year) throws SQLException {
        String getUserId = "SELECT user_id FROM university_auth.users_auth WHERE username = '" + username + "'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(getUserId)) {
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String insertStudentProfile = """
                    INSERT IGNORE INTO university_erp.students (user_id, roll_no, program, year) 
                    VALUES (?, ?, ?, ?)
                    """;
                try (PreparedStatement pstmt = connection.prepareStatement(insertStudentProfile)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, rollNo);
                    pstmt.setString(3, program);
                    pstmt.setInt(4, year);
                    pstmt.execute();
                }
            }
        }
    }
    
    private static void insertSampleCoursesAndSections(Connection connection) throws SQLException {
        String[] courses = {
            "INSERT IGNORE INTO university_erp.courses VALUES ('CS101', 'Introduction to Programming', 3)",
            "INSERT IGNORE INTO university_erp.courses VALUES ('CS102', 'Data Structures', 4)",
            "INSERT IGNORE INTO university_erp.courses VALUES ('MATH101', 'Calculus I', 3)",
            "INSERT IGNORE INTO university_erp.courses VALUES ('PHY101', 'Physics I', 4)"
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String course : courses) {
                stmt.execute(course);
            }
            
            String getInstId = "SELECT user_id FROM university_auth.users_auth WHERE username = 'inst1'";
            ResultSet rs = stmt.executeQuery(getInstId);
            if (rs.next()) {
                int instId = rs.getInt("user_id");
                
                if (!sectionsExist(connection, instId)) {
                    String[] sections = {
                        String.format("INSERT INTO university_erp.sections (course_code, instructor_id, day, time, room, capacity, semester, year) VALUES ('CS101', %d, 'MONDAY', '09:00-10:30', 'Room 101', 30, 'FALL', 2024)", instId),
                        String.format("INSERT INTO university_erp.sections (course_code, instructor_id, day, time, room, capacity, semester, year) VALUES ('CS102', %d, 'WEDNESDAY', '11:00-12:30', 'Room 102', 25, 'FALL', 2024)", instId),
                        String.format("INSERT INTO university_erp.sections (course_code, instructor_id, day, time, room, capacity, semester, year) VALUES ('MATH101', %d, 'TUESDAY', '14:00-15:30', 'Room 201', 40, 'FALL', 2024)", instId)
                    };
                    
                    for (String section : sections) {
                        stmt.execute(section);
                    }
                    logger.info("Sample sections inserted successfully");
                } else {
                    logger.info("Sections already exist, skipping insertion");
                }
            }
        }
    }

    private static boolean sectionsExist(Connection connection, int instructorId) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM university_erp.sections WHERE instructor_id = ? AND semester = 'FALL' AND year = 2024";
        
        try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        }
        return false;
    }
}