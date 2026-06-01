package edu.univ.erp.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.CSVExporter;
import edu.univ.erp.util.PDFExporter;

public class StudentAPI {
    private static final Logger logger = Logger.getLogger(StudentAPI.class.getName());
    private final StudentService studentService;
    
    public StudentAPI() {
        this.studentService = new StudentService();
    }
    
    
    public List<Section> getStudentRegistrations(int studentId) {
        try {
            StudentService studentService = new StudentService();
            List<Enrollment> enrollments = studentService.getStudentEnrollments(studentId);
            List<Section> sections = new ArrayList<>();
            
            for (Enrollment enrollment : enrollments) {
                Section section = studentService.getSectionById(enrollment.getSectionId());
                if (section != null) {
                    sections.add(section);
                }
            }
            return sections;
        } catch (Exception e) {
            logger.severe("Error getting student registrations: " + e.getMessage());
            throw new RuntimeException("Failed to load registrations: " + e.getMessage());
        }
    }
    
    
    public ApiResult<List<Section>> getAvailableSections() {
        try {
            List<Section> sections = studentService.getAvailableSections();
            return new ApiResult<>(true, "Sections retrieved successfully", sections);
        } catch (Exception e) {
            logger.severe("Error getting available sections: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving sections: " + e.getMessage());
        }
    }
    
    public ApiResult<List<Enrollment>> getMyEnrollments(int studentId) {
        try {
            List<Enrollment> enrollments = studentService.getStudentEnrollments(studentId);
            return new ApiResult<>(true, "Enrollments retrieved successfully", enrollments);
        } catch (Exception e) {
            logger.severe("Error getting enrollments: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving enrollments: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> registerForSection(int studentId, int sectionId) {
        try {
            boolean success = studentService.registerForSection(studentId, sectionId);
            if (success) {
                return new ApiResult<>(true, "Successfully registered for section");
            } else {
                return new ApiResult<>(false, "Failed to register for section. It may be full or you may already be enrolled.");
            }
        } catch (Exception e) {
            logger.severe("Error registering for section: " + e.getMessage());
            return new ApiResult<>(false, "Error during registration: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> dropSection(int userId, int sectionId) {
        try {
            boolean success = studentService.dropSection(userId, sectionId);
            if (success) {
                return new ApiResult<>(true, "Successfully dropped section");
            } else {
                return new ApiResult<>(false, "Failed to drop section");
            }
        } catch (Exception e) {
            logger.severe("Error dropping section: " + e.getMessage());
            return new ApiResult<>(false, "Error during drop: " + e.getMessage());
        }
    }
    
    
    public ApiResult<Boolean> dropSectionWithResult(int enrollmentId, int sectionId) {
        try {
            boolean success = studentService.dropSection(enrollmentId, sectionId);
            if (success) {
                return new ApiResult<>(true, "Successfully dropped section");
            } else {
                return new ApiResult<>(false, "Failed to drop section");
            }
        } catch (Exception e) {
            logger.severe("Error dropping section: " + e.getMessage());
            return new ApiResult<>(false, "Error during drop: " + e.getMessage());
        }
    }
    
    public ApiResult<List<Grade>> getMyGrades(int studentId) {
        try {
            List<Grade> grades = studentService.getStudentGrades(studentId);
            return new ApiResult<>(true, "Grades retrieved successfully", grades);
        } catch (Exception e) {
            logger.severe("Error getting grades: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving grades: " + e.getMessage());
        }
    }
    
    public ApiResult<Boolean> exportTranscript(int studentId, String filePath, String format) {
        try {
            List<Enrollment> enrollments = studentService.getStudentEnrollments(studentId);
            List<Grade> grades = studentService.getStudentGrades(studentId);
            Student student = studentService.getStudentProfile(studentId);
            
            boolean success = false;
            if ("PDF".equalsIgnoreCase(format)) {
                success = PDFExporter.exportTranscript(student, enrollments, grades, filePath);
            } else {
                success = CSVExporter.exportTranscript(enrollments, grades, filePath);
            }
            
            if (success) {
                return new ApiResult<>(true, "Transcript exported successfully to: " + filePath);
            } else {
                return new ApiResult<>(false, "Failed to export transcript");
            }
        } catch (Exception e) {
            logger.severe("Error exporting transcript: " + e.getMessage());
            return new ApiResult<>(false, "Error exporting transcript: " + e.getMessage());
        }
    }
    
    public ApiResult<Student> getMyProfile(int studentId) {
        try {
            Student student = studentService.getStudentProfile(studentId);
            if (student != null) {
                return new ApiResult<>(true, "Profile retrieved successfully", student);
            } else {
                return new ApiResult<>(false, "Profile not found");
            }
        } catch (Exception e) {
            logger.severe("Error getting profile: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving profile: " + e.getMessage());
        }
        
    }
    
    
    public ApiResult<String> getStudentName(int studentId) {
        try {
            Student student = studentService.getStudentProfile(studentId);
            if (student != null && student.getUser() != null) {
                return new ApiResult<>(true, "Student name retrieved", student.getUser().getUsername());
            } else {
                return new ApiResult<>(false, "Error retrieving student name", "Unknown Student");
            }
        } catch (Exception e) {
            logger.severe("Error getting student name: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving student name", "Unknown Student");
        }
    }
    
    public ApiResult<String> getStudentRollNo(int studentId) {
        try {
            Student student = studentService.getStudentProfile(studentId);
            if (student != null) {
                return new ApiResult<>(true, "Roll number retrieved", student.getRollNo());
            } else {
                return new ApiResult<>(false, "Student profile not found", "N/A");
            }
        } catch (Exception e) {
            logger.severe("Error getting student roll number: " + e.getMessage());
            return new ApiResult<>(false, "Error retrieving roll number", "N/A");
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