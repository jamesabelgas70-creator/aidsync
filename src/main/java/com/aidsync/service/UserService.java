package com.aidsync.service;

import com.aidsync.config.DatabaseConfig;
import com.aidsync.model.User;
import at.favre.lib.crypto.bcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public void createUser(User user, String password) throws SQLException {
        if (isUsernameExists(user.getUsername())) {
            throw new SQLException("Username already exists");
        }
        
        if (isEmailExists(user.getEmail())) {
            throw new SQLException("Email already registered");
        }
        
        String sql = "INSERT INTO users (username, email, full_name, password_hash, role, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, BCrypt.withDefaults().hashToString(12, password.toCharArray()));
            stmt.setString(5, user.getRole().name());
            
            stmt.executeUpdate();
            logger.info("Created new user: {}", user.getUsername());
        }
    }
    
    private boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}