package com.aidsync.controller;

import com.aidsync.model.User;
import com.aidsync.service.UserService;
import com.aidsync.util.AlertUtil;
import com.aidsync.util.SceneManager;
import com.aidsync.util.SecurityUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<User.UserRole> roleComboBox;
    @FXML private Button registerButton;
    @FXML private Button backToLoginButton;
    
    private UserService userService;
    
    public void initialize() {
        userService = new UserService();
        setupForm();
    }
    
    private void setupForm() {
        roleComboBox.getItems().addAll(User.UserRole.DISTRIBUTION_STAFF, User.UserRole.VIEWER);
        roleComboBox.setValue(User.UserRole.VIEWER);
    }
    
    @FXML
    private void handleRegister() {
        if (!validateForm()) return;
        
        try {
            User newUser = new User();
            newUser.setUsername(usernameField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setFullName(fullNameField.getText().trim());
            newUser.setRole(roleComboBox.getValue());
            
            userService.createUser(newUser, passwordField.getText());
            
            AlertUtil.showInfo("Success", "Account created successfully! You can now log in.");
            SceneManager.switchToLogin();
            
        } catch (Exception e) {
            logger.error("Registration failed", e);
            AlertUtil.showError("Registration Failed", e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToLogin() {
        SceneManager.switchToLogin();
    }
    
    private boolean validateForm() {
        String username = usernameField.getText();
        if (username == null || username.trim().length() < 3) {
            AlertUtil.showWarning("Validation Error", "Username must be at least 3 characters.");
            return false;
        }
        
        if (!SecurityUtil.isValidUsername(username)) {
            AlertUtil.showWarning("Validation Error", "Username contains invalid characters.");
            return false;
        }
        
        String email = emailField.getText();
        if (!SecurityUtil.isValidEmail(email)) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email address.");
            return false;
        }
        
        String password = passwordField.getText();
        if (!SecurityUtil.isValidPassword(password)) {
            AlertUtil.showWarning("Validation Error", 
                "Password must be at least 8 characters with uppercase, lowercase, and number.");
            return false;
        }
        
        if (!password.equals(confirmPasswordField.getText())) {
            AlertUtil.showWarning("Validation Error", "Passwords do not match.");
            return false;
        }
        
        return true;
    }
}