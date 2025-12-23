package com.aidsync.service;

import com.aidsync.config.DatabaseConfig;
import com.aidsync.model.Beneficiary;
import com.aidsync.util.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryService {
    private static final Logger logger = LoggerFactory.getLogger(BeneficiaryService.class);
    
    public List<Beneficiary> getAllBeneficiaries() throws SQLException {
        List<Beneficiary> beneficiaries = new ArrayList<>();
        
        String sql = "SELECT b.*, br.name as barangay_name, p.name as purok_name, " +
            "bv.is_pwd, bv.is_senior_citizen, bv.is_pregnant_lactating, bv.is_solo_parent, " +
            "bv.has_chronic_illness, bv.is_orphan, bv.is_indigenous, bv.is_homeless " +
            "FROM beneficiaries b " +
            "LEFT JOIN barangays br ON b.barangay_id = br.id " +
            "LEFT JOIN puroks p ON b.purok_id = p.id " +
            "LEFT JOIN beneficiary_vulnerabilities bv ON b.id = bv.beneficiary_id " +
            "WHERE b.status != 'DECEASED' " +
            "ORDER BY b.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                beneficiaries.add(mapResultSetToBeneficiary(rs));
            }
        }
        
        return beneficiaries;
    }
    
    public List<Beneficiary> searchBeneficiaries(String searchText, String barangayFilter, String statusFilter) throws SQLException {
        List<Beneficiary> beneficiaries = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("SELECT b.*, br.name as barangay_name, p.name as purok_name, " +
            "bv.is_pwd, bv.is_senior_citizen, bv.is_pregnant_lactating, bv.is_solo_parent, " +
            "bv.has_chronic_illness, bv.is_orphan, bv.is_indigenous, bv.is_homeless " +
            "FROM beneficiaries b " +
            "LEFT JOIN barangays br ON b.barangay_id = br.id " +
            "LEFT JOIN puroks p ON b.purok_id = p.id " +
            "LEFT JOIN beneficiary_vulnerabilities bv ON b.id = bv.beneficiary_id " +
            "WHERE 1=1");
        
        List<Object> params = new ArrayList<>();
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (b.full_name LIKE ? OR b.beneficiary_id LIKE ? OR b.contact_number LIKE ?)");
            String searchPattern = "%" + searchText.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (barangayFilter != null && !barangayFilter.equals("All Barangays")) {
            sql.append(" AND br.name = ?");
            params.add(barangayFilter);
        }
        
        if (statusFilter != null && !statusFilter.equals("All")) {
            sql.append(" AND b.status = ?");
            params.add(statusFilter);
        }
        
        sql.append(" ORDER BY b.created_at DESC");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    beneficiaries.add(mapResultSetToBeneficiary(rs));
                }
            }
        }
        
        return beneficiaries;
    }
    
    public void createBeneficiary(Beneficiary beneficiary) throws SQLException {
        String beneficiaryId = generateBeneficiaryId();
        beneficiary.setBeneficiaryId(beneficiaryId);
        
        Connection conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Insert beneficiary
            String sql = "INSERT INTO beneficiaries (beneficiary_id, full_name, birth_date, gender, civil_status, " +
                "contact_number, email, barangay_id, purok_id, street_sitio, is_household_head, " +
                "family_size, monthly_income_range, employment_status, priority_level, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            int beneficiaryDbId;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, beneficiary.getBeneficiaryId());
                stmt.setString(2, beneficiary.getFullName());
                stmt.setDate(3, Date.valueOf(beneficiary.getBirthDate()));
                stmt.setString(4, beneficiary.getGender().name());
                stmt.setString(5, beneficiary.getCivilStatus().name());
                stmt.setString(6, beneficiary.getContactNumber());
                stmt.setString(7, beneficiary.getEmail());
                stmt.setInt(8, getBarangayId(conn, beneficiary.getBarangayName()));
                stmt.setInt(9, 1); // Default purok
                stmt.setString(10, beneficiary.getStreetSitio());
                stmt.setBoolean(11, beneficiary.isHouseholdHead());
                stmt.setInt(12, beneficiary.getFamilySize());
                stmt.setString(13, beneficiary.getMonthlyIncomeRange() != null ? beneficiary.getMonthlyIncomeRange().name() : null);
                stmt.setString(14, beneficiary.getEmploymentStatus() != null ? beneficiary.getEmploymentStatus().name() : null);
                stmt.setInt(15, beneficiary.getPriorityLevel());
                stmt.setInt(16, SessionManager.getInstance().getCurrentUser().getId());
                
                stmt.executeUpdate();
            }
            
            // Get the generated ID
            String getIdSql = DatabaseConfig.isUsingMySQL() ? 
                "SELECT LAST_INSERT_ID() as id" : 
                "SELECT last_insert_rowid() as id";
            
            try (PreparedStatement idStmt = conn.prepareStatement(getIdSql);
                 ResultSet rs = idStmt.executeQuery()) {
                if (rs.next()) {
                    beneficiaryDbId = rs.getInt("id");
                    beneficiary.setId(beneficiaryDbId);
                } else {
                    throw new SQLException("Failed to get generated beneficiary ID");
                }
            }
            
            // Insert vulnerability assessment
            insertVulnerabilityAssessment(conn, beneficiaryDbId, beneficiary);
            
            conn.commit();
            logger.info("Created beneficiary: {}", beneficiary.getBeneficiaryId());
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    public void updateBeneficiary(Beneficiary beneficiary) throws SQLException {
        Connection conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Update beneficiary
            String sql = "UPDATE beneficiaries SET full_name = ?, birth_date = ?, gender = ?, civil_status = ?, " +
                "contact_number = ?, email = ?, barangay_id = ?, street_sitio = ?, is_household_head = ?, " +
                "family_size = ?, monthly_income_range = ?, employment_status = ?, priority_level = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, beneficiary.getFullName());
                stmt.setDate(2, Date.valueOf(beneficiary.getBirthDate()));
                stmt.setString(3, beneficiary.getGender().name());
                stmt.setString(4, beneficiary.getCivilStatus().name());
                stmt.setString(5, beneficiary.getContactNumber());
                stmt.setString(6, beneficiary.getEmail());
                stmt.setInt(7, getBarangayId(conn, beneficiary.getBarangayName()));
                stmt.setString(8, beneficiary.getStreetSitio());
                stmt.setBoolean(9, beneficiary.isHouseholdHead());
                stmt.setInt(10, beneficiary.getFamilySize());
                stmt.setString(11, beneficiary.getMonthlyIncomeRange() != null ? beneficiary.getMonthlyIncomeRange().name() : null);
                stmt.setString(12, beneficiary.getEmploymentStatus() != null ? beneficiary.getEmploymentStatus().name() : null);
                stmt.setInt(13, beneficiary.getPriorityLevel());
                stmt.setInt(14, beneficiary.getId());
                
                stmt.executeUpdate();
            }
            
            // Update vulnerability assessment
            updateVulnerabilityAssessment(conn, beneficiary.getId(), beneficiary);
            
            conn.commit();
            logger.info("Updated beneficiary: {}", beneficiary.getBeneficiaryId());
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    public void deleteBeneficiary(int id) throws SQLException {
        String sql = "UPDATE beneficiaries SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
            logger.info("Deleted beneficiary with ID: {}", id);
        }
    }
    
    public List<String> getAllBarangays() throws SQLException {
        List<String> barangays = new ArrayList<>();
        
        String sql = "SELECT name FROM barangays ORDER BY name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                barangays.add(rs.getString("name"));
            }
        }
        
        return barangays;
    }
    
    private String generateBeneficiaryId() throws SQLException {
        String sql = DatabaseConfig.isUsingMySQL() ? 
            "SELECT MAX(CAST(SUBSTRING(beneficiary_id, 4) AS UNSIGNED)) as max_id FROM beneficiaries WHERE beneficiary_id LIKE 'BEN%'" :
            "SELECT MAX(CAST(SUBSTR(beneficiary_id, 4) AS INTEGER)) as max_id FROM beneficiaries WHERE beneficiary_id LIKE 'BEN%'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt("max_id") + 1;
            }
            
            return String.format("BEN%05d", nextId);
        }
    }
    
    private int getBarangayId(Connection conn, String barangayName) throws SQLException {
        String sql = "SELECT id FROM barangays WHERE name = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, barangayName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("Barangay not found: " + barangayName);
                }
            }
        }
    }
    
    private void insertVulnerabilityAssessment(Connection conn, int beneficiaryId, Beneficiary beneficiary) throws SQLException {
        String sql = "INSERT INTO beneficiary_vulnerabilities (beneficiary_id, is_pwd, is_senior_citizen, " +
            "is_pregnant_lactating, is_solo_parent, has_chronic_illness, is_orphan, " +
            "is_indigenous, is_homeless) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, beneficiaryId);
            stmt.setBoolean(2, beneficiary.isPWD());
            stmt.setBoolean(3, beneficiary.isSeniorCitizen());
            stmt.setBoolean(4, beneficiary.isPregnantLactating());
            stmt.setBoolean(5, beneficiary.isSoloParent());
            stmt.setBoolean(6, beneficiary.isHasChronicIllness());
            stmt.setBoolean(7, beneficiary.isOrphan());
            stmt.setBoolean(8, beneficiary.isIndigenous());
            stmt.setBoolean(9, beneficiary.isHomeless());
            
            stmt.executeUpdate();
        }
    }
    
    private void updateVulnerabilityAssessment(Connection conn, int beneficiaryId, Beneficiary beneficiary) throws SQLException {
        String sql = "UPDATE beneficiary_vulnerabilities SET is_pwd = ?, is_senior_citizen = ?, " +
            "is_pregnant_lactating = ?, is_solo_parent = ?, has_chronic_illness = ?, is_orphan = ?, " +
            "is_indigenous = ?, is_homeless = ?, updated_at = CURRENT_TIMESTAMP " +
            "WHERE beneficiary_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, beneficiary.isPWD());
            stmt.setBoolean(2, beneficiary.isSeniorCitizen());
            stmt.setBoolean(3, beneficiary.isPregnantLactating());
            stmt.setBoolean(4, beneficiary.isSoloParent());
            stmt.setBoolean(5, beneficiary.isHasChronicIllness());
            stmt.setBoolean(6, beneficiary.isOrphan());
            stmt.setBoolean(7, beneficiary.isIndigenous());
            stmt.setBoolean(8, beneficiary.isHomeless());
            stmt.setInt(9, beneficiaryId);
            
            stmt.executeUpdate();
        }
    }
    
    private Beneficiary mapResultSetToBeneficiary(ResultSet rs) throws SQLException {
        Beneficiary beneficiary = new Beneficiary();
        
        beneficiary.setId(rs.getInt("id"));
        beneficiary.setBeneficiaryId(rs.getString("beneficiary_id"));
        beneficiary.setFullName(rs.getString("full_name"));
        
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            beneficiary.setBirthDate(birthDate.toLocalDate());
        }
        
        beneficiary.setGender(Beneficiary.Gender.valueOf(rs.getString("gender")));
        beneficiary.setCivilStatus(Beneficiary.CivilStatus.valueOf(rs.getString("civil_status")));
        beneficiary.setContactNumber(rs.getString("contact_number"));
        beneficiary.setEmail(rs.getString("email"));
        beneficiary.setBarangayId(rs.getInt("barangay_id"));
        beneficiary.setBarangayName(rs.getString("barangay_name"));
        beneficiary.setPurokId(rs.getInt("purok_id"));
        beneficiary.setPurokName(rs.getString("purok_name"));
        beneficiary.setStreetSitio(rs.getString("street_sitio"));
        beneficiary.setHouseholdHead(rs.getBoolean("is_household_head"));
        beneficiary.setFamilySize(rs.getInt("family_size"));
        
        String incomeRange = rs.getString("monthly_income_range");
        if (incomeRange != null) {
            beneficiary.setMonthlyIncomeRange(Beneficiary.IncomeRange.valueOf(incomeRange));
        }
        
        String employmentStatus = rs.getString("employment_status");
        if (employmentStatus != null) {
            beneficiary.setEmploymentStatus(Beneficiary.EmploymentStatus.valueOf(employmentStatus));
        }
        
        beneficiary.setPriorityLevel(rs.getInt("priority_level"));
        beneficiary.setStatus(Beneficiary.BeneficiaryStatus.valueOf(rs.getString("status")));
        
        // Vulnerability flags
        beneficiary.setPWD(rs.getBoolean("is_pwd"));
        beneficiary.setSeniorCitizen(rs.getBoolean("is_senior_citizen"));
        beneficiary.setPregnantLactating(rs.getBoolean("is_pregnant_lactating"));
        beneficiary.setSoloParent(rs.getBoolean("is_solo_parent"));
        beneficiary.setHasChronicIllness(rs.getBoolean("has_chronic_illness"));
        beneficiary.setOrphan(rs.getBoolean("is_orphan"));
        beneficiary.setIndigenous(rs.getBoolean("is_indigenous"));
        beneficiary.setHomeless(rs.getBoolean("is_homeless"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            beneficiary.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return beneficiary;
    }
}