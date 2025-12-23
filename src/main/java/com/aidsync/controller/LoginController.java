package com.aidsync.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aidsync.model.User;
import com.aidsync.service.AuthenticationService;
import com.aidsync.util.AlertUtil;
import com.aidsync.util.SceneManager;
import com.aidsync.util.SecurityUtil;
import com.aidsync.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Button loginButton;

    @FXML private Label statusLabel;
    
    private AuthenticationService authService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthenticationService();
        
        // Add enter key handler for password field
        passwordField.setOnKeyPressed(this::handleKeyPressed);
        usernameField.setOnKeyPressed(this::handleKeyPressed);
        
        // Focus on username field
        usernameField.requestFocus();
        
        // Load remembered username if exists
        loadRememberedCredentials();
    }
    
    @FXML
    private void handleCreateAccount() {
        SceneManager.switchToRegister();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both username and password", true);
            return;
        }
        
        // Validate input format
        if (!SecurityUtil.isValidUsername(username)) {
            showStatus("Invalid username format", true);
            return;
        }
        
        if (!SecurityUtil.isInputSafe(username) || !SecurityUtil.isInputSafe(password)) {
            showStatus("Invalid characters detected in input", true);
            logger.warn("Suspicious login attempt with potentially malicious input from: {}", SecurityUtil.sanitizeForLogging(username));
            return;
        }
        
        try {
            loginButton.setDisable(true);
            showStatus("Authenticating...", false);
            
            User user = authService.authenticate(username, password);
            
            if (user != null) {
                if (user.isLocked()) {
                    showStatus("Account is locked due to multiple failed login attempts", true);
                    return;
                }
                
                // Save credentials if remember me is checked
                if (rememberMeCheckBox.isSelected()) {
                    saveCredentials(username);
                }
                
                // Login successful
                SessionManager.getInstance().login(user);
                logger.info("User logged in successfully: {}", SecurityUtil.sanitizeForLogging(username));
                
                // Switch to dashboard
                SceneManager.switchToDashboard();
                
            } else {
                showStatus("Invalid username or password", true);
                passwordField.clear();
                passwordField.requestFocus();
            }
            
        } catch (Exception e) {
            logger.error("Login error", e);
            showStatus("Login failed: " + e.getMessage(), true);
        } finally {
            loginButton.setDisable(false);
        }
    }
    
    @FXML
    private void handleForgotPassword() {
        AlertUtil.showInfo("Password Recovery", 
            "Please contact your system administrator to reset your password.");
    }
    
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #d32f2f;" : "-fx-text-fill: #1976d2;");
    }
    
    private void loadRememberedCredentials() {
        // Load from preferences if implemented
        // For now, just clear the status
        statusLabel.setText("");
    }
    
    private void saveCredentials(String userLogin) {
        // Save to preferences if implemented
        // For security, only save username, never password
        logger.debug("Saving credentials for user: {}", SecurityUtil.sanitizeForLogging(userLogin));
    }
}