package com.aidsync.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryItem {
    private int id;
    private String itemCode;
    private String itemName;
    private int categoryId;
    private String categoryName;
    private String unitOfMeasure;
    private BigDecimal currentStock;
    private BigDecimal minimumStockLevel;
    private BigDecimal maximumStockLevel;
    private BigDecimal unitCost;
    private int supplierId;
    private String supplierName;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expirationDate;
    private String storageLocation;
    private String storageConditions;
    private String photoPath;
    private String barcode;
    private ItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum ItemStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        DISCONTINUED("Discontinued");
        
        private final String displayName;
        ItemStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum StockStatus {
        NORMAL("Normal"),
        LOW("Low Stock"),
        CRITICAL("Critical"),
        OUT_OF_STOCK("Out of Stock"),
        OVERSTOCK("Overstock");
        
        private final String displayName;
        StockStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    // Constructors
    public InventoryItem() {}
    
    public InventoryItem(String itemName, String itemCode, int categoryId) {
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.categoryId = categoryId;
        this.currentStock = BigDecimal.ZERO;
        this.minimumStockLevel = BigDecimal.ZERO;
        this.maximumStockLevel = BigDecimal.ZERO;
        this.unitCost = BigDecimal.ZERO;
        this.status = ItemStatus.ACTIVE;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    
    public BigDecimal getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(BigDecimal minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }
    
    public BigDecimal getMaximumStockLevel() { return maximumStockLevel; }
    public void setMaximumStockLevel(BigDecimal maximumStockLevel) { this.maximumStockLevel = maximumStockLevel; }
    
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    
    public LocalDate getManufactureDate() { return manufactureDate; }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    
    public String getStorageConditions() { return storageConditions; }
    public void setStorageConditions(String storageConditions) { this.storageConditions = storageConditions; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Utility methods
    public BigDecimal getTotalValue() {
        if (currentStock == null || unitCost == null) return BigDecimal.ZERO;
        return currentStock.multiply(unitCost);
    }
    
    public StockStatus getStockStatus() {
        if (currentStock == null || minimumStockLevel == null || maximumStockLevel == null) {
            return StockStatus.NORMAL;
        }
        
        if (currentStock.compareTo(BigDecimal.ZERO) == 0) {
            return StockStatus.OUT_OF_STOCK;
        } else if (currentStock.compareTo(minimumStockLevel) <= 0) {
            return StockStatus.LOW;
        } else if (currentStock.compareTo(maximumStockLevel) >= 0) {
            return StockStatus.OVERSTOCK;
        } else {
            return StockStatus.NORMAL;
        }
    }
    
    public boolean isExpiringSoon(int daysThreshold) {
        if (expirationDate == null) return false;
        return expirationDate.isBefore(LocalDate.now().plusDays(daysThreshold));
    }
    
    public boolean isExpired() {
        if (expirationDate == null) return false;
        return expirationDate.isBefore(LocalDate.now());
    }
    
    public long getDaysToExpiry() {
        if (expirationDate == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }
    
    @Override
    public String toString() {
        return itemName + " (" + itemCode + ")";
    }
}