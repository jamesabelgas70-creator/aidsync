-- AIDSYNC 2.0 Database Schema (SQL Server Compatible)
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
    id INT IDENTITY(1,1) PRIMARY KEY,
    beneficiary_id VARCHAR(10) UNIQUE NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'PREFER_NOT_TO_SAY')),
    civil_status VARCHAR(20) NOT NULL CHECK (civil_status IN ('SINGLE', 'MARRIED', 'WIDOWED', 'SEPARATED', 'DIVORCED')),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    barangay_id INT NOT NULL,
    purok_id INT NOT NULL,
    street_sitio VARCHAR(200),
    is_household_head BIT DEFAULT 0,
    household_id VARCHAR(20),
    family_size INT DEFAULT 1,
    monthly_income_range VARCHAR(20),
    employment_status VARCHAR(20),
    priority_level INT DEFAULT 3,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    photo_path VARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    created_by INT,
    FOREIGN KEY (barangay_id) REFERENCES barangays(id),
    FOREIGN KEY (purok_id) REFERENCES puroks(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Insert Default Admin User
INSERT INTO users (username, password_hash, full_name, role) VALUES 
('admin', 'password', 'System Administrator', 'SUPER_ADMIN');

-- Insert Barangays for Mati City
INSERT INTO barangays (name, code) VALUES 
('Badas', 'BRG001'), ('Bato', 'BRG002'), ('Bohewan', 'BRG003'), ('Cabuaya', 'BRG004'),
('Central', 'BRG005'), ('Culian', 'BRG006'), ('Dahican', 'BRG007'), ('Dawan', 'BRG008'),
('Don Enrique Lopez', 'BRG009'), ('Don Martin Marundan', 'BRG010'), ('Don Salvador Lopez Sr.', 'BRG011'),
('Langka', 'BRG012'), ('Lawigan', 'BRG013'), ('Libudon', 'BRG014'), ('Luban', 'BRG015'),
('Macambol', 'BRG016'), ('Mamali', 'BRG017'), ('Matiao', 'BRG018'), ('Mayo', 'BRG019'),
('Sainz', 'BRG020'), ('Tagabakid', 'BRG021'), ('Tagbinonga', 'BRG022'), ('Taguibo', 'BRG023'),
('Tamisan', 'BRG024'), ('Tarragona', 'BRG025'), ('Wasi', 'BRG026');