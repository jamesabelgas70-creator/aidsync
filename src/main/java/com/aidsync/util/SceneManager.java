package com.aidsync.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SceneManager {
    private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);
    
    private static void loadScene(String fxmlPath, String cssPath, String title, int width, int height, boolean maximized) {
        try {
            Stage stage = SessionManager.getInstance().getPrimaryStage();
            if (stage == null) {
                logger.error("Primary stage is null, cannot switch scenes");
                AlertUtil.showError("Application Error", "Application window not available. Please restart the application.");
                return;
            }
            
            java.net.URL fxmlResource = SceneManager.class.getResource(fxmlPath);
            if (fxmlResource == null) {
                logger.error("FXML resource not found: {}", fxmlPath);
                AlertUtil.showError("Resource Error", "Screen layout file not found. Please check installation.");
                return;
            }
            
            java.net.URL cssResource = SceneManager.class.getResource(cssPath);
            if (cssResource == null) {
                logger.error("CSS resource not found: {}", cssPath);
                AlertUtil.showError("Resource Error", "Style file not found. Please check installation.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Scene scene = new Scene(loader.load(), width, height);
            scene.getStylesheets().add(cssResource.toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle(title);
            if (maximized) {
                stage.setMaximized(true);
            } else {
                stage.centerOnScreen();
            }
            
        } catch (java.io.IOException e) {
            logger.error("Failed to load FXML: {}", fxmlPath, e);
            AlertUtil.showError("Loading Error", "Failed to load the requested screen. Please try again or contact support.");
        } catch (IllegalStateException e) {
            logger.error("JavaFX state error for scene: {}", title, e);
            AlertUtil.showError("Application State Error", "Invalid application state. Please restart the application.");
        } catch (RuntimeException e) {
            logger.error("Runtime error switching to scene: {}", title, e);
            AlertUtil.showError("Runtime Error", "An unexpected runtime error occurred. Please try again.");
        }
    }
    
    public static void switchToLogin() {
        loadScene("/fxml/login.fxml", "/css/login.css", "AIDSYNC - Login", 480, 650, false);
    }
    
    public static void switchToRegister() {
        loadScene("/fxml/register.fxml", "/css/register.css", "AIDSYNC - Create Account", 520, 750, false);
    }
    
    public static void switchToDashboard() {
        loadScene("/fxml/dashboard.fxml", "/css/dashboard.css", "AIDSYNC - Dashboard", 1200, 800, true);
    }
    
    public static void switchToBeneficiaryManagement() {
        loadScene("/fxml/beneficiary-management.fxml", "/css/beneficiary.css", "AIDSYNC - Beneficiary Management", 1400, 900, true);
    }
    
    public static void switchToInventoryManagement() {
        loadScene("/fxml/inventory-management.fxml", "/css/inventory.css", "AIDSYNC - Inventory Management", 1200, 800, true);
    }
    
    public static void switchToDistributionManagement() {
        loadScene("/fxml/distribution-management.fxml", "/css/distribution.css", "AIDSYNC - Distribution Management", 1200, 800, true);
    }
}