package edu.univ.erp.service;

import java.util.List;
import java.util.logging.Logger;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.AuthDBManager;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.DayOfWeek;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Semester;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.UserRole;

public class AdminService {
    private static final Logger logger = Logger.getLogger(AdminService.class.getName());
    private final AccessControl accessControl;
    private final SessionManager sessionManager;
    private final AuthDBManager authDBManager;
    private final CourseDAO courseDAO;
    private final SectionDAO sectionDAO;
    private final StudentDAO studentDAO;
    private final InstructorDAO instructorDAO;
    private final SettingsDAO settingsDAO;
    
    public AdminService() {
        this.sessionManager = SessionManager.getInstance();
        this.accessControl = new AccessControl(sessionManager, null);
        this.authDBManager = new AuthDBManager();
        this.courseDAO = new CourseDAO();
        this.sectionDAO = new SectionDAO();
        this.studentDAO = new StudentDAO();
        this.instructorDAO = new InstructorDAO();
        this.settingsDAO = new SettingsDAO();
    }
    
    // User Management
    public boolean createUser(String username, String password, UserRole role) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("User creation denied: Maintenance mode");
            return false;
        }
        
        if (username == null || username.trim().isEmpty()) {
            logger.warning("Invalid username");
            return false;
        }
        
        if (!PasswordHasher.isPasswordStrong(password)) {
            logger.warning("Password does not meet strength requirements");
            return false;
        }
        
        String passwordHash = PasswordHasher.hashPassword(password);
        return authDBManager.createUser(username, passwordHash, role);
    }
    
    public boolean createStudentProfile(int userId, String rollNo, String program, int year) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("Student profile creation denied: Maintenance mode");
            return false;
        }
        
        // Check if roll number already exists using StudentDAO
        Student existingStudent = studentDAO.getStudentByRollNo(rollNo);
        if (existingStudent != null) {
            logger.warning("Roll number already exists: " + rollNo);
            return false;
        }
        
        // Use StudentDAO to create student profile
        return studentDAO.createStudentProfile(userId, rollNo, program, year);
    }
    
    public boolean createInstructorProfile(int userId, String department) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("Instructor profile creation denied: Maintenance mode");
            return false;
        }
        
        // Use InstructorDAO to create instructor profile
        return instructorDAO.createInstructorProfile(userId, department);
    }
    
    // Course Management
    public boolean createCourse(String code, String title, int credits) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("Course creation denied: Maintenance mode");
            return false;
        }
        
        if (credits <= 0) {
            logger.warning("Invalid credits: " + credits);
            return false;
        }
        
        // Check if course code already exists using CourseDAO
        Course existingCourse = courseDAO.getCourseByCode(code);
        if (existingCourse != null) {
            logger.warning("Course code already exists: " + code);
            return false;
        }
        
        // Use CourseDAO to create course
        return courseDAO.createCourse(code, title, credits);
    }
    
    // Section Management
    public boolean createSection(String courseCode, Integer instructorId, DayOfWeek day, 
                                String time, String room, int capacity, Semester semester, int year) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("Section creation denied: Maintenance mode");
            return false;
        }
        
        if (capacity <= 0) {
            logger.warning("Invalid capacity: " + capacity);
            return false;
        }
        
        // Verify course exists using CourseDAO
        Course course = courseDAO.getCourseByCode(courseCode);
        if (course == null) {
            logger.warning("Course not found: " + courseCode);
            return false;
        }
        
        // Verify instructor exists if provided using InstructorDAO
        if (instructorId != null) {
            Instructor instructor = instructorDAO.getInstructorByUserId(instructorId);
            if (instructor == null) {
                logger.warning("Instructor not found: " + instructorId);
                return false;
            }
        }
        
        // Use SectionDAO to create section
        return sectionDAO.createSection(courseCode, instructorId, day, time, room, capacity, semester, year);
    }
    
    public boolean assignInstructorToSection(int sectionId, int instructorId) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("Instructor assignment denied: Maintenance mode");
            return false;
        }
        
        // Verify section exists using SectionDAO
        Section section = sectionDAO.getSectionById(sectionId);
        if (section == null) {
            logger.warning("Section not found: " + sectionId);
            return false;
        }
        
        // Verify instructor exists using InstructorDAO
        Instructor instructor = instructorDAO.getInstructorByUserId(instructorId);
        if (instructor == null) {
            logger.warning("Instructor not found: " + instructorId);
            return false;
        }
        
        // Use SectionDAO to assign instructor
        return sectionDAO.assignInstructor(sectionId, instructorId);
    }
    
    // System Settings
    public boolean setMaintenanceMode(boolean enabled) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        return settingsDAO.setMaintenanceMode(enabled);
    }
    
    public boolean isMaintenanceMode() {
        return settingsDAO.isMaintenanceModeEnabled();
    }
    
    // User Management - Additional methods
    public List<User> getAllUsers() {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return List.of();
        }
        
        return authDBManager.getAllUsers();
    }
    
    public boolean lockUser(int userId) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("User lock denied: Maintenance mode");
            return false;
        }
        
        return authDBManager.lockUser(userId);
    }
    
    public boolean unlockUser(int userId) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("User unlock denied: Maintenance mode");
            return false;
        }
        
        return authDBManager.unlockUser(userId);
    }
    
    public boolean resetUserPassword(int userId, String newPassword) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: User cannot perform admin actions");
            return false;
        }
        
        if (isMaintenanceMode()) {
            logger.warning("Password reset denied: Maintenance mode");
            return false;
        }
        
        if (!edu.univ.erp.auth.PasswordHasher.isPasswordStrong(newPassword)) {
            logger.warning("New password does not meet strength requirements");
            return false;
        }
        
        String passwordHash = edu.univ.erp.auth.PasswordHasher.hashPassword(newPassword);
        return authDBManager.updatePassword(userId, passwordHash);
    }
    
    // Helper methods
    public User getRecentUserByUsername(String username) {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            return null;
        }
        return authDBManager.getUserByUsername(username);
    }
    
    public List<Course> getAllCourses() {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            return List.of();
        }
        return courseDAO.getAllCourses();
    }
    
    public List<Instructor> getAllInstructors() {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            return List.of();
        }
        return instructorDAO.getAllInstructors();
    }
    
    public List<Section> getAllSections() {
        if (!accessControl.canPerformAction(UserRole.ADMIN)) {
            return List.of();
        }
        return sectionDAO.getAllSections();
    }
    public boolean studentProfileExists(int userId) {
        return studentDAO.studentProfileExists(userId);
    }

    public boolean instructorProfileExists(int userId) {
        return instructorDAO.instructorProfileExists(userId);
    }

    public User getUserById(int userId) {
        // You might need to add this method to AuthDBManager first
        return authDBManager.getAllUsers().stream()
            .filter(u -> u.getUserId() == userId)
            .findFirst()
            .orElse(null);
    }
    
}