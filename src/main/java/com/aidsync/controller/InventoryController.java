package com.aidsync.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aidsync.model.InventoryItem;
import com.aidsync.service.InventoryService;
import com.aidsync.util.AlertUtil;
import com.aidsync.util.SceneManager;
import com.aidsync.util.SecurityUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class InventoryController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> codeColumn;
    @FXML private TableColumn<InventoryItem, String> nameColumn;
    @FXML private TableColumn<InventoryItem, String> categoryColumn;
    @FXML private TableColumn<InventoryItem, BigDecimal> stockColumn;
    @FXML private TableColumn<InventoryItem, String> unitColumn;
    @FXML private TableColumn<InventoryItem, String> statusColumn;
    @FXML private TableColumn<InventoryItem, BigDecimal> valueColumn;
    
    // Form fields
    @FXML private TextField itemCodeField;
    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField unitOfMeasureField;
    @FXML private TextField currentStockField;
    @FXML private TextField minimumStockField;
    @FXML private TextField maximumStockField;
    @FXML private TextField unitCostField;
    @FXML private ComboBox<String> supplierComboBox;
    @FXML private TextField batchNumberField;
    @FXML private DatePicker manufactureDatePicker;
    @FXML private DatePicker expirationDatePicker;
    @FXML private TextField storageLocationField;
    @FXML private TextArea storageConditionsArea;
    @FXML private ComboBox<InventoryItem.ItemStatus> itemStatusComboBox;
    @FXML private ComboBox<String> movementTypeComboBox;
    
    @FXML private Button deleteButton;
    @FXML private Button adjustStockButton;
    
    private InventoryService inventoryService;
    private ObservableList<InventoryItem> inventoryList;
    private InventoryItem selectedItem;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inventoryService = new InventoryService();
        inventoryList = FXCollections.observableArrayList();
        
        setupTable();
        setupForm();
        loadInventoryItems();
        setupFilters();
    }
    
    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStockStatus().getDisplayName()));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        
        statusColumn.setCellFactory(column -> new TableCell<InventoryItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Low Stock":
                        case "Critical":
                            setStyle("-fx-text-fill: #ff5722; -fx-font-weight: bold;");
                            break;
                        case "Out of Stock":
                            setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                            break;
                        case "Overstock":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #4caf50;");
                            break;
                    }
                }
            }
        });
        
        inventoryTable.setItems(inventoryList);
        inventoryTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedItem = newSelection;
                    populateForm(newSelection);
                }
            });
    }
    
    private void setupForm() {
        itemStatusComboBox.setItems(FXCollections.observableArrayList(InventoryItem.ItemStatus.values()));
        movementTypeComboBox.setItems(FXCollections.observableArrayList("IN", "OUT", "ADJUSTMENT", "TRANSFER", "WRITEOFF"));
        
        loadCategories();
        loadSuppliers();
        
        itemStatusComboBox.setValue(InventoryItem.ItemStatus.ACTIVE);
        currentStockField.setText("0");
        minimumStockField.setText("0");
        maximumStockField.setText("0");
        unitCostField.setText("0.00");
    }
    
    private void loadCategories() {
        try {
            List<String> categories = inventoryService.getAllCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryFilter.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            logger.error("Error loading categories", e);
            AlertUtil.showError("Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    private void loadSuppliers() {
        try {
            List<String> suppliers = inventoryService.getAllSuppliers();
            supplierComboBox.setItems(FXCollections.observableArrayList(suppliers));
        } catch (Exception e) {
            logger.error("Error loading suppliers", e);
            AlertUtil.showError("Error", "Failed to load suppliers: " + e.getMessage());
        }
    }
    
    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Normal", "Low Stock", "Critical", "Out of Stock", "Overstock"));
        statusFilter.setValue("All");
        categoryFilter.setValue("All Categories");
        
        searchField.textProperty().addListener((obs, oldText, newText) -> filterInventory());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterInventory());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterInventory());
    }
    
    private void loadInventoryItems() {
        try {
            List<InventoryItem> items = inventoryService.getAllInventoryItems();
            inventoryList.setAll(items);
        } catch (Exception e) {
            logger.error("Error loading inventory items", e);
            AlertUtil.showError("Error", "Failed to load inventory items: " + e.getMessage());
        }
    }
    
    private void filterInventory() {
        try {
            String searchText = searchField.getText();
            String selectedCategory = this.categoryFilter.getValue();
            String selectedStatus = this.statusFilter.getValue();
            
            if (searchText != null && !SecurityUtil.isInputSafe(searchText)) {
                logger.warn("Suspicious search input detected: {}", SecurityUtil.sanitizeForLogging(searchText));
                AlertUtil.showWarning("Invalid Input", "Search contains invalid characters.");
                return;
            }
            
            List<InventoryItem> filtered = inventoryService.searchInventoryItems(searchText, selectedCategory, selectedStatus);
            if (filtered != null) {
                inventoryList.setAll(filtered);
            }
        } catch (Exception e) {
            logger.error("Error filtering inventory", e);
            AlertUtil.showError("Search Error", "Failed to filter inventory. Please try again.");
        }
    }
    
    private void populateForm(InventoryItem item) {
        itemCodeField.setText(item.getItemCode());
        itemNameField.setText(item.getItemName());
        categoryComboBox.setValue(item.getCategoryName());
        unitOfMeasureField.setText(item.getUnitOfMeasure());
        currentStockField.setText(item.getCurrentStock().toString());
        minimumStockField.setText(item.getMinimumStockLevel().toString());
        maximumStockField.setText(item.getMaximumStockLevel().toString());
        unitCostField.setText(item.getUnitCost().toString());
        supplierComboBox.setValue(item.getSupplierName());
        batchNumberField.setText(item.getBatchNumber());
        manufactureDatePicker.setValue(item.getManufactureDate());
        expirationDatePicker.setValue(item.getExpirationDate());
        storageLocationField.setText(item.getStorageLocation());
        storageConditionsArea.setText(item.getStorageConditions());
        itemStatusComboBox.setValue(item.getStatus());
        
        deleteButton.setDisable(false);
        adjustStockButton.setDisable(false);
    }
    
    private void clearForm() {
        itemCodeField.clear();
        itemNameField.clear();
        categoryComboBox.setValue(null);
        unitOfMeasureField.clear();
        currentStockField.setText("0");
        minimumStockField.setText("0");
        maximumStockField.setText("0");
        unitCostField.setText("0.00");
        supplierComboBox.setValue(null);
        batchNumberField.clear();
        manufactureDatePicker.setValue(null);
        expirationDatePicker.setValue(null);
        storageLocationField.clear();
        storageConditionsArea.clear();
        itemStatusComboBox.setValue(InventoryItem.ItemStatus.ACTIVE);
        
        selectedItem = null;
        deleteButton.setDisable(true);
        adjustStockButton.setDisable(true);
    }
    
    @FXML
    private void handleSave() {
        try {
            if (!validateForm()) return;
            
            InventoryItem item = selectedItem != null ? selectedItem : new InventoryItem();
            
            item.setItemCode(itemCodeField.getText().trim());
            item.setItemName(itemNameField.getText().trim());
            item.setCategoryName(categoryComboBox.getValue());
            item.setUnitOfMeasure(unitOfMeasureField.getText().trim());
            item.setCurrentStock(new BigDecimal(currentStockField.getText()));
            item.setMinimumStockLevel(new BigDecimal(minimumStockField.getText()));
            item.setMaximumStockLevel(new BigDecimal(maximumStockField.getText()));
            item.setUnitCost(new BigDecimal(unitCostField.getText()));
            item.setSupplierName(supplierComboBox.getValue());
            item.setBatchNumber(batchNumberField.getText().trim());
            item.setManufactureDate(manufactureDatePicker.getValue());
            item.setExpirationDate(expirationDatePicker.getValue());
            item.setStorageLocation(storageLocationField.getText().trim());
            item.setStorageConditions(storageConditionsArea.getText().trim());
            item.setStatus(itemStatusComboBox.getValue());
            
            if (selectedItem == null) {
                inventoryService.createInventoryItem(item);
                AlertUtil.showInfo("Success", "Inventory item created successfully!");
            } else {
                inventoryService.updateInventoryItem(item);
                AlertUtil.showInfo("Success", "Inventory item updated successfully!");
            }
            
            loadInventoryItems();
            clearForm();
            
        } catch (Exception e) {
            logger.error("Error saving inventory item", e);
            AlertUtil.showError("Error", "Failed to save inventory item: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClear() {
        clearForm();
    }
    
    @FXML
    private void handleDelete() {
        if (selectedItem == null) return;
        
        if (AlertUtil.showConfirmation("Delete Item", 
            "Are you sure you want to delete " + selectedItem.getItemName() + "?")) {
            try {
                inventoryService.deleteInventoryItem(selectedItem.getId());
                AlertUtil.showInfo("Success", "Inventory item deleted successfully!");
                loadInventoryItems();
                clearForm();
            } catch (Exception e) {
                logger.error("Error deleting inventory item", e);
                AlertUtil.showError("Error", "Failed to delete inventory item: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleAdjustStock() {
        if (selectedItem == null) return;
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Adjust Stock - " + selectedItem.getItemName());
        dialog.setHeaderText("Current Stock: " + selectedItem.getCurrentStock() + " " + selectedItem.getUnitOfMeasure());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("IN", "OUT", "ADJUSTMENT"));
        typeCombo.setValue("ADJUSTMENT");
        
        TextField adjustQuantityField = new TextField();
        adjustQuantityField.setPromptText("Enter quantity");
        
        TextField adjustReasonField = new TextField();
        adjustReasonField.setPromptText("Enter reason");
        
        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(adjustQuantityField, 1, 1);
        grid.add(new Label("Reason:"), 0, 2);
        grid.add(adjustReasonField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    String movementType = typeCombo.getValue();
                    BigDecimal adjustQuantity = new BigDecimal(adjustQuantityField.getText());
                    String adjustReason = adjustReasonField.getText();
                    
                    inventoryService.recordStockMovement(selectedItem.getId(), movementType, adjustQuantity, adjustReason);
                    AlertUtil.showInfo("Success", "Stock adjustment recorded successfully!");
                    loadInventoryItems();
                    
                } catch (Exception e) {
                    logger.error("Error adjusting stock", e);
                    AlertUtil.showError("Error", "Failed to adjust stock: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleBack() {
        SceneManager.switchToDashboard();
    }
    
    private boolean validateForm() {
        String itemCode = itemCodeField.getText();
        if (itemCode == null || itemCode.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Item code is required.");
            return false;
        }
        
        if (!SecurityUtil.isInputSafe(itemCode)) {
            AlertUtil.showWarning("Validation Error", "Item code contains invalid characters.");
            logger.warn("Suspicious input in item code: {}", SecurityUtil.sanitizeForLogging(itemCode));
            return false;
        }
        
        String itemName = itemNameField.getText();
        if (itemName == null || itemName.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Item name is required.");
            return false;
        }
        
        if (!SecurityUtil.isInputSafe(itemName)) {
            AlertUtil.showWarning("Validation Error", "Item name contains invalid characters.");
            return false;
        }
        
        if (categoryComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Category is required.");
            return false;
        }
        
        String unitOfMeasure = unitOfMeasureField.getText();
        if (unitOfMeasure == null || unitOfMeasure.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Unit of measure is required.");
            return false;
        }
        
        try {
            BigDecimal currentStock = new BigDecimal(currentStockField.getText());
            BigDecimal minStock = new BigDecimal(minimumStockField.getText());
            BigDecimal maxStock = new BigDecimal(maximumStockField.getText());
            BigDecimal unitCost = new BigDecimal(unitCostField.getText());
            
            if (currentStock.compareTo(BigDecimal.ZERO) < 0 || minStock.compareTo(BigDecimal.ZERO) < 0 || 
                maxStock.compareTo(BigDecimal.ZERO) < 0 || unitCost.compareTo(BigDecimal.ZERO) < 0) {
                AlertUtil.showWarning("Validation Error", "Stock and cost values cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Please enter valid numbers for stock and cost fields.");
            return false;
        }
        
        return true;
    }
}