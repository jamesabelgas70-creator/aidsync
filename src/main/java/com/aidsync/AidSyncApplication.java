package com.aidsync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aidsync.config.DatabaseConfig;
import com.aidsync.util.SessionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AidSyncApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(AidSyncApplication.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseConfig.initialize();
            
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 480, 650);
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
            
            primaryStage.setTitle("AIDSYNC - Aid Distribution Management System");
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/aidsync-icon.png")));
            } catch (Exception e) {
                logger.warn("Application icon not found, continuing without it", e);
            }
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            
            // Center the window on screen
            primaryStage.centerOnScreen();
            
            primaryStage.show();
            
            // Set primary stage in session manager
            SessionManager.getInstance().setPrimaryStage(primaryStage);
            
            logger.info("AIDSYNC Application started successfully");
            
        } catch (java.io.IOException e) {
            logger.error("Failed to load FXML resources", e);
            cleanup();
            Platform.exit();
        } catch (Exception e) {
            logger.error("Failed to start AIDSYNC Application", e);
            cleanup();
            Platform.exit();
        }
    }
    
    private void cleanup() {
        try {
            DatabaseConfig.shutdown();
        } catch (Exception e) {
            logger.error("Error during emergency cleanup", e);
        }
    }
    
    @Override
    public void stop() {
        boolean cleanupSuccessful = true;
        
        try {
            SessionManager.getInstance().cleanup();
        } catch (Exception e) {
            logger.error("Error cleaning up session manager", e);
            cleanupSuccessful = false;
        }
        
        try {
            DatabaseConfig.shutdown();
        } catch (Exception e) {
            logger.error("Error shutting down database", e);
            cleanupSuccessful = false;
        }
        
        if (cleanupSuccessful) {
            logger.info("AIDSYNC Application stopped successfully");
        } else {
            logger.warn("AIDSYNC Application stopped with cleanup errors");
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}