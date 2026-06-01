package edu.univ.erp.api;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.domain.*;
import java.util.List;
import java.util.logging.Logger;

public class AdminAPI {
    private static final Logger logger = Logger.getLogger(AdminAPI.class.getName());
    private final AdminService adminService;
    
    public AdminAPI() {
        this.adminService = new AdminService();
    }
    
    public ApiResult<User> createUser(String username, String password, UserRole role) {
        try {
            boolean success = adminService.createUser(username, password, role);
            if (success) {
                User recentUser = adminService.getRecentUserByUsername(username);
                return new ApiResult<>(true, "User created successfully", recentUser);
            } else {
                return new ApiResult<>(false, "Failed to create user");
            }
        } catch (Exception e) {
            logger.severe("Error creating user: " + e.getMessage());
            return new ApiResult<>(false, "Error creating user: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Boolean> createStudentProfile(int userId, String rollNo, String program, int year) {
        try {
             boolean success = adminService.createStudentProfile(userId, rollNo, program, year);
            if (success) {
                return new ApiResult<>(true, "Student profile created successfully");
            } else {
                return new ApiResult<>(false, "Failed to create student profile");
            }
        } catch (Exception e) {
            logger.severe("Error creating student profile: " + e.getMessage());
            return new ApiResult<>(false, "Error creating student profile: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Boolean> createInstructorProfile(int userId, String department) {
        try {
            boolean success = adminService.createInstructorProfile(userId, department);
            if (success) {
                return new ApiResult<>(true, "Instructor profile created successfully");
            } else {
                return new ApiResult<>(false, "Failed to create instructor profile");
            }
        } catch (Exception e) {
            logger.severe("Error creating instructor profile: " + e.getMessage());
            return new ApiResult<>(false, "Error creating instructor profile: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Boolean> createCourse(String code, String title, int credits) {
        try {
            if (credits <= 0) {
                return new ApiResult<>(false, "Credits must be greater than 0");
            }
            
            boolean success = adminService.createCourse(code, title, credits);
            if (success) {
                return new ApiResult<>(true, "Course created successfully");
            } else {
                return new ApiResult<>(false, "Failed to create course");
            }
        } catch (Exception e) {
            logger.severe("Error creating course: " + e.getMessage());
            return new ApiResult<>(false, "Error creating course: " + e.getMessage());
        }
    }
    
    
	public ApiResult<Boolean> createSection(String courseCode, Integer instructorId, DayOfWeek day, String time,
			String room, int capacity, Semester semester, int year) {
		try {
			if (capacity <= 0) {
				return new ApiResult<>(false, "Capacity must be greater than 0");
			}

			boolean success = adminService.createSection(courseCode, instructorId, day, time, room, capacity, semester,
					year);
			if (success) {
				return new ApiResult<>(true, "Section created successfully");
			} else {
				return new ApiResult<>(false, "Failed to create section");
			}
		} catch (Exception e) {
			logger.severe("Error creating section: " + e.getMessage());
			return new ApiResult<>(false, "Error creating section: " + e.getMessage());
		}
	}   
	
	
	public ApiResult<Boolean> assignInstructorToSection(int sectionId, int instructorId) {
        try {
            boolean success = adminService.assignInstructorToSection(sectionId, instructorId);
            if (success) {
                return new ApiResult<>(true, "Instructor assigned successfully");
            } else {
                return new ApiResult<>(false, "Failed to assign instructor");
            }
        } catch (Exception e) {
            logger.severe("Error assigning instructor: " + e.getMessage());
            return new ApiResult<>(false, "Error assigning instructor: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> setMaintenanceMode(boolean enabled) {
        try {
            boolean success = adminService.setMaintenanceMode(enabled);
            if (success) {
                String status = enabled ? "enabled" : "disabled";
                return new ApiResult<>(true, "Maintenance mode " + status + " successfully");
            } else {
                return new ApiResult<>(false, "Failed to update maintenance mode");
            }
        } catch (Exception e) {
            logger.severe("Error setting maintenance mode: " + e.getMessage());
            return new ApiResult<>(false, "Error setting maintenance mode: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> isMaintenanceMode() {
        try {
            boolean maintenanceMode = adminService.isMaintenanceMode();
            return new ApiResult<>(true, "Maintenance mode status retrieved", maintenanceMode);
        } catch (Exception e) {
            logger.severe("Error checking maintenance mode: " + e.getMessage());
            return new ApiResult<>(false, "Error checking maintenance mode: " + e.getMessage());
        }
    }
    
    public ApiResult<List<User>> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return new ApiResult<>(true, "Users retrieved successfully", users);
        } catch (Exception e) {
            logger.severe("Error getting users: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving users: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> lockUser(int userId) {
        try {
            boolean success = adminService.lockUser(userId);
            if (success) {
                return new ApiResult<>(true, "User locked successfully");
            } else {
                return new ApiResult<>(false, "Failed to lock user");
            }
        } catch (Exception e) {
            logger.severe("Error locking user: " + e.getMessage());
            return new ApiResult<>(false, "Error locking user: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> unlockUser(int userId) {
        try {
            boolean success = adminService.unlockUser(userId);
            if (success) {
                return new ApiResult<>(true, "User unlocked successfully");
            } else {
                return new ApiResult<>(false, "Failed to unlock user");
            }
        } catch (Exception e) {
            logger.severe("Error unlocking user: " + e.getMessage());
            return new ApiResult<>(false, "Error unlocking user: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> resetUserPassword(int userId, String newPassword) {
        try {
            boolean success = adminService.resetUserPassword(userId, newPassword);
            if (success) {
                return new ApiResult<>(true, "Password reset successfully");
            } else {
                return new ApiResult<>(false, "Failed to reset password");
            }
        } catch (Exception e) {
            logger.severe("Error resetting password: " + e.getMessage());
            return new ApiResult<>(false, "Error resetting password: " + e.getMessage());
        }
    }
    
    public ApiResult<List<Course>> getAllCourses() {
        try {
            List<Course> courses = adminService.getAllCourses();
            return new ApiResult<>(true, "Courses retrieved successfully", courses);
        } catch (Exception e) {
            logger.severe("Error getting courses: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving courses: " + e.getMessage());
        }
    }
    
    public ApiResult<List<Instructor>> getAllInstructors() {
        try {
            List<Instructor> instructors = adminService.getAllInstructors();
            return new ApiResult<>(true, "Instructors retrieved successfully", instructors);
        } catch (Exception e) {
            logger.severe("Error getting instructors: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving instructors: " + e.getMessage());
        }
    }
    
    public ApiResult<List<Section>> getAllSections() {
        try {
            List<Section> sections = adminService.getAllSections();
            if (sections != null && !sections.isEmpty()) {
                return new ApiResult<>(true, "Sections retrieved successfully", sections);
            } else {
                return new ApiResult<>(true, "No sections found", sections != null ? sections : List.of());
            }
        } catch (Exception e) {
            logger.severe("Error getting all sections: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving sections: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Boolean> studentProfileExists(int userId) {
        try {
            boolean exists = adminService.studentProfileExists(userId);
            return new ApiResult<>(true, "Student profile check completed", exists);
        } catch (Exception e) {
            logger.severe("Error checking student profile: " + e.getMessage());
            return new ApiResult<>(false, "Error checking student profile: " + e.getMessage());
        }
    }

    public ApiResult<Boolean> instructorProfileExists(int userId) {
        try {
            boolean exists = adminService.instructorProfileExists(userId);
            return new ApiResult<>(true, "Instructor profile check completed", exists);
        } catch (Exception e) {
            logger.severe("Error checking instructor profile: " + e.getMessage());
            return new ApiResult<>(false, "Error checking instructor profile: " + e.getMessage());
        }
    }

    public ApiResult<User> getUserById(int userId) {
        try {
            User user = adminService.getUserById(userId);
            if (user != null) {
                return new ApiResult<>(true, "User retrieved successfully", user);
            } else {
                return new ApiResult<>(false, "User not found");
            }
        } catch (Exception e) {
            logger.severe("Error getting user: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving user: " + e.getMessage());
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