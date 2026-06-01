package edu.univ.erp.domain;

public class User {
    private final int userId;
    private final String username;
    private final UserRole role;
    private UserStatus status;
    private String lastLogin;
    private int failedLoginAttempts;
    
    public User(int userId, String username, UserRole role, UserStatus status, 
                String lastLogin, int failedLoginAttempts) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
        this.lastLogin = lastLogin;
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public String getLastLogin() { return lastLogin; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    
    public void setStatus(UserStatus status) { this.status = status; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { 
        this.failedLoginAttempts = failedLoginAttempts; 
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
    
    public boolean isLocked() {
        return status == UserStatus.LOCKED;
    }
    
    @Override
    public String toString() {
        return String.format("User{userId=%d, username='%s', role=%s, status=%s}", 
                           userId, username, role, status);
    }
}