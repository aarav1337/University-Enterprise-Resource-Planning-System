package edu.univ.erp.service;

import java.util.List;
import java.util.logging.Logger;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;

public class CommonService {
    private static final Logger logger = Logger.getLogger(CommonService.class.getName());
    private final SessionManager sessionManager;
    private final CourseDAO courseDAO;
    private final SettingsDAO settingsDAO;
    private final SectionDAO sectionDAO;
    public CommonService() {
        this.sessionManager = SessionManager.getInstance();
        this.courseDAO = new CourseDAO();
        this.settingsDAO = new SettingsDAO();
        this.sectionDAO = new SectionDAO();
    }
    
    public List<Course> getAllCourses() {
        if (!sessionManager.isLoggedIn()) {
            logger.warning("Access denied: User not logged in");
            return List.of();
        }
        return courseDAO.getAllCourses();
    }
    
    public List<Section> getAllSections() {
        if (!sessionManager.isLoggedIn()) {
            logger.warning("Access denied: User not logged in");
            return List.of();
        }
        return sectionDAO.getAllSections();
    }
    
    public boolean isMaintenanceMode() {
        return settingsDAO.isMaintenanceModeEnabled();
    }
    
    public String getCurrentUserInfo() {
        if (!sessionManager.isLoggedIn()) {
            return "Not logged in";
        }
        return String.format("User: %s (%s)", 
            sessionManager.getCurrentUsername(),
            sessionManager.getCurrentUserRole().getDisplayName());
    }
}