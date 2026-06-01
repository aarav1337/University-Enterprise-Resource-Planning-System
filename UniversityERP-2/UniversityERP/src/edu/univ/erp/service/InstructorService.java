package edu.univ.erp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradeDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.UserRole;

public class InstructorService {
    private static final Logger logger = Logger.getLogger(InstructorService.class.getName());
    private final AccessControl accessControl;
    private final SessionManager sessionManager;
    
    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;
    private final InstructorDAO instructorDAO;
    
    
    public InstructorService() {
        this.sessionManager = SessionManager.getInstance();
        this.accessControl = new AccessControl(sessionManager);
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
        this.instructorDAO = new InstructorDAO();
        
    }
    
    public List<Section> getInstructorSections(int instructorId) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return List.of();
        }
        
        if (sessionManager.getCurrentUserId() != instructorId && 
            !accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: Cannot access sections for instructor ID " + instructorId);
            return List.of();
        }
        
        return sectionDAO.getSectionsByInstructor(instructorId);
    }
    
    
    
    public List<Enrollment> getEnrollmentsForSection(int sectionId) {
        if (!accessControl.canAccessSection(sectionId)) {
            logger.warning("Access denied: Cannot access section " + sectionId);
            return List.of();
        }
        
        return enrollmentDAO.getEnrollmentsBySection(sectionId);
    }
    
    
    
    
    public boolean enterGrade(int enrollmentId, GradeComponent component, double score, int instructorId) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return false;
        }
        
        if (!accessControl.canModifyData()) {
            logger.warning("Grade entry denied: Maintenance mode or no modification rights");
            return false;
        }
        
        if (!accessControl.canGradeStudent(enrollmentId, instructorId)) {
            logger.warning("Access denied: Cannot grade for enrollment " + enrollmentId);
            return false;
        }
        
        if (score < 0 || score > 100) {
            logger.warning("Invalid score: " + score);
            return false;
        }
        
        return gradeDAO.saveGrade(enrollmentId, component, score);
    }
    
    
    
    public boolean computeFinalGrade(int enrollmentId, int instructorId) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return false;
        }
        
        if (!accessControl.canModifyData()) {
            logger.warning("Grade computation denied: Maintenance mode or no modification rights");
            return false;
        }
        
        if (!accessControl.canGradeStudent(enrollmentId, instructorId)) {
            logger.warning("Access denied: Cannot compute grade for enrollment " + enrollmentId);
            return false;
        }
        
        List<Grade> grades = gradeDAO.getGradesByEnrollment(enrollmentId);
        if (grades.isEmpty()) {
            logger.warning("No grades found for enrollment: " + enrollmentId);
            return false;
        }
        
        double finalGrade = 0.0;
        for (Grade grade : grades) {
            double weight = grade.getComponent().getDefaultWeight() / 100.0;
            finalGrade += grade.getScore() * weight;
        }
        
        return gradeDAO.updateFinalGrade(enrollmentId, finalGrade);
    }
    
    
    public ClassStatistics getClassStatistics(int sectionId) {
        if (!accessControl.canAccessSection(sectionId)) {
            logger.warning("Access denied: Cannot access section " + sectionId);
            return null;
        }
        
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsBySection(sectionId);
        Section section = sectionDAO.getSectionById(sectionId);
        
        if (section == null) {
            return null;
        }
        
        double averageGrade = calculateAverageGrade(sectionId);
        int totalStudents = enrollments.size();
        int capacity = section.getCapacity();
        double enrollmentRate = (double) totalStudents / capacity * 100;
        
        return new ClassStatistics(section, totalStudents, averageGrade, enrollmentRate);
    }
      
    private double calculateAverageGrade(int sectionId) {
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsBySection(sectionId);
        double total = 0;
        int count = 0;
        
        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeDAO.getGradesByEnrollment(enrollment.getEnrollmentId());
            Double finalGrade = getFinalGrade(grades);
            if (finalGrade != null) {
                total += finalGrade;
                count++;
            }
        }
        
        return count > 0 ? total / count : 0.0;
    }
    
    private Double getFinalGrade(List<Grade> grades) {
        return grades.stream()
            .filter(grade -> grade.getFinalGrade() != null)
            .map(Grade::getFinalGrade)
            .findFirst()
            .orElse(null);
    }
    
    public Instructor getInstructorProfile(int instructorId) {
        if (sessionManager.getCurrentUserId() != instructorId && 
            !accessControl.canPerformAction(UserRole.ADMIN)) {
            logger.warning("Access denied: Cannot access instructor profile for ID " + instructorId);
            return null;
        }
        return instructorDAO.getInstructorByUserId(instructorId);
    }
    
    
    public List<Grade> getGradesByEnrollment(int enrollmentId) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return List.of();
        }
        
        if (!accessControl.canGradeStudent(enrollmentId, getCurrentInstructorId())) {
            logger.warning("Access denied: Cannot access grades for enrollment " + enrollmentId);
            return List.of();
        }
        
        return gradeDAO.getGradesByEnrollment(enrollmentId);
    }

    public Double getFinalGrade(int enrollmentId) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return null;
        }
        
        if (!accessControl.canGradeStudent(enrollmentId, getCurrentInstructorId())) {
            logger.warning("Access denied: Cannot access final grade for enrollment " + enrollmentId);
            return null;
        }
        
        return gradeDAO.getFinalGrade(enrollmentId);
    }

    public Map<GradeComponent, Double> getComponentGrades(int enrollmentId) {
        List<Grade> grades = getGradesByEnrollment(enrollmentId);
        Map<GradeComponent, Double> componentGrades = new HashMap<>();
        
        for (Grade grade : grades) {
            if (grade.getFinalGrade() == null) { 
                componentGrades.put(grade.getComponent(), grade.getScore());
            }
        }
        
        return componentGrades;
    }

    private int getCurrentInstructorId() {
        return sessionManager.getCurrentUserId();
    }
    
    
    public Double getExistingGrade(int enrollmentId, GradeComponent component) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return null;
        }
        
        if (!accessControl.canGradeStudent(enrollmentId, getCurrentInstructorId())) {
            logger.warning("Access denied: Cannot access grades for enrollment " + enrollmentId);
            return null;
        }
        
        return gradeDAO.getGradeByComponent(enrollmentId, component);
    }

    public Double getExistingFinalGrade(int enrollmentId) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return null;
        }
        
        if (!accessControl.canGradeStudent(enrollmentId, getCurrentInstructorId())) {
            logger.warning("Access denied: Cannot access final grade for enrollment " + enrollmentId);
            return null;
        }
        
        return gradeDAO.getFinalGrade(enrollmentId);
    }

    public boolean checkIfFinalGradeExists(int enrollmentId) {
        if (!accessControl.canPerformAction(UserRole.INSTRUCTOR)) {
            logger.warning("Access denied: User cannot perform instructor actions");
            return false;
        }
        
        if (!accessControl.canGradeStudent(enrollmentId, getCurrentInstructorId())) {
            logger.warning("Access denied: Cannot check final grade for enrollment " + enrollmentId);
            return false;
        }
        
        return gradeDAO.checkIfFinalGradeExists(enrollmentId);
    }
    
    
    
    public static class ClassStatistics {
        private final Section section;
        private final int totalStudents;
        private final double averageGrade;
        private final double enrollmentRate;
        
        public ClassStatistics(Section section, int totalStudents, double averageGrade, double enrollmentRate) {
            this.section = section;
            this.totalStudents = totalStudents;
            this.averageGrade = averageGrade;
            this.enrollmentRate = enrollmentRate;
        }
        
        public Section getSection() { return section; }
        public int getTotalStudents() { return totalStudents; }
        public double getAverageGrade() { return averageGrade; }
        public double getEnrollmentRate() { return enrollmentRate; }
        
        @Override
        public String toString() {
            return String.format(
                "Section: %s\nEnrolled: %d/%d (%.1f%%)\nAvailable: %d\nAverage Grade: %.2f",
                section.getCourseCode(),
                totalStudents,
                section.getCapacity(),
                enrollmentRate,
                section.getAvailableSeats(),
                averageGrade
            );
        }
    
}  

}
