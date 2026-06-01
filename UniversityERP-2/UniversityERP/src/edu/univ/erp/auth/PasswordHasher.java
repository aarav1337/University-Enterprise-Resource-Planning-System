package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;

public class PasswordHasher {
    private static final Logger logger = Logger.getLogger(PasswordHasher.class.getName());
    private static final int BCRYPT_ROUNDS = 12;
    
    public static String hashPassword(String plainPassword) {
        validatePassword(plainPassword);
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid hash format during password verification: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warning("Password verification failed: " + e.getMessage());
            return false;
        }
    }
    
    private static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }
    
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = !password.matches("[A-Za-z0-9]*");
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}