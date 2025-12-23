package com.aidsync.controller;

import com.aidsync.model.User;
import com.aidsync.service.DashboardService;
import com.aidsync.util.AlertUtil;
import com.aidsync.util.SceneManager;
import com.aidsync.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    // Header
    @FXML private Label welcomeLabel;
    @FXML private Label dateTimeLabel;
    // Quick Actions
    @FXML private Button addBeneficiaryButton;
    @FXML private Button recordDistributionButton;
    @FXML private Button emergencyModeButton;
    
    // Statistics Cards
    @FXML private Label totalBeneficiariesLabel;
    @FXML private Label distributionsTodayLabel;
    @FXML private Label lowStockItemsLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label totalBeneficiariesChangeLabel;
    @FXML private Label distributionsTodayChangeLabel;
    
    // Activity Feed
    @FXML private VBox activityFeedVBox;
    
    // Navigation Menu
    @FXML private Button dashboardMenuButton;
    @FXML private Button beneficiaryMenuButton;
    @FXML private Button inventoryMenuButton;
    @FXML private Button distributionMenuButton;
    @FXML private Button reportingMenuButton;
    @FXML private Button gisMenuButton;
    @FXML private Button adminMenuButton;
    
    private DashboardService dashboardService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardService = new DashboardService();
        
        setupUserInterface();
        loadDashboardData();
        startDateTimeUpdater();
        
        // Update activity every 30 seconds
        SessionManager.getInstance().updateActivity();
    }
    
    private void setupUserInterface() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            
            // Configure menu visibility based on user role
            configureMenuAccess(currentUser);
        }
        
        // Set current date and time
        updateDateTime();
    }
    
    private void configureMenuAccess(User user) {
        // All users can access dashboard and beneficiary management
        dashboardMenuButton.setDisable(false);
        beneficiaryMenuButton.setDisable(false);
        
        // Role-based access control
        switch (user.getRole()) {
            case SUPER_ADMIN:
            case LGU_ADMIN:
                // Full access
                inventoryMenuButton.setDisable(false);
                distributionMenuButton.setDisable(false);
                reportingMenuButton.setDisable(false);
                gisMenuButton.setDisable(false);
                adminMenuButton.setDisable(false);
                emergencyModeButton.setDisable(false);
                break;
                
            case BARANGAY_CAPTAIN:
                // Limited access
                inventoryMenuButton.setDisable(false);
                distributionMenuButton.setDisable(false);
                reportingMenuButton.setDisable(false);
                gisMenuButton.setDisable(true);
                adminMenuButton.setDisable(true);
                emergencyModeButton.setDisable(false);
                break;
                
            case DISTRIBUTION_STAFF:
                // Distribution focused
                inventoryMenuButton.setDisable(false);
                distributionMenuButton.setDisable(false);
                reportingMenuButton.setDisable(true);
                gisMenuButton.setDisable(true);
                adminMenuButton.setDisable(true);
                emergencyModeButton.setDisable(false);
                break;
                
            case VIEWER:
                // Read-only access
                inventoryMenuButton.setDisable(true);
                distributionMenuButton.setDisable(true);
                reportingMenuButton.setDisable(false);
                gisMenuButton.setDisable(true);
                adminMenuButton.setDisable(true);
                emergencyModeButton.setDisable(true);
                
                // Disable action buttons for viewers
                addBeneficiaryButton.setDisable(true);
                recordDistributionButton.setDisable(true);
                break;
        }
    }
    
    private void loadDashboardData() {
        try {
            // Load statistics
            int totalBeneficiaries = dashboardService.getTotalBeneficiaries();
            int distributionsToday = dashboardService.getDistributionsToday();
            int lowStockItems = dashboardService.getLowStockItemsCount();
            int pendingTasks = dashboardService.getPendingTasksCount();
            
            // Update UI
            totalBeneficiariesLabel.setText(String.valueOf(totalBeneficiaries));
            distributionsTodayLabel.setText(String.valueOf(distributionsToday));
            lowStockItemsLabel.setText(String.valueOf(lowStockItems));
            pendingTasksLabel.setText(String.valueOf(pendingTasks));
            
            // Set alert styles for critical items
            if (lowStockItems > 0) {
                lowStockItemsLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            }
            
            // Load activity feed
            loadActivityFeed();
            
        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            AlertUtil.showError("Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    private void loadActivityFeed() {
        // Clear existing items
        activityFeedVBox.getChildren().clear();
        
        // Add recent activities
        addActivityItem("New beneficiary registered: Maria Santos", "2 minutes ago");
        addActivityItem("Distribution completed at Badas Barangay", "15 minutes ago");
        addActivityItem("Inventory updated: Rice stock replenished", "1 hour ago");
        addActivityItem("Report generated: Monthly Distribution Summary", "2 hours ago");
    }
    
    private void addActivityItem(String activity, String time) {
        Label activityLabel = new Label(activity);
        activityLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        VBox activityItem = new VBox(2, activityLabel, timeLabel);
        activityItem.setStyle("-fx-padding: 5px; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        
        activityFeedVBox.getChildren().add(activityItem);
    }
    
    private void startDateTimeUpdater() {
        // Update date/time every minute
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.minutes(1), e -> updateDateTime())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy - hh:mm a", java.util.Locale.ENGLISH);
        dateTimeLabel.setText(now.format(formatter));
    }
    
    // Event Handlers
    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            SceneManager.switchToLogin();
        }
    }
    
    @FXML
    private void handleAddBeneficiary() {
        SceneManager.switchToBeneficiaryManagement();
    }
    
    @FXML
    private void handleRecordDistribution() {
        SceneManager.switchToDistributionManagement();
    }
    
    @FXML
    private void handleCheckInventory() {
        SceneManager.switchToInventoryManagement();
    }
    
    @FXML
    private void handleGenerateReport() {
        AlertUtil.showInfo("Generate Report", "Report generation feature coming soon!");
    }
    
    @FXML
    private void handleEmergencyMode() {
        if (AlertUtil.showConfirmation("Emergency Mode", 
            "Switch to Emergency Mode? This will simplify the interface for crisis situations.")) {
            AlertUtil.showInfo("Emergency Mode", "Emergency Mode activated!");
        }
    }
    
    // Navigation Menu Handlers
    @FXML
    private void handleDashboardMenu() {
        // Already on dashboard
    }
    
    @FXML
    private void handleBeneficiaryMenu() {
        SceneManager.switchToBeneficiaryManagement();
    }
    
    @FXML
    private void handleInventoryMenu() {
        SceneManager.switchToInventoryManagement();
    }
    
    @FXML
    private void handleDistributionMenu() {
        SceneManager.switchToDistributionManagement();
    }
    
    @FXML
    private void handleReportingMenu() {
        AlertUtil.showInfo("Reporting", "Reporting module coming soon!");
    }
    
    @FXML
    private void handleGISMenu() {
        AlertUtil.showInfo("GIS Mapping", "GIS mapping module coming soon!");
    }
    
    @FXML
    private void handleAdminMenu() {
        AlertUtil.showInfo("Administration", "Administration module coming soon!");
    }
}