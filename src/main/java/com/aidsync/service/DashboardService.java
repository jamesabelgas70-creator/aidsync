package com.aidsync.service;

import com.aidsync.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    public int getTotalBeneficiaries() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT COUNT(*) FROM beneficiaries WHERE status = 'ACTIVE'";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting total beneficiaries count", e);
        }
        return 0;
    }
    
    public int getDistributionsToday() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = DatabaseConfig.isUsingMySQL() ?
            "SELECT COUNT(*) FROM distributions WHERE DATE(created_at) = CURDATE()" :
            "SELECT COUNT(*) FROM distributions WHERE DATE(created_at) = DATE('now')";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting today's distributions count", e);
        }
        return 0;
    }
    
    public int getLowStockItemsCount() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT COUNT(*) FROM inventory_items WHERE current_stock <= minimum_stock_level AND status = 'ACTIVE'";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting low stock items count", e);
        }
        return 0;
    }
    
    public int getPendingTasksCount() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = DatabaseConfig.isUsingMySQL() ?
            "SELECT COUNT(*) FROM distribution_events WHERE status = 'PLANNED' AND planned_date >= CURDATE()" :
            "SELECT COUNT(*) FROM distribution_events WHERE status = 'PLANNED' AND planned_date >= DATE('now')";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting pending tasks count", e);
        }
        return 0;
    }
    
    public double getBeneficiaryGrowthPercentage() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get current month count
            String currentSql = DatabaseConfig.isUsingMySQL() ?
                "SELECT COUNT(*) FROM beneficiaries WHERE MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())" :
                "SELECT COUNT(*) FROM beneficiaries WHERE strftime('%m', created_at) = strftime('%m', 'now') AND strftime('%Y', created_at) = strftime('%Y', 'now')";
            int currentMonth = 0;
            try (PreparedStatement stmt = conn.prepareStatement(currentSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    currentMonth = rs.getInt(1);
                }
            }
            
            // Get previous month count
            String previousSql = DatabaseConfig.isUsingMySQL() ?
                "SELECT COUNT(*) FROM beneficiaries WHERE MONTH(created_at) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) AND YEAR(created_at) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))" :
                "SELECT COUNT(*) FROM beneficiaries WHERE strftime('%m', created_at) = strftime('%m', 'now', '-1 month') AND strftime('%Y', created_at) = strftime('%Y', 'now', '-1 month')";
            int previousMonth = 0;
            try (PreparedStatement stmt = conn.prepareStatement(previousSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    previousMonth = rs.getInt(1);
                }
            }
            
            if (previousMonth == 0) return 0;
            return ((double)(currentMonth - previousMonth) / previousMonth) * 100;
            
        } catch (SQLException e) {
            logger.error("Error calculating beneficiary growth percentage", e);
        }
        return 0;
    }
    
    public double getDistributionGrowthPercentage() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Get today's count
            String todaySql = DatabaseConfig.isUsingMySQL() ?
                "SELECT COUNT(*) FROM distributions WHERE DATE(created_at) = CURDATE()" :
                "SELECT COUNT(*) FROM distributions WHERE DATE(created_at) = DATE('now')";
            int today = 0;
            try (PreparedStatement stmt = conn.prepareStatement(todaySql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    today = rs.getInt(1);
                }
            }
            
            // Get yesterday's count
            String yesterdaySql = DatabaseConfig.isUsingMySQL() ?
                "SELECT COUNT(*) FROM distributions WHERE DATE(created_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)" :
                "SELECT COUNT(*) FROM distributions WHERE DATE(created_at) = DATE('now', '-1 day')";
            int yesterday = 0;
            try (PreparedStatement stmt = conn.prepareStatement(yesterdaySql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    yesterday = rs.getInt(1);
                }
            }
            
            if (yesterday == 0) return 0;
            return ((double)(today - yesterday) / yesterday) * 100;
            
        } catch (SQLException e) {
            logger.error("Error calculating distribution growth percentage", e);
        }
        return 0;
    }
}