package edu.univ.erp.domain;

public enum GradeComponent {
    QUIZ("Quiz", 20),
    MIDTERM("Midterm", 30), 
    END_SEM("End Semester", 50);
    
    private final String displayName;
    private final int defaultWeight;
    
    GradeComponent(String displayName, int defaultWeight) {
        this.displayName = displayName;
        this.defaultWeight = defaultWeight;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getDefaultWeight() {
        return defaultWeight;
    }
}