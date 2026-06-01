package edu.univ.erp.domain;

public class Grade {
    private final int enrollmentId;
    private final GradeComponent component;
    private final double score;
    private final Double finalGrade;
    
    public Grade(int enrollmentId, GradeComponent component, double score, Double finalGrade) {
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }
    
    public int getEnrollmentId() { return enrollmentId; }
    public GradeComponent getComponent() { return component; }
    public double getScore() { return score; }
    public Double getFinalGrade() { return finalGrade; }
    
    @Override
    public String toString() {
        return String.format("Grade{enrollmentId=%d, component=%s, score=%.2f, finalGrade=%s}", 
                           enrollmentId, component, score, finalGrade);
    }
}