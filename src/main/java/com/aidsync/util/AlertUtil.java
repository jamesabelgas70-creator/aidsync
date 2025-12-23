package com.aidsync.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AlertUtil {
    private static final Logger logger = LoggerFactory.getLogger(AlertUtil.class);
    
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        setIcon(alert);
        alert.showAndWait();
    }
    
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        setIcon(alert);
        alert.showAndWait();
    }
    
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        setIcon(alert);
        alert.showAndWait();
    }
    
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        setIcon(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    public static Optional<String> showTextInput(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        setIcon(dialog);
        
        return dialog.showAndWait();
    }
    
    private static javafx.scene.image.Image cachedIcon;
    
    private static void setIcon(Alert alert) {
        setIconForDialog(alert.getDialogPane());
    }
    
    private static void setIcon(TextInputDialog dialog) {
        setIconForDialog(dialog.getDialogPane());
    }
    
    private static void setIconForDialog(javafx.scene.control.DialogPane dialogPane) {
        if (dialogPane == null) {
            return;
        }
        
        try {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            if (stage != null) {
                if (cachedIcon == null) {
                    java.io.InputStream iconStream = AlertUtil.class.getResourceAsStream("/images/aidsync-icon.png");
                    if (iconStream != null) {
                        cachedIcon = new javafx.scene.image.Image(iconStream);
                    }
                }
                if (cachedIcon != null) {
                    stage.getIcons().add(cachedIcon);
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to set icon for dialog", e);
        }
    }
}