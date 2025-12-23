-- AIDSYNC 2.0 Database Schema
-- Complete database structure for Aid Distribution Management System

-- Create database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'aidsync')
BEGIN
    CREATE DATABASE aidsync;
END;
GO

USE aidsync;
GO

-- Users and Authentication
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN', 'LGU_ADMIN', 'BARANGAY_CAPTAIN', 'DISTRIBUTION_STAFF', 'VIEWER')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    failed_login_attempts INT DEFAULT 0,
    last_login DATETIME NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_status ON users(status);

-- Geographic Data
CREATE TABLE barangays (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    population INT DEFAULT 0,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    created_at DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_code ON barangays(code);

CREATE TABLE puroks (
    id INT IDENTITY(1,1) PRIMARY KEY,
    barangay_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    population INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (barangay_id) REFERENCES barangays(id),
    UNIQUE (barangay_id, code)
);

CREATE INDEX idx_barangay ON puroks(barangay_id);

-- Beneficiary Management
CREATE TABLE beneficiaries (
    id INT PRIMARY KEY AUTO_INCREMENT,
    beneficiary_id VARCHAR(10) UNIQUE NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    birth_date DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'PREFER_NOT_TO_SAY') NOT NULL,
    civil_status ENUM('SINGLE', 'MARRIED', 'WIDOWED', 'SEPARATED', 'DIVORCED') NOT NULL,
    contact_number VARCHAR(20),
    email VARCHAR(100),
    barangay_id INT NOT NULL,
    purok_id INT NOT NULL,
    street_sitio VARCHAR(200),
    is_household_head BOOLEAN DEFAULT FALSE,
    household_id VARCHAR(20),
    family_size INT DEFAULT 1,
    monthly_income_range ENUM('BELOW_5000', '5000_10000', '10000_15000', '15000_25000', 'ABOVE_25000'),
    employment_status ENUM('EMPLOYED', 'UNEMPLOYED', 'SELF_EMPLOYED', 'STUDENT', 'RETIRED'),
    priority_level INT DEFAULT 3,
    status ENUM('ACTIVE', 'INACTIVE', 'TRANSFERRED', 'DECEASED') DEFAULT 'ACTIVE',
    photo_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by INT,
    FOREIGN KEY (barangay_id) REFERENCES barangays(id),
    FOREIGN KEY (purok_id) REFERENCES puroks(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_beneficiary_id (beneficiary_id),
    INDEX idx_full_name (full_name),
    INDEX idx_barangay (barangay_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority_level)
);

-- Vulnerability Assessment
CREATE TABLE beneficiary_vulnerabilities (
    id INT PRIMARY KEY AUTO_INCREMENT,
    beneficiary_id INT NOT NULL,
    is_pwd BOOLEAN DEFAULT FALSE,
    is_senior_citizen BOOLEAN DEFAULT FALSE,
    is_pregnant_lactating BOOLEAN DEFAULT FALSE,
    is_solo_parent BOOLEAN DEFAULT FALSE,
    has_chronic_illness BOOLEAN DEFAULT FALSE,
    is_orphan BOOLEAN DEFAULT FALSE,
    is_indigenous BOOLEAN DEFAULT FALSE,
    is_homeless BOOLEAN DEFAULT FALSE,
    disability_type VARCHAR(100),
    medical_conditions TEXT,
    special_needs TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id) ON DELETE CASCADE,
    INDEX idx_beneficiary (beneficiary_id)
);

-- Inventory Management
CREATE TABLE inventory_categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    supplier_type ENUM('GOVERNMENT', 'NGO', 'PRIVATE', 'INDIVIDUAL') DEFAULT 'PRIVATE',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_code VARCHAR(50) UNIQUE NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    category_id INT NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    current_stock DECIMAL(10, 2) DEFAULT 0,
    minimum_stock_level DECIMAL(10, 2) DEFAULT 0,
    maximum_stock_level DECIMAL(10, 2) DEFAULT 0,
    unit_cost DECIMAL(10, 2) DEFAULT 0,
    supplier_id INT,
    batch_number VARCHAR(50),
    manufacture_date DATE,
    expiration_date DATE,
    storage_location VARCHAR(100),
    storage_conditions TEXT,
    photo_path VARCHAR(500),
    barcode VARCHAR(100),
    status ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES inventory_categories(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    INDEX idx_item_code (item_code),
    INDEX idx_category (category_id),
    INDEX idx_stock_level (current_stock),
    INDEX idx_expiration (expiration_date)
);

CREATE TABLE stock_movements (
    id INT PRIMARY KEY AUTO_INCREMENT,
    item_id INT NOT NULL,
    movement_type ENUM('IN', 'OUT', 'ADJUSTMENT', 'TRANSFER', 'WRITEOFF') NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_cost DECIMAL(10, 2),
    reference_number VARCHAR(50),
    reason VARCHAR(200),
    performed_by INT NOT NULL,
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES inventory_items(id),
    FOREIGN KEY (performed_by) REFERENCES users(id),
    INDEX idx_item (item_id),
    INDEX idx_date (movement_date),
    INDEX idx_type (movement_type)
);

-- Distribution Management
CREATE TABLE distribution_events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(200) NOT NULL,
    event_type ENUM('REGULAR', 'EMERGENCY', 'SCHEDULED') NOT NULL,
    description TEXT,
    planned_date DATE NOT NULL,
    actual_start_date DATETIME,
    actual_end_date DATETIME,
    venue VARCHAR(200),
    target_beneficiaries INT DEFAULT 0,
    actual_beneficiaries INT DEFAULT 0,
    status ENUM('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PLANNED',
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_date (planned_date),
    INDEX idx_status (status)
);

CREATE TABLE distributions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    distribution_event_id INT,
    beneficiary_id INT NOT NULL,
    distribution_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    distributed_by INT NOT NULL,
    receipt_number VARCHAR(50),
    total_value DECIMAL(10, 2) DEFAULT 0,
    beneficiary_signature LONGBLOB,
    photo_path VARCHAR(500),
    notes TEXT,
    status ENUM('COMPLETED', 'PENDING', 'CANCELLED') DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (distribution_event_id) REFERENCES distribution_events(id),
    FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id),
    FOREIGN KEY (distributed_by) REFERENCES users(id),
    INDEX idx_event (distribution_event_id),
    INDEX idx_beneficiary (beneficiary_id),
    INDEX idx_date (distribution_date)
);

CREATE TABLE distribution_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    distribution_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_cost DECIMAL(10, 2),
    total_cost DECIMAL(10, 2),
    FOREIGN KEY (distribution_id) REFERENCES distributions(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(id),
    INDEX idx_distribution (distribution_id),
    INDEX idx_item (item_id)
);

-- Reporting and Analytics
CREATE TABLE reports (
    id INT PRIMARY KEY AUTO_INCREMENT,
    report_name VARCHAR(200) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    parameters JSON,
    file_path VARCHAR(500),
    generated_by INT NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (generated_by) REFERENCES users(id),
    INDEX idx_type (report_type),
    INDEX idx_date (generated_at)
);

-- System Audit Trail
CREATE TABLE audit_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(50),
    record_id INT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_date (created_at)
);

-- System Configuration
CREATE TABLE system_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    description TEXT,
    updated_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- Insert Default Data
INSERT INTO users (username, password_hash, full_name, role) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1VdLSnqpRt1lO6F.U8EqujSJmf6kway', 'System Administrator', 'SUPER_ADMIN');

-- Insert Barangays for Mati City
INSERT INTO barangays (name, code) VALUES 
('Badas', 'BRG001'), ('Bato', 'BRG002'), ('Bohewan', 'BRG003'), ('Cabuaya', 'BRG004'),
('Central', 'BRG005'), ('Culian', 'BRG006'), ('Dahican', 'BRG007'), ('Dawan', 'BRG008'),
('Don Enrique Lopez', 'BRG009'), ('Don Martin Marundan', 'BRG010'), ('Don Salvador Lopez Sr.', 'BRG011'),
('Langka', 'BRG012'), ('Lawigan', 'BRG013'), ('Libudon', 'BRG014'), ('Luban', 'BRG015'),
('Macambol', 'BRG016'), ('Mamali', 'BRG017'), ('Matiao', 'BRG018'), ('Mayo', 'BRG019'),
('Sainz', 'BRG020'), ('Tagabakid', 'BRG021'), ('Tagbinonga', 'BRG022'), ('Taguibo', 'BRG023'),
('Tamisan', 'BRG024'), ('Tarragona', 'BRG025'), ('Wasi', 'BRG026');

-- Insert Default Inventory Categories
INSERT INTO inventory_categories (name, code, description) VALUES 
('Food & Nutrition', 'FOOD', 'Food items and nutritional supplements'),
('Hygiene & Sanitation', 'HYGIENE', 'Personal hygiene and sanitation items'),
('Medical & Health', 'MEDICAL', 'Medical supplies and health-related items'),
('Clothing & Bedding', 'CLOTHING', 'Clothing, blankets, and bedding materials'),
('Shelter Materials', 'SHELTER', 'Construction and shelter materials'),
('Cash Assistance', 'CASH', 'Monetary assistance and vouchers'),
('Livelihood Support', 'LIVELIHOOD', 'Tools and materials for livelihood'),
('Educational Materials', 'EDUCATION', 'School supplies and educational materials'),
('Specialized Kits', 'SPECIAL', 'PWD, infant, elderly specialized kits');

-- Insert Default System Settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES 
('organization_name', 'Mati City LGU', 'Organization name'),
('organization_address', 'Mati City, Davao Oriental', 'Organization address'),
('session_timeout_minutes', '30', 'Session timeout in minutes'),
('max_failed_login_attempts', '3', 'Maximum failed login attempts before lock'),
('backup_retention_days', '30', 'Number of days to retain backup files'),
('default_distribution_limit', '1', 'Default distributions per beneficiary per month');

-- Create Views for Common Queries
CREATE VIEW beneficiary_summary AS
SELECT 
    b.id,
    b.beneficiary_id,
    b.full_name,
    b.birth_date,
    YEAR(CURDATE()) - YEAR(b.birth_date) - (DATE_FORMAT(CURDATE(), '%m%d') < DATE_FORMAT(b.birth_date, '%m%d')) AS age,
    b.gender,
    b.contact_number,
    br.name AS barangay_name,
    p.name AS purok_name,
    b.family_size,
    b.priority_level,
    b.status,
    bv.is_pwd,
    bv.is_senior_citizen,
    bv.is_pregnant_lactating,
    b.created_at
FROM beneficiaries b
LEFT JOIN barangays br ON b.barangay_id = br.id
LEFT JOIN puroks p ON b.purok_id = p.id
LEFT JOIN beneficiary_vulnerabilities bv ON b.id = bv.beneficiary_id;

CREATE VIEW inventory_summary AS
SELECT 
    i.id,
    i.item_code,
    i.item_name,
    c.name AS category_name,
    i.unit_of_measure,
    i.current_stock,
    i.minimum_stock_level,
    i.maximum_stock_level,
    i.unit_cost,
    i.current_stock * i.unit_cost AS total_value,
    CASE 
        WHEN i.current_stock <= i.minimum_stock_level THEN 'LOW'
        WHEN i.current_stock >= i.maximum_stock_level THEN 'HIGH'
        ELSE 'NORMAL'
    END AS stock_status,
    i.expiration_date,
    DATEDIFF(i.expiration_date, CURDATE()) AS days_to_expiry,
    s.name AS supplier_name
FROM inventory_items i
LEFT JOIN inventory_categories c ON i.category_id = c.id
LEFT JOIN suppliers s ON i.supplier_id = s.id
WHERE i.status = 'ACTIVE';

-- Create Indexes for Performance
CREATE INDEX idx_beneficiaries_search ON beneficiaries(full_name, beneficiary_id, contact_number);
CREATE INDEX idx_distributions_date_range ON distributions(distribution_date, status);
CREATE INDEX idx_inventory_alerts ON inventory_items(current_stock, minimum_stock_level, expiration_date);
CREATE INDEX idx_audit_user_date ON audit_logs(user_id, created_at);

-- Create Triggers for Audit Trail
DELIMITER //

CREATE TRIGGER beneficiary_audit_insert 
AFTER INSERT ON beneficiaries
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, new_values)
    VALUES (NEW.created_by, 'INSERT', 'beneficiaries', NEW.id, JSON_OBJECT(
        'beneficiary_id', NEW.beneficiary_id,
        'full_name', NEW.full_name,
        'barangay_id', NEW.barangay_id
    ));
END//

CREATE TRIGGER beneficiary_audit_update
AFTER UPDATE ON beneficiaries
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, old_values, new_values)
    VALUES (NEW.created_by, 'UPDATE', 'beneficiaries', NEW.id,
        JSON_OBJECT('full_name', OLD.full_name, 'status', OLD.status),
        JSON_OBJECT('full_name', NEW.full_name, 'status', NEW.status)
    );
END//

CREATE TRIGGER distribution_audit_insert
AFTER INSERT ON distributions
FOR EACH ROW
BEGIN
    INSERT INTO audit_logs (user_id, action, table_name, record_id, new_values)
    VALUES (NEW.distributed_by, 'INSERT', 'distributions', NEW.id, JSON_OBJECT(
        'beneficiary_id', NEW.beneficiary_id,
        'total_value', NEW.total_value,
        'distribution_date', NEW.distribution_date
    ));
END//

DELIMITER ;

-- Create Stored Procedures for Common Operations
DELIMITER //

CREATE PROCEDURE GetBeneficiaryStats()
BEGIN
    SELECT 
        COUNT(*) as total_beneficiaries,
        COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_beneficiaries,
        COUNT(CASE WHEN YEAR(CURDATE()) - YEAR(birth_date) >= 65 THEN 1 END) as senior_citizens,
        COUNT(CASE WHEN priority_level <= 2 THEN 1 END) as high_priority,
        AVG(family_size) as avg_family_size
    FROM beneficiaries;
END//

CREATE PROCEDURE GetDistributionStats(IN start_date DATE, IN end_date DATE)
BEGIN
    SELECT 
        COUNT(*) as total_distributions,
        COUNT(DISTINCT beneficiary_id) as unique_beneficiaries,
        SUM(total_value) as total_value_distributed,
        AVG(total_value) as avg_distribution_value
    FROM distributions 
    WHERE DATE(distribution_date) BETWEEN start_date AND end_date;
END//

CREATE PROCEDURE GetLowStockItems()
BEGIN
    SELECT 
        item_code,
        item_name,
        current_stock,
        minimum_stock_level,
        (minimum_stock_level - current_stock) as shortage_quantity
    FROM inventory_items 
    WHERE current_stock <= minimum_stock_level 
    AND status = 'ACTIVE'
    ORDER BY (current_stock / minimum_stock_level) ASC;
END//

DELIMITER ;