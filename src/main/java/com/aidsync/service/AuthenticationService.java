package com.aidsync.service;

import com.aidsync.config.DatabaseConfig;
import com.aidsync.model.User;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    public User authenticate(String username, String password) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get user from database
            String sql = "SELECT * FROM users WHERE username = ? AND status = 'ACTIVE'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = mapResultSetToUser(rs);
                        
                        // Check if account is locked
                        if (user.isLocked()) {
                            logger.warn("Login attempt for locked account: {}", username);
                            return null;
                        }
                        
                        // Verify password
                        if (BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash()).verified) {
                            // Password correct - reset failed attempts and update last login
                            resetFailedAttempts(conn, user.getId());
                            updateLastLogin(conn, user.getId());
                            user.setFailedLoginAttempts(0);
                            user.setLastLogin(LocalDateTime.now());
                            
                            logger.info("Successful login for user: {}", username);
                            return user;
                        } else {
                            // Password incorrect - increment failed attempts
                            incrementFailedAttempts(conn, user.getId());
                            logger.warn("Failed login attempt for user: {}", username);
                            return null;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Database error during authentication", e);
            throw new RuntimeException("Authentication failed", e);
        }
        
        logger.warn("Login attempt for non-existent user: {}", username);
        return null;
    }
    
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // First verify current password
            String selectSql = "SELECT password_hash FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String currentHash = rs.getString("password_hash");
                        
                        if (!BCrypt.verifyer().verify(currentPassword.toCharArray(), currentHash).verified) {
                            return false; // Current password is incorrect
                        }
                        
                        // Update password
                        String updateSql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, BCrypt.withDefaults().hashToString(12, newPassword.toCharArray()));
                            updateStmt.setInt(2, userId);
                            
                            int rowsUpdated = updateStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                logger.info("Password changed for user ID: {}", userId);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error changing password", e);
            throw new RuntimeException("Password change failed", e);
        }
        
        return false;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setStatus(User.UserStatus.valueOf(rs.getString("status")));
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }
        
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return user;
    }
    
    private void incrementFailedAttempts(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1, " +
                    "status = CASE WHEN failed_login_attempts >= 2 THEN 'LOCKED' ELSE status END " +
                    "WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    private void resetFailedAttempts(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users SET failed_login_attempts = 0, status = 'ACTIVE' WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    private void updateLastLogin(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}