package edu.univ.erp.api;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.util.CSVExporter;
import edu.univ.erp.util.PDFExporter;

public class InstructorAPI {
    private static final Logger logger = Logger.getLogger(InstructorAPI.class.getName());
    private final InstructorService instructorService;
    
    public InstructorAPI() {
        this.instructorService = new InstructorService();
    }
    
    public ApiResult<List<Section>> getMySections(int instructorId) {
        try {
            List<Section> sections = instructorService.getInstructorSections(instructorId);
            if (sections.isEmpty()) {
                return new ApiResult<>(true, "No sections found for this instructor", sections);
            }
            return new ApiResult<>(true, "Sections retrieved successfully", sections);
        } catch (Exception e) {
            logger.severe("Error getting instructor sections: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving sections: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Section> getSectionDetails(int sectionId, int instructorId) {
        try {
            List<Section> sections = instructorService.getInstructorSections(instructorId);
            Section section = sections.stream()
                .filter(s -> s.getSectionId() == sectionId)
                .findFirst()
                .orElse(null);
            
            if (section != null) {
                return new ApiResult<>(true, "Section details retrieved", section);
            } else {
                return new ApiResult<>(false, "Section not found or access denied");
            }
        } catch (Exception e) {
            logger.severe("Error getting section details: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving section details: " + e.getMessage());
        }
    }
    
    
    public ApiResult<List<Enrollment>> getSectionEnrollments(int sectionId, int instructorId) {
        try {
            List<Enrollment> enrollments = instructorService.getEnrollmentsForSection(sectionId);
            if (enrollments.isEmpty()) {
                return new ApiResult<>(true, "No enrollments found for this section", enrollments);
            }
            return new ApiResult<>(true, "Enrollments retrieved successfully", enrollments);
        } catch (Exception e) {
            logger.severe("Error getting section enrollments: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving enrollments: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> enterGrade(int enrollmentId, GradeComponent component, double score, int instructorId) {
        try {
            if (score < 0 || score > 100) {
                return new ApiResult<>(false, "Score must be between 0 and 100");
            }
            
            boolean success = instructorService.enterGrade(enrollmentId, component, score, instructorId);
            if (success) {
                return new ApiResult<>(true, String.format("%s grade entered successfully", component.getDisplayName()));
            } else {
                return new ApiResult<>(false, "Failed to enter grade - check access rights or maintenance mode");
            }
        } catch (Exception e) {
            logger.severe("Error entering grade: " + e.getMessage());
            return new ApiResult<>(false, "Error entering grade: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Boolean> enterMultipleGrades(Map<Integer, Map<GradeComponent, Double>> gradeMap, int instructorId) {
        try {
            int successCount = 0;
            int totalCount = 0;
            
            for (Map.Entry<Integer, Map<GradeComponent, Double>> entry : gradeMap.entrySet()) {
                int enrollmentId = entry.getKey();
                Map<GradeComponent, Double> grades = entry.getValue();
                
                for (Map.Entry<GradeComponent, Double> gradeEntry : grades.entrySet()) {
                    GradeComponent component = gradeEntry.getKey();
                    Double score = gradeEntry.getValue();
                    
                    if (score != null && score >= 0 && score <= 100) {
                        boolean success = instructorService.enterGrade(enrollmentId, component, score, instructorId);
                        if (success) successCount++;
                        totalCount++;
                    }
                }
            }
            
            if (successCount == totalCount) {
                return new ApiResult<>(true, String.format("All %d grades entered successfully", totalCount));
            } else {
                return new ApiResult<>(false, 
                    String.format("Entered %d out of %d grades successfully", successCount, totalCount));
            }
        } catch (Exception e) {
            logger.severe("Error entering multiple grades: " + e.getMessage());
            return new ApiResult<>(false, "Error entering grades: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> computeFinalGrade(int enrollmentId, int instructorId) {
        try {
            boolean success = instructorService.computeFinalGrade(enrollmentId, instructorId);
            if (success) {
                return new ApiResult<>(true, "Final grade computed successfully");
            } else {
                return new ApiResult<>(false, "Failed to compute final grade - check if all component grades are entered");
            }
        } catch (Exception e) {
            logger.severe("Error computing final grade: " + e.getMessage());
            return new ApiResult<>(false, "Error computing final grade: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Boolean> computeAllFinalGrades(int sectionId, int instructorId) {
        try {
            List<Enrollment> enrollments = instructorService.getEnrollmentsForSection(sectionId);
            int successCount = 0;
            
            for (Enrollment enrollment : enrollments) {
                boolean success = instructorService.computeFinalGrade(enrollment.getEnrollmentId(), instructorId);
                if (success) successCount++;
            }
            
            if (successCount == enrollments.size()) {
                return new ApiResult<>(true, 
                    String.format("Final grades computed for all %d students", enrollments.size()));
            } else {
                return new ApiResult<>(false, 
                    String.format("Final grades computed for %d out of %d students", successCount, enrollments.size()));
            }
        } catch (Exception e) {
            logger.severe("Error computing all final grades: " + e.getMessage());
            return new ApiResult<>(false, "Error computing final grades: " + e.getMessage());
        }
    }
    
    public ApiResult<InstructorService.ClassStatistics> getClassStatistics(int sectionId) {
        try {
            InstructorService.ClassStatistics stats = instructorService.getClassStatistics(sectionId);
            if (stats != null) {
                return new ApiResult<>(true, "Class statistics retrieved successfully", stats);
            } else {
                return new ApiResult<>(false, "Unable to generate statistics for this section");
            }
        } catch (Exception e) {
            logger.severe("Error getting class statistics: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving statistics: " + e.getMessage());
        }
    }
    
    public ApiResult<Map<String, Object>> getDetailedAnalytics(int sectionId) {
        try {
            // This would return more detailed analytics
            // For now, return basic stats in a map
            InstructorService.ClassStatistics stats = instructorService.getClassStatistics(sectionId);
            if (stats != null) {
                Map<String, Object> analytics = Map.of(
                    "enrollmentStats", Map.of(
                        "total", stats.getTotalStudents(),
                        "capacity", stats.getSection().getCapacity(),
                        "enrollmentRate", stats.getEnrollmentRate()
                    ),
                    "performanceStats", Map.of(
                        "averageGrade", stats.getAverageGrade(),
                        "gradeDistribution", "To be implemented" // Would calculate actual distribution
                    )
                );
                return new ApiResult<>(true, "Analytics retrieved successfully", analytics);
            } else {
                return new ApiResult<>(false, "Unable to generate analytics for this section");
            }
        } catch (Exception e) {
            logger.severe("Error getting detailed analytics: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving analytics: " + e.getMessage());
        }
    }
    
    // Export functionality
    public ApiResult<Boolean> exportGradeSheet(int sectionId, String filePath) {
        try {
            List<Enrollment> enrollments = instructorService.getEnrollmentsForSection(sectionId);
            boolean success = CSVExporter.exportGradeSheet(enrollments, filePath);
            if (success) {
                return new ApiResult<>(true, "Grade sheet exported successfully to: " + filePath);
            } else {
                return new ApiResult<>(false, "Failed to export grade sheet");
            }
        } catch (Exception e) {
            logger.severe("Error exporting grade sheet: " + e.getMessage());
            return new ApiResult<>(false, "Error exporting grade sheet: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> exportClassReport(int sectionId, String filePath) {
        try {
            InstructorService.ClassStatistics stats = instructorService.getClassStatistics(sectionId);
            boolean success = PDFExporter.exportClassReport(stats, filePath);
            if (success) {
                return new ApiResult<>(true, "Class report exported successfully to: " + filePath);
            } else {
                return new ApiResult<>(false, "Failed to export class report");
            }
        } catch (Exception e) {
            logger.severe("Error exporting class report: " + e.getMessage());
            return new ApiResult<>(false, "Error exporting class report: " + e.getMessage());
        }
    }
    
    // Profile Management
    public ApiResult<Instructor> getMyProfile(int instructorId) {
        try {
            Instructor instructor = instructorService.getInstructorProfile(instructorId);
            if (instructor != null) {
                return new ApiResult<>(true, "Profile retrieved successfully", instructor);
            } else {
                return new ApiResult<>(false, "Profile not found");
            }
        } catch (Exception e) {
            logger.severe("Error getting profile: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving profile: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> validateGradeData(Map<Integer, Map<GradeComponent, Double>> gradeMap) {
        try {
            for (Map.Entry<Integer, Map<GradeComponent, Double>> entry : gradeMap.entrySet()) {
                Map<GradeComponent, Double> grades = entry.getValue();
                for (Map.Entry<GradeComponent, Double> gradeEntry : grades.entrySet()) {
                    Double score = gradeEntry.getValue();
                    if (score != null && (score < 0 || score > 100)) {
                        return new ApiResult<>(false, 
                            String.format("Invalid score %.2f for enrollment %d. Score must be between 0-100", 
                                score, entry.getKey()));
                    }
                }
            }
            return new ApiResult<>(true, "Grade data validation passed");
        } catch (Exception e) {
            logger.severe("Error validating grade data: " + e.getMessage());
            return new ApiResult<>(false, "Error validating grade data: " + e.getMessage());
        }
    }
    
    public ApiResult<List<Grade>> getGradesByEnrollment(int enrollmentId) {
        try {
            List<Grade> grades = instructorService.getGradesByEnrollment(enrollmentId);
            return new ApiResult<>(true, "Grades retrieved successfully", grades);
        } catch (Exception e) {
            logger.severe("Error getting grades: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving grades: " + e.getMessage());
        }
    }
    
    public ApiResult<Double> getFinalGrade(int enrollmentId) {
        try {
            Double finalGrade = instructorService.getFinalGrade(enrollmentId);
            if (finalGrade != null) {
                return new ApiResult<>(true, "Final grade retrieved", finalGrade);
            } else {
                return new ApiResult<>(false, "No final grade found");
            }
        } catch (Exception e) {
            logger.severe("Error getting final grade: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving final grade: " + e.getMessage());
        }
    }
    
    public ApiResult<Map<GradeComponent, Double>> getComponentGrades(int enrollmentId) {
        try {
            Map<GradeComponent, Double> componentGrades = instructorService.getComponentGrades(enrollmentId);
            return new ApiResult<>(true, "Component grades retrieved", componentGrades);
        } catch (Exception e) {
            logger.severe("Error getting component grades: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving component grades: " + e.getMessage());
        }
    
    }
    
    
    public ApiResult<Double> getExistingGrade(int enrollmentId, GradeComponent component) {
        try {
            Double grade = instructorService.getExistingGrade(enrollmentId, component);
            if (grade != null) {
                return new ApiResult<>(true, component.getDisplayName() + " grade retrieved", grade);
            } else {
                return new ApiResult<>(false, "No " + component.getDisplayName() + " grade found");
            }
        } catch (Exception e) {
            logger.severe("Error getting grade: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving grade: " + e.getMessage());
        }
    }

    public ApiResult<Double> getExistingFinalGrade(int enrollmentId) {
        try {
            Double finalGrade = instructorService.getExistingFinalGrade(enrollmentId);
            if (finalGrade != null) {
                return new ApiResult<>(true, "Final grade retrieved", finalGrade);
            } else {
                return new ApiResult<>(false, "No final grade found");
            }
        } catch (Exception e) {
            logger.severe("Error getting final grade: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving final grade: " + e.getMessage());
        }
    }

    public ApiResult<Boolean> checkIfFinalGradeExists(int enrollmentId) {
        try {
            boolean exists = instructorService.checkIfFinalGradeExists(enrollmentId);
            String message = exists ? "Final grade exists" : "No final grade found";
            return new ApiResult<>(true, message, exists);
        } catch (Exception e) {
            logger.severe("Error checking final grade: " + e.getMessage());
            return new ApiResult<>(false, "Error checking final grade: " + e.getMessage());
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