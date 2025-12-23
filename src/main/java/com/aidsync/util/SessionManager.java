package com.aidsync.util;

import com.aidsync.model.User;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    
    private static SessionManager instance;
    private User currentUser;
    private LocalDateTime lastActivity;
    private Stage primaryStage;
    private ScheduledExecutorService sessionChecker;
    
    private SessionManager() {
        startSessionChecker();
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            try {
                instance = new SessionManager();
            } catch (Exception e) {
                logger.error("Failed to initialize SessionManager", e);
                throw new RuntimeException("SessionManager initialization failed", e);
            }
        }
        return instance;
    }
    
    public void login(User user) {
        this.currentUser = user;
        this.lastActivity = LocalDateTime.now();
        logger.info("User {} logged in", user.getUsername());
    }
    
    public void logout() {
        if (currentUser != null) {
            logger.info("User {} logged out", currentUser.getUsername());
            currentUser = null;
            lastActivity = null;
        }
    }
    
    public void updateActivity() {
        if (currentUser != null) {
            lastActivity = LocalDateTime.now();
        }
    }
    
    public boolean isLoggedIn() {
        return currentUser != null && !isSessionExpired();
    }
    
    public boolean isSessionExpired() {
        if (lastActivity == null) return true;
        return lastActivity.plusMinutes(SESSION_TIMEOUT_MINUTES).isBefore(LocalDateTime.now());
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    private void startSessionChecker() {
        sessionChecker = Executors.newSingleThreadScheduledExecutor();
        sessionChecker.scheduleAtFixedRate(() -> {
            if (isLoggedIn() && isSessionExpired()) {
                logger.info("Session expired for user {}", currentUser.getUsername());
                logout();
                // Show session expired dialog
                javafx.application.Platform.runLater(() -> {
                    AlertUtil.showWarning("Session Expired", "Your session has expired. Please log in again.");
                    SceneManager.switchToLogin();
                });
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    public void cleanup() {
        if (sessionChecker != null) {
            sessionChecker.shutdown();
        }
        logout();
    }
}