package edu.univ.erp.domain;

public class Enrollment {
    private final int enrollmentId;
    private final int studentId;
    private final int sectionId;
    private EnrollmentStatus status;
    private final String enrollmentDate;
    
    public Enrollment(int enrollmentId, int studentId, int sectionId, 
                     EnrollmentStatus status, String enrollmentDate) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
    }
    
    public int getEnrollmentId() { return enrollmentId; }
    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public EnrollmentStatus getStatus() { return status; }
    public String getEnrollmentDate() { return enrollmentDate; }
    
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
    
    public boolean isActive() {
        return status == EnrollmentStatus.REGISTERED;
    }
    
    @Override
    public String toString() {
        return String.format("Enrollment{enrollmentId=%d, studentId=%d, sectionId=%d, status=%s}", 
                           enrollmentId, studentId, sectionId, status);
    }
}