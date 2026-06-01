package edu.univ.erp.access;

import java.util.logging.Logger;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.UserRole;

public class AccessControl {
    private static final Logger logger = Logger.getLogger(AccessControl.class.getName());
   
    private final SessionManager sessionManager;
    private final SettingsDAO settingsDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final SectionDAO sectionDAO;
    
    public AccessControl(SessionManager sessionManager, Object dataManager) {
        this.sessionManager = sessionManager;
        this.settingsDAO = new SettingsDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.sectionDAO = new SectionDAO();
    }
    
    
    public AccessControl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.settingsDAO = new SettingsDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.sectionDAO = new SectionDAO();
    }
    
    public boolean canPerformAction(UserRole requiredRole) {
        if (!sessionManager.isLoggedIn()) {
            logger.warning("Access denied: User not logged in");
            return false;
        }
        
        UserRole userRole = sessionManager.getCurrentUserRole();
        
        if (userRole == UserRole.ADMIN) {
            return true;
        }
        
        boolean allowed = userRole == requiredRole;
        if (!allowed) {
            logger.warning("Access denied: User " + sessionManager.getCurrentUsername() + 
                          " with role " + userRole + " tried to access " + requiredRole + " functionality");
        }
        return allowed;
    }
    
    
    public boolean canModifyData() {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }
        
        if (isMaintenanceModeEnabled()) {
            logger.warning("Modification denied: Maintenance mode is enabled");
            return false;
        }
        
        return true;
    }
    
    
    
    public boolean canAccessStudentData(int studentId) {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }
        
        UserRole userRole = sessionManager.getCurrentUserRole();
        int currentUserId = sessionManager.getCurrentUserId();
        
        switch (userRole) {
            case STUDENT:
                // Students can only access their own data
                return currentUserId == studentId;
                
            case INSTRUCTOR:
                return enrollmentDAO.isStudentInInstructorSection(studentId, currentUserId);
                
            case ADMIN:
                return true;
                
            default:
                return false;
        }
    }
    
    
    public boolean canAccessSection(int sectionId) {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }
        
        UserRole userRole = sessionManager.getCurrentUserRole();
        int currentUserId = sessionManager.getCurrentUserId();
        
        switch (userRole) {
            case STUDENT:
                return enrollmentDAO.isStudentEnrolledInSection(currentUserId, sectionId);
                
            case INSTRUCTOR:
                Integer instructorId = sectionDAO.getSectionInstructorId(sectionId);
                return instructorId != null && instructorId == currentUserId;
                
            case ADMIN:
                return true;
                
            default:
                return false;
        }
    }
    
    
    public boolean canGradeStudent(int enrollmentId, int instructorId) {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }
        
        UserRole userRole = sessionManager.getCurrentUserRole();
        int currentUserId = sessionManager.getCurrentUserId();
        
        if (userRole == UserRole.ADMIN) {
            return true;
        }
        
        if (userRole == UserRole.INSTRUCTOR) {
            return currentUserId == instructorId;
        }
        
        return false;
    }
    
    
    
    private boolean isMaintenanceModeEnabled() {
        return settingsDAO.isMaintenanceModeEnabled();
    }
    
    public boolean isMaintenanceMode() {
        return isMaintenanceModeEnabled();
    }
}