package edu.univ.erp.domain;

public enum UserRole {
    STUDENT("Student"),
    INSTRUCTOR("Instructor"), 
    ADMIN("Admin");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static UserRole fromString(String role) {
        for (UserRole r : UserRole.values()) {
            if (r.name().equalsIgnoreCase(role) || r.displayName.equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}