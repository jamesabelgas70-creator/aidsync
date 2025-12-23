package com.aidsync.util;

import java.util.regex.Pattern;

/**
 * Security utility class for input validation and sanitization
 */
public class SecurityUtil {
    
    // Pattern to detect potential log injection attempts
    private static final Pattern LOG_INJECTION_PATTERN = Pattern.compile("[\\r\\n\\t]");
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\p{Cntrl}]");
    
    // Pattern for SQL injection detection (basic)
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript)"
    );
    
    /**
     * Sanitizes input for logging to prevent log injection attacks (CWE-117)
     * @param input The input string to sanitize
     * @return Sanitized string safe for logging
     */
    public static String sanitizeForLogging(String input) {
        if (input == null) {
            return "null";
        }
        
        // Remove line breaks and control characters to prevent log injection
        String sanitized = LOG_INJECTION_PATTERN.matcher(input).replaceAll("_");
        sanitized = CONTROL_CHARS_PATTERN.matcher(sanitized).replaceAll("");
        
        // Limit length to prevent log flooding
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200) + "...[truncated]";
        }
        
        return sanitized;
    }
    
    /**
     * Validates input for potential SQL injection patterns
     * @param input The input to validate
     * @return true if input appears safe, false if suspicious
     */
    public static boolean isInputSafe(String input) {
        if (input == null) {
            return true;
        }
        
        return !SQL_INJECTION_PATTERN.matcher(input).find();
    }
    
    /**
     * Sanitizes user input for display in UI
     * @param input The input to sanitize
     * @return Sanitized string safe for display
     */
    public static String sanitizeForDisplay(String input) {
        if (input == null) {
            return "";
        }
        
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "")
                   .trim();
    }
    
    /**
     * Validates username format
     * @param username The username to validate
     * @return true if valid format
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        // Username should be alphanumeric with underscores, 3-50 characters
        return username.matches("^[a-zA-Z0-9_]{3,50}$");
    }
    
    /**
     * Validates password strength
     * @param password The password to validate
     * @return true if password meets security requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Password should have at least 8 characters with mixed case and numbers
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Validates email format
     * @param email The email to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}