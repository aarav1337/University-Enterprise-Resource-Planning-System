package edu.univ.erp.api;

import edu.univ.erp.service.CommonService;
import java.util.logging.Logger;

public class MaintenanceAPI {
    private static final Logger logger = Logger.getLogger(MaintenanceAPI.class.getName());
    private final CommonService commonService;
    
    public MaintenanceAPI() {
        this.commonService = new CommonService();
    }
    
    public ApiResult<Boolean> isMaintenanceMode() {
        try {
            boolean maintenanceMode = commonService.isMaintenanceMode();
            String message = maintenanceMode ? 
                "Maintenance mode is ENABLED - Read Only" : 
                "Maintenance mode is DISABLED - Normal Operations";
            return new ApiResult<>(true, message, maintenanceMode);
        } catch (Exception e) {
            logger.severe("Error checking maintenance mode: " + e.getMessage());
            return new ApiResult<>(false, "Error checking maintenance mode", false);
        }
    }
    
    public ApiResult<String> getSystemStatus() {
        try {
            boolean maintenanceMode = commonService.isMaintenanceMode();
            String userInfo = commonService.getCurrentUserInfo();
            
            String status = String.format(
                "System Status:\n- %s\n- %s\n- Maintenance Mode: %s",
                userInfo,
                "Database: Connected",
                maintenanceMode ? "ENABLED" : "DISABLED"
            );
            
            return new ApiResult<>(true, "System status retrieved", status);
        } catch (Exception e) {
            logger.severe("Error getting system status: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving system status");
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