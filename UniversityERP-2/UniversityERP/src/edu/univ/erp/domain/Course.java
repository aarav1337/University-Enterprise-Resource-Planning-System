package edu.univ.erp.domain;

public class Course {
    private final String code;
    private final String title;
    private final int credits;
    
    public Course(String code, String title, int credits) {
        this.code = code;
        this.title = title;
        this.credits = credits;
    }
    
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }
    
    @Override
    public String toString() {
        return String.format("Course{code='%s', title='%s', credits=%d}", code, title, credits);
    }
}