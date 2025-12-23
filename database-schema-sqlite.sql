-- AIDSYNC 2.0 Database Schema (SQLite Compatible)
-- Complete database structure for Aid Distribution Management System

-- Users and Authentication
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    failed_login_attempts INTEGER DEFAULT 0,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Geographic Data
CREATE TABLE IF NOT EXISTS barangays (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    population INTEGER DEFAULT 0,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS puroks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    barangay_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    population INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (barangay_id) REFERENCES barangays(id),
    UNIQUE (barangay_id, code)
);

-- Beneficiary Management
CREATE TABLE IF NOT EXISTS beneficiaries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    beneficiary_id VARCHAR(10) UNIQUE NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    civil_status VARCHAR(20) NOT NULL,
    contact_number VARCHAR(20),
    email VARCHAR(100),
    barangay_id INTEGER NOT NULL,
    purok_id INTEGER NOT NULL,
    street_sitio VARCHAR(200),
    is_household_head INTEGER DEFAULT 0,
    household_id VARCHAR(20),
    family_size INTEGER DEFAULT 1,
    monthly_income_range VARCHAR(20),
    employment_status VARCHAR(20),
    priority_level INTEGER DEFAULT 3,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    photo_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    FOREIGN KEY (barangay_id) REFERENCES barangays(id),
    FOREIGN KEY (purok_id) REFERENCES puroks(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Vulnerability Assessment
CREATE TABLE IF NOT EXISTS beneficiary_vulnerabilities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    beneficiary_id INTEGER NOT NULL,
    is_pwd INTEGER DEFAULT 0,
    is_senior_citizen INTEGER DEFAULT 0,
    is_pregnant_lactating INTEGER DEFAULT 0,
    is_solo_parent INTEGER DEFAULT 0,
    has_chronic_illness INTEGER DEFAULT 0,
    is_orphan INTEGER DEFAULT 0,
    is_indigenous INTEGER DEFAULT 0,
    is_homeless INTEGER DEFAULT 0,
    disability_type VARCHAR(100),
    medical_conditions TEXT,
    special_needs TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id) ON DELETE CASCADE
);

-- Inventory Management
CREATE TABLE IF NOT EXISTS inventory_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS suppliers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    supplier_type VARCHAR(20) DEFAULT 'PRIVATE',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_code VARCHAR(50) UNIQUE NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    category_id INTEGER NOT NULL,
    unit_of_measure VARCHAR(20) NOT NULL,
    current_stock DECIMAL(10, 2) DEFAULT 0,
    minimum_stock_level DECIMAL(10, 2) DEFAULT 0,
    maximum_stock_level DECIMAL(10, 2) DEFAULT 0,
    unit_cost DECIMAL(10, 2) DEFAULT 0,
    supplier_id INTEGER,
    batch_number VARCHAR(50),
    manufacture_date DATE,
    expiration_date DATE,
    storage_location VARCHAR(100),
    storage_conditions TEXT,
    photo_path VARCHAR(500),
    barcode VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES inventory_categories(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    item_id INTEGER NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_cost DECIMAL(10, 2),
    reference_number VARCHAR(50),
    reason VARCHAR(200),
    performed_by INTEGER NOT NULL,
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES inventory_items(id),
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- Insert Default Admin User
INSERT OR IGNORE INTO users (username, password_hash, full_name, role) VALUES 
('admin', 'password', 'System Administrator', 'SUPER_ADMIN');

-- Insert Barangays for Mati City
INSERT OR IGNORE INTO barangays (name, code) VALUES 
('Badas', 'BRG001'), ('Bato', 'BRG002'), ('Bohewan', 'BRG003'), ('Cabuaya', 'BRG004'),
('Central', 'BRG005'), ('Culian', 'BRG006'), ('Dahican', 'BRG007'), ('Dawan', 'BRG008'),
('Don Enrique Lopez', 'BRG009'), ('Don Martin Marundan', 'BRG010'), ('Don Salvador Lopez Sr.', 'BRG011'),
('Langka', 'BRG012'), ('Lawigan', 'BRG013'), ('Libudon', 'BRG014'), ('Luban', 'BRG015'),
('Macambol', 'BRG016'), ('Mamali', 'BRG017'), ('Matiao', 'BRG018'), ('Mayo', 'BRG019'),
('Sainz', 'BRG020'), ('Tagabakid', 'BRG021'), ('Tagbinonga', 'BRG022'), ('Taguibo', 'BRG023'),
('Tamisan', 'BRG024'), ('Tarragona', 'BRG025'), ('Wasi', 'BRG026');

-- Insert Default Inventory Categories
INSERT OR IGNORE INTO inventory_categories (name, code, description) VALUES 
('Food & Nutrition', 'FOOD', 'Food items and nutritional supplements'),
('Hygiene & Sanitation', 'HYGN', 'Personal hygiene and sanitation items'),
('Medical & Health', 'MEDL', 'Medical supplies and health-related items'),
('Clothing & Bedding', 'CLTH', 'Clothing, blankets, and bedding materials'),
('Shelter Materials', 'SHLT', 'Construction and shelter materials'),
('Cash Assistance', 'CASH', 'Monetary assistance and vouchers'),
('Livelihood Support', 'LIVE', 'Tools and materials for livelihood'),
('Educational Materials', 'EDUC', 'School supplies and educational materials'),
('Specialized Kits', 'SPEC', 'PWD, infant, elderly specialized kits');