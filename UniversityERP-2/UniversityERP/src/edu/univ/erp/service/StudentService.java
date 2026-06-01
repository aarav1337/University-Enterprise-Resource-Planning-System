package edu.univ.erp.service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.UserRole;

public class StudentService {
    private static final Logger logger = Logger.getLogger(StudentService.class.getName());
    private final AccessControl accessControl;
    private final SessionManager sessionManager;
    private SectionDAO sectionDAO;
    private EnrollmentDAO enrollmentDAO;
    private StudentDAO studentDAO;
    
    public StudentService() {
        this.sessionManager = SessionManager.getInstance();
        this.accessControl = new AccessControl(sessionManager);
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.studentDAO = new StudentDAO();
    }
    
    public List<Section> getAvailableSections() {
        if (!accessControl.canPerformAction(UserRole.STUDENT)) {
            logger.warning("Access denied: User cannot perform student actions");
            return List.of();
        }
        
        return sectionDAO.getAllSections().stream()
            .filter(Section::hasAvailableSeats)
            .collect(Collectors.toList());
    }
    
    public List<Enrollment> getStudentEnrollments(int studentId) {
        if (!accessControl.canAccessStudentData(studentId)) {
            logger.warning("Access denied: Cannot access student data for student ID " + studentId);
            return List.of();
        }
        
        return enrollmentDAO.getEnrollmentsByStudentId(studentId).stream()
            .filter(Enrollment::isActive)
            .collect(Collectors.toList());
    }
    
    public boolean registerForSection(int studentId, int sectionId) {
        if (!accessControl.canPerformAction(UserRole.STUDENT)) {
            logger.warning("Access denied: User cannot perform student actions");
            return false;
        }
        
        if (!accessControl.canModifyData()) {
            logger.warning("Registration denied: Maintenance mode or no modification rights");
            return false;
        }
        
        if (!accessControl.canAccessStudentData(studentId)) {
            logger.warning("Access denied: Cannot access student data for student ID " + studentId);
            return false;
        }
        
        Section section = sectionDAO.getAllSections().stream()
            .filter(s -> s.getSectionId() == sectionId)
            .findFirst()
            .orElse(null);
            
        if (section == null) {
            logger.warning("Section not found: " + sectionId);
            return false;
        }
        
        if (!section.hasAvailableSeats()) {
            logger.warning("Section full: " + sectionId);
            return false;
        }
        
        List<Enrollment> currentEnrollments = getStudentEnrollments(studentId);
        boolean alreadyEnrolled = currentEnrollments.stream()
            .anyMatch(e -> e.getSectionId() == sectionId);
            
        if (alreadyEnrolled) {
        	String message="Student already enrolled in section: " + sectionId;
            logger.warning(message);
            throw new RuntimeException(message);
        }
        
        return sectionDAO.enrollStudentInSection(studentId, sectionId);
    }
    
    public boolean dropSection(int userId, int sectionId) {
        if (!accessControl.canModifyData()) {
            logger.warning("Drop denied: Maintenance mode or no modification rights");
            return false;
        }
        
        if (!accessControl.canAccessStudentData(userId)) {
            logger.warning("Access denied: Cannot drop section");
            return false;
        }
        
        return enrollmentDAO.dropEnrollment(userId, sectionId);
    }
    
    public List<Grade> getStudentGrades(int studentId) {
        if (!accessControl.canAccessStudentData(studentId)) {
            logger.warning("Access denied: Cannot access grades for student ID " + studentId);
            return List.of();
        }
        
        List<Enrollment> enrollments = getStudentEnrollments(studentId);
        return enrollments.stream()
            .flatMap(enrollment -> enrollmentDAO.getGradesByEnrollment(enrollment.getEnrollmentId()).stream())
            .collect(Collectors.toList());
    }
    
    
    public Student getStudentProfile(int studentId) {
        if (!accessControl.canAccessStudentData(studentId)) {
            logger.warning("Access denied: Cannot access student profile for ID " + studentId);
            return null;
        }
        return studentDAO.getStudentByUserId(studentId);
    }
    
    private Enrollment getEnrollmentById(int enrollmentId) {
        int currentUserId = sessionManager.getCurrentUserId();
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudentId(currentUserId);
        return enrollments.stream()
            .filter(e -> e.getEnrollmentId() == enrollmentId)
            .findFirst()
            .orElse(null);
    }

	public Section getSectionById(int sectionId) {
		
		return sectionDAO.getSectionById(sectionId);
	}
}