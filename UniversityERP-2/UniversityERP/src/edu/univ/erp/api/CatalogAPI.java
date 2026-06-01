package edu.univ.erp.api;

import edu.univ.erp.service.CommonService;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import java.util.List;
import java.util.logging.Logger;

public class CatalogAPI {
    private static final Logger logger = Logger.getLogger(CatalogAPI.class.getName());
    private final CommonService commonService;
    
    public CatalogAPI() {
        this.commonService = new CommonService();
    }
    
    public ApiResult<List<Course>> getAllCourses() {
        try {
            List<Course> courses = commonService.getAllCourses();
            return new ApiResult<>(true, "Courses retrieved successfully", courses);
        } catch (Exception e) {
            logger.severe("Error getting courses: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving courses: " + e.getMessage());
        }
    }
    
    public ApiResult<List<Section>> getAllSections() {
        try {
            List<Section> sections = commonService.getAllSections();
            return new ApiResult<>(true, "Sections retrieved successfully", sections);
        } catch (Exception e) {
            logger.severe("Error getting sections: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving sections: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> isMaintenanceMode() {
        try {
            boolean maintenanceMode = commonService.isMaintenanceMode();
            return new ApiResult<>(true, "Maintenance mode status retrieved", maintenanceMode);
        } catch (Exception e) {
            logger.severe("Error checking maintenance mode: " + e.getMessage());
            return new ApiResult<>(false, "Error checking maintenance mode", false);
        }
    }
    
    public static class ApiResult<T> {
        private final boolean success;
        private final String message;
        private final T data;
        
        public ApiResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public ApiResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}