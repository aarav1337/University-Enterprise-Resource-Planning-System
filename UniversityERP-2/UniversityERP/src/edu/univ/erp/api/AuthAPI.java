package edu.univ.erp.api;

import edu.univ.erp.auth.AuthManager;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserRole;
import java.util.logging.Logger;

public class AuthAPI {
    private static final Logger logger = Logger.getLogger(AuthAPI.class.getName());
    private final AuthManager authManager;
    private final SessionManager sessionManager;
    
    public AuthAPI() {
        this.authManager = new AuthManager();
        this.sessionManager = SessionManager.getInstance();
    }
    
    public LoginResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new LoginResult(false, "Username cannot be empty");
        }
        
        if (password == null || password.isEmpty()) {
            return new LoginResult(false, "Password cannot be empty");
        }
        
        boolean success = authManager.authenticate(username, password);
        if (success) {
            UserRole role = sessionManager.getCurrentUserRole();
            return new LoginResult(true, "Login successful", role);
        } else {
            int remainingAttempts = authManager.getRemainingLoginAttempts(username);
            if (authManager.isUserLocked(username)) {
                return new LoginResult(false, "Account locked. Please contact administrator.");
            } else if (remainingAttempts <= 2) {
                return new LoginResult(false, 
                    String.format("Invalid credentials. %d attempts remaining.", remainingAttempts));
            } else {
                return new LoginResult(false, "Invalid username or password");
            }
        }
    }
    
    public void logout() {
        authManager.logout();
    }
    
    public boolean changePassword(String currentPassword, String newPassword) {
        if (!sessionManager.isLoggedIn()) {
            return false;
        }
        
        return authManager.changePassword(
            sessionManager.getCurrentUserId(), 
            currentPassword, 
            newPassword
        );
    }
    
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }
    
    public UserRole getCurrentUserRole() {
        return sessionManager.getCurrentUserRole();
    }
    
    public String getCurrentUsername() {
        return sessionManager.getCurrentUsername();
    }
    
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final UserRole role;
        
        public LoginResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public LoginResult(boolean success, String message, UserRole role) {
            this.success = success;
            this.message = message;
            this.role = role;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserRole getRole() { return role; }
    }
}