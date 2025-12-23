package com.aidsync.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private UserRole role;
    private UserStatus status;
    private int failedLoginAttempts;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum UserRole {
        SUPER_ADMIN("Super Administrator"),
        LGU_ADMIN("LGU Administrator"),
        BARANGAY_CAPTAIN("Barangay Captain"),
        DISTRIBUTION_STAFF("Distribution Staff"),
        VIEWER("Viewer");
        
        private final String displayName;
        
        UserRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED
    }
    
    // Constructors
    public User() {}
    
    public User(String username, String fullName, UserRole role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Utility methods
    public boolean isLocked() {
        return status == UserStatus.LOCKED || failedLoginAttempts >= 3;
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isLocked();
    }
    
    public boolean hasRole(UserRole... roles) {
        for (UserRole r : roles) {
            if (this.role == r) return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}