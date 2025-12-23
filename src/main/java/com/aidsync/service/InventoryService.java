package com.aidsync.service;

import com.aidsync.config.DatabaseConfig;
import com.aidsync.model.InventoryItem;
import com.aidsync.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    public List<InventoryItem> getAllInventoryItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        
        String sql = "SELECT i.*, c.name as category_name, s.name as supplier_name " +
            "FROM inventory_items i " +
            "LEFT JOIN inventory_categories c ON i.category_id = c.id " +
            "LEFT JOIN suppliers s ON i.supplier_id = s.id " +
            "WHERE i.status = 'ACTIVE' " +
            "ORDER BY i.item_name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        }
        
        return items;
    }
    
    public List<InventoryItem> searchInventoryItems(String searchText, String categoryFilter, String statusFilter) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("SELECT i.*, c.name as category_name, s.name as supplier_name " +
            "FROM inventory_items i " +
            "LEFT JOIN inventory_categories c ON i.category_id = c.id " +
            "LEFT JOIN suppliers s ON i.supplier_id = s.id " +
            "WHERE i.status = 'ACTIVE'");
        
        List<Object> params = new ArrayList<>();
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (i.item_name LIKE ? OR i.item_code LIKE ?)");
            String searchPattern = "%" + searchText.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (categoryFilter != null && !categoryFilter.equals("All Categories")) {
            sql.append(" AND c.name = ?");
            params.add(categoryFilter);
        }
        
        if (statusFilter != null && !statusFilter.equals("All")) {
            switch (statusFilter) {
                case "Low Stock":
                    sql.append(" AND i.current_stock <= i.minimum_stock_level");
                    break;
                case "Critical":
                    sql.append(" AND i.current_stock < (i.minimum_stock_level * 0.5)");
                    break;
                case "Out of Stock":
                    sql.append(" AND i.current_stock = 0");
                    break;
                case "Overstock":
                    sql.append(" AND i.current_stock >= i.maximum_stock_level");
                    break;
            }
        }
        
        sql.append(" ORDER BY i.item_name");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToInventoryItem(rs));
                }
            }
        }
        
        return items;
    }
    
    public void createInventoryItem(InventoryItem item) throws SQLException {
        String itemCode = generateItemCode(item.getCategoryName());
        item.setItemCode(itemCode);
        
        String sql = "INSERT INTO inventory_items (item_code, item_name, category_id, unit_of_measure, " +
            "current_stock, minimum_stock_level, maximum_stock_level, unit_cost, supplier_id, " +
            "batch_number, manufacture_date, expiration_date, storage_location, storage_conditions, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, item.getItemCode());
            stmt.setString(2, item.getItemName());
            stmt.setInt(3, getCategoryId(conn, item.getCategoryName()));
            stmt.setString(4, item.getUnitOfMeasure());
            stmt.setBigDecimal(5, item.getCurrentStock());
            stmt.setBigDecimal(6, item.getMinimumStockLevel());
            stmt.setBigDecimal(7, item.getMaximumStockLevel());
            stmt.setBigDecimal(8, item.getUnitCost());
            stmt.setObject(9, getSupplierId(conn, item.getSupplierName()));
            stmt.setString(10, item.getBatchNumber());
            stmt.setDate(11, item.getManufactureDate() != null ? Date.valueOf(item.getManufactureDate()) : null);
            stmt.setDate(12, item.getExpirationDate() != null ? Date.valueOf(item.getExpirationDate()) : null);
            stmt.setString(13, item.getStorageLocation());
            stmt.setString(14, item.getStorageConditions());
            stmt.setString(15, item.getStatus().name());
            
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }
            
            // Record initial stock movement
            if (item.getCurrentStock().compareTo(BigDecimal.ZERO) > 0) {
                recordStockMovement(item.getId(), "IN", item.getCurrentStock(), "Initial stock");
            }
            
            logger.info("Created inventory item: {}", item.getItemCode());
        }
    }
    
    public void updateInventoryItem(InventoryItem item) throws SQLException {
        String sql = "UPDATE inventory_items SET item_name = ?, category_id = ?, unit_of_measure = ?, " +
            "minimum_stock_level = ?, maximum_stock_level = ?, unit_cost = ?, supplier_id = ?, " +
            "batch_number = ?, manufacture_date = ?, expiration_date = ?, storage_location = ?, " +
            "storage_conditions = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, item.getItemName());
            stmt.setInt(2, getCategoryId(conn, item.getCategoryName()));
            stmt.setString(3, item.getUnitOfMeasure());
            stmt.setBigDecimal(4, item.getMinimumStockLevel());
            stmt.setBigDecimal(5, item.getMaximumStockLevel());
            stmt.setBigDecimal(6, item.getUnitCost());
            stmt.setObject(7, getSupplierId(conn, item.getSupplierName()));
            stmt.setString(8, item.getBatchNumber());
            stmt.setDate(9, item.getManufactureDate() != null ? Date.valueOf(item.getManufactureDate()) : null);
            stmt.setDate(10, item.getExpirationDate() != null ? Date.valueOf(item.getExpirationDate()) : null);
            stmt.setString(11, item.getStorageLocation());
            stmt.setString(12, item.getStorageConditions());
            stmt.setString(13, item.getStatus().name());
            stmt.setInt(14, item.getId());
            
            stmt.executeUpdate();
            
            logger.info("Updated inventory item: {}", item.getItemCode());
        }
    }
    
    public void deleteInventoryItem(int id) throws SQLException {
        String sql = "UPDATE inventory_items SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
            logger.info("Deleted inventory item with ID: {}", id);
        }
    }
    
    public void recordStockMovement(int itemId, String movementType, BigDecimal quantity, String reason) throws SQLException {
        Connection conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Insert stock movement record
            String movementSql = "INSERT INTO stock_movements (item_id, movement_type, quantity, reason, performed_by) " +
                "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(movementSql)) {
                stmt.setInt(1, itemId);
                stmt.setString(2, movementType);
                stmt.setBigDecimal(3, quantity);
                stmt.setString(4, reason);
                stmt.setInt(5, SessionManager.getInstance().getCurrentUser().getId());
                
                stmt.executeUpdate();
            }
            
            // Update current stock
            String updateSql;
            switch (movementType) {
                case "IN":
                case "ADJUSTMENT":
                    updateSql = "UPDATE inventory_items SET current_stock = current_stock + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    break;
                case "OUT":
                case "WRITEOFF":
                    updateSql = "UPDATE inventory_items SET current_stock = current_stock - ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    break;
                default:
                    updateSql = "UPDATE inventory_items SET current_stock = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                    break;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setBigDecimal(1, quantity);
                stmt.setInt(2, itemId);
                
                stmt.executeUpdate();
            }
            
            conn.commit();
            logger.info("Recorded stock movement: {} {} for item ID {}", movementType, quantity, itemId);
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        
        String sql = "SELECT name FROM inventory_categories ORDER BY name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        }
        
        return categories;
    }
    
    public List<String> getAllSuppliers() throws SQLException {
        List<String> suppliers = new ArrayList<>();
        
        String sql = "SELECT name FROM suppliers WHERE status = 'ACTIVE' ORDER BY name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                suppliers.add(rs.getString("name"));
            }
        }
        
        return suppliers;
    }
    
    private String generateItemCode(String categoryName) throws SQLException {
        String categoryCode = getCategoryCode(categoryName);
        
        String sql = DatabaseConfig.isUsingMySQL() ?
            "SELECT MAX(CAST(SUBSTRING(item_code, 6) AS UNSIGNED)) as max_id FROM inventory_items WHERE item_code LIKE ?" :
            "SELECT MAX(CAST(SUBSTR(item_code, 6) AS INTEGER)) as max_id FROM inventory_items WHERE item_code LIKE ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categoryCode + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                int nextId = 1;
                if (rs.next()) {
                    nextId = rs.getInt("max_id") + 1;
                }
                
                return String.format("%s-%03d", categoryCode, nextId);
            }
        }
    }
    
    private String getCategoryCode(String categoryName) {
        switch (categoryName) {
            case "Food & Nutrition": return "FOOD";
            case "Hygiene & Sanitation": return "HYGN";
            case "Medical & Health": return "MEDL";
            case "Clothing & Bedding": return "CLTH";
            case "Shelter Materials": return "SHLT";
            case "Cash Assistance": return "CASH";
            case "Livelihood Support": return "LIVE";
            case "Educational Materials": return "EDUC";
            case "Specialized Kits": return "SPEC";
            default: return "MISC";
        }
    }
    
    private int getCategoryId(Connection conn, String categoryName) throws SQLException {
        String sql = "SELECT id FROM inventory_categories WHERE name = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("Category not found: " + categoryName);
                }
            }
        }
    }
    
    private Integer getSupplierId(Connection conn, String supplierName) throws SQLException {
        if (supplierName == null || supplierName.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT id FROM suppliers WHERE name = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplierName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    return null;
                }
            }
        }
    }
    
    private InventoryItem mapResultSetToInventoryItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        
        item.setId(rs.getInt("id"));
        item.setItemCode(rs.getString("item_code"));
        item.setItemName(rs.getString("item_name"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setUnitOfMeasure(rs.getString("unit_of_measure"));
        item.setCurrentStock(rs.getBigDecimal("current_stock"));
        item.setMinimumStockLevel(rs.getBigDecimal("minimum_stock_level"));
        item.setMaximumStockLevel(rs.getBigDecimal("maximum_stock_level"));
        item.setUnitCost(rs.getBigDecimal("unit_cost"));
        item.setSupplierId(rs.getInt("supplier_id"));
        item.setSupplierName(rs.getString("supplier_name"));
        item.setBatchNumber(rs.getString("batch_number"));
        
        Date manufactureDate = rs.getDate("manufacture_date");
        if (manufactureDate != null) {
            item.setManufactureDate(manufactureDate.toLocalDate());
        }
        
        Date expirationDate = rs.getDate("expiration_date");
        if (expirationDate != null) {
            item.setExpirationDate(expirationDate.toLocalDate());
        }
        
        item.setStorageLocation(rs.getString("storage_location"));
        item.setStorageConditions(rs.getString("storage_conditions"));
        item.setPhotoPath(rs.getString("photo_path"));
        item.setBarcode(rs.getString("barcode"));
        item.setStatus(InventoryItem.ItemStatus.valueOf(rs.getString("status")));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return item;
    }
}