package edu.univ.erp.domain;

public enum Semester {
    FALL("Fall"),
    SPRING("Spring"), 
    SUMMER("Summer");
    
    private final String displayName;
    
    Semester(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}