package edu.univ.erp.domain;

public enum EnrollmentStatus {
    REGISTERED("Registered"),
    DROPPED("Dropped"),
    COMPLETED("Completed");
    
    private final String displayName;
    
    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}