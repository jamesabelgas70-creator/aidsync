package com.aidsync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private static final String MYSQL_URL;
    private static final String SQLITE_URL;
    private static final String USERNAME;
    private static final String PASSWORD;
    
    static {
        try {
            MYSQL_URL = System.getProperty("db.mysql.url", System.getenv().getOrDefault("DB_MYSQL_URL", "jdbc:mysql://localhost:3306/aidsync"));
            SQLITE_URL = System.getProperty("db.sqlite.url", System.getenv().getOrDefault("DB_SQLITE_URL", "jdbc:sqlite:aidsync.db"));
            USERNAME = System.getProperty("db.username", System.getenv().getOrDefault("DB_USERNAME", "root"));
            PASSWORD = System.getProperty("db.password", System.getenv().getOrDefault("DB_PASSWORD", ""));
            
            if (MYSQL_URL == null || SQLITE_URL == null || USERNAME == null) {
                throw new IllegalStateException("Database configuration properties cannot be null");
            }
            
            // Log configuration (without sensitive data)
            logger.info("Database configuration loaded - MySQL URL: {}, SQLite URL: {}, Username: {}", 
                       MYSQL_URL.replaceAll("password=[^&]*", "password=***"), 
                       SQLITE_URL, 
                       USERNAME);
        } catch (SecurityException e) {
            logger.error("Security error accessing system properties", e);
            throw new ExceptionInInitializerError("Failed to initialize database configuration: " + e.getMessage());
        }
    }
    
    private static Connection connection;
    private static boolean useMysql = true;
    
    public static synchronized void initialize() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    return; // Already initialized
                }
            } catch (SQLException e) {
                logger.debug("Checking connection status failed, reinitializing", e);
            }
        }
        
        try {
            // Try MySQL first
            connection = connectToMySQL();
            useMysql = true;
            logger.info("Connected to MySQL database");
            createTables();
            return;
        } catch (SQLException e) {
            logger.warn("MySQL connection failed, falling back to SQLite: {}", e.getMessage());
        }
        
        try {
            // Fallback to SQLite
            connection = connectToSQLite();
            useMysql = false;
            logger.info("Connected to SQLite database");
            createTables();
        } catch (SQLException e) {
            logger.error("Failed to connect to any database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private static Connection connectToMySQL() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("serverTimezone", "Asia/Manila");
        
        return DriverManager.getConnection(MYSQL_URL, props);
    }
    
    private static Connection connectToSQLite() throws SQLException {
        return DriverManager.getConnection(SQLITE_URL);
    }
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initialize();
            }
            return connection;
        } catch (SQLException e) {
            logger.error("Error getting database connection", e);
            throw new RuntimeException("Database connection error", e);
        }
    }
    
    private static void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            String createUsersTable = useMysql ? 
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password_hash VARCHAR(255) NOT NULL," +
                "email VARCHAR(100)," +
                "full_name VARCHAR(100) NOT NULL," +
                "role VARCHAR(50) NOT NULL," +
                "status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "failed_login_attempts INT DEFAULT 0," +
                "last_login TIMESTAMP NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")" :
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password_hash VARCHAR(255) NOT NULL," +
                "email VARCHAR(100)," +
                "full_name VARCHAR(100) NOT NULL," +
                "role VARCHAR(50) NOT NULL," +
                "status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "failed_login_attempts INTEGER DEFAULT 0," +
                "last_login TIMESTAMP NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(createUsersTable);
            
            // Barangays table
            String createBarangaysTable = useMysql ?
                "CREATE TABLE IF NOT EXISTS barangays (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "code VARCHAR(20) UNIQUE NOT NULL," +
                "population INT DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")" :
                "CREATE TABLE IF NOT EXISTS barangays (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(100) NOT NULL," +
                "code VARCHAR(20) UNIQUE NOT NULL," +
                "population INTEGER DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(createBarangaysTable);
            
            // Puroks table
            String createPuroksTable = useMysql ?
                "CREATE TABLE IF NOT EXISTS puroks (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "barangay_id INT NOT NULL," +
                "name VARCHAR(100) NOT NULL," +
                "code VARCHAR(20) NOT NULL," +
                "FOREIGN KEY (barangay_id) REFERENCES barangays(id)," +
                "UNIQUE KEY unique_purok (barangay_id, code)" +
                ")" :
                "CREATE TABLE IF NOT EXISTS puroks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "barangay_id INTEGER NOT NULL," +
                "name VARCHAR(100) NOT NULL," +
                "code VARCHAR(20) NOT NULL," +
                "FOREIGN KEY (barangay_id) REFERENCES barangays(id)," +
                "UNIQUE (barangay_id, code)" +
                ")";
            stmt.execute(createPuroksTable);
            
            // Beneficiaries table
            String createBeneficiariesTable = useMysql ?
                "CREATE TABLE IF NOT EXISTS beneficiaries (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "beneficiary_id VARCHAR(10) UNIQUE NOT NULL," +
                "full_name VARCHAR(150) NOT NULL," +
                "birth_date DATE NOT NULL," +
                "gender VARCHAR(20) NOT NULL," +
                "civil_status VARCHAR(20) NOT NULL," +
                "contact_number VARCHAR(20)," +
                "email VARCHAR(100)," +
                "barangay_id INT NOT NULL," +
                "purok_id INT NOT NULL," +
                "street_sitio VARCHAR(200)," +
                "is_household_head BOOLEAN DEFAULT FALSE," +
                "family_size INT DEFAULT 1," +
                "monthly_income_range VARCHAR(20)," +
                "employment_status VARCHAR(20)," +
                "priority_level INT DEFAULT 3," +
                "status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "photo_path VARCHAR(500)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "created_by INT," +
                "FOREIGN KEY (barangay_id) REFERENCES barangays(id)," +
                "FOREIGN KEY (purok_id) REFERENCES puroks(id)" +
                ")" :
                "CREATE TABLE IF NOT EXISTS beneficiaries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "beneficiary_id VARCHAR(10) UNIQUE NOT NULL," +
                "full_name VARCHAR(150) NOT NULL," +
                "birth_date DATE NOT NULL," +
                "gender VARCHAR(20) NOT NULL," +
                "civil_status VARCHAR(20) NOT NULL," +
                "contact_number VARCHAR(20)," +
                "email VARCHAR(100)," +
                "barangay_id INTEGER NOT NULL," +
                "purok_id INTEGER NOT NULL," +
                "street_sitio VARCHAR(200)," +
                "is_household_head INTEGER DEFAULT 0," +
                "family_size INTEGER DEFAULT 1," +
                "monthly_income_range VARCHAR(20)," +
                "employment_status VARCHAR(20)," +
                "priority_level INTEGER DEFAULT 3," +
                "status VARCHAR(20) DEFAULT 'ACTIVE'," +
                "photo_path VARCHAR(500)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "created_by INTEGER," +
                "FOREIGN KEY (barangay_id) REFERENCES barangays(id)," +
                "FOREIGN KEY (purok_id) REFERENCES puroks(id)" +
                ")";
            stmt.execute(createBeneficiariesTable);
            
            // Beneficiary vulnerabilities table
            String createVulnerabilitiesTable = useMysql ?
                "CREATE TABLE IF NOT EXISTS beneficiary_vulnerabilities (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "beneficiary_id INT NOT NULL," +
                "is_pwd BOOLEAN DEFAULT FALSE," +
                "is_senior_citizen BOOLEAN DEFAULT FALSE," +
                "is_pregnant_lactating BOOLEAN DEFAULT FALSE," +
                "is_solo_parent BOOLEAN DEFAULT FALSE," +
                "has_chronic_illness BOOLEAN DEFAULT FALSE," +
                "is_orphan BOOLEAN DEFAULT FALSE," +
                "is_indigenous BOOLEAN DEFAULT FALSE," +
                "is_homeless BOOLEAN DEFAULT FALSE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id) ON DELETE CASCADE" +
                ")" :
                "CREATE TABLE IF NOT EXISTS beneficiary_vulnerabilities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "beneficiary_id INTEGER NOT NULL," +
                "is_pwd INTEGER DEFAULT 0," +
                "is_senior_citizen INTEGER DEFAULT 0," +
                "is_pregnant_lactating INTEGER DEFAULT 0," +
                "is_solo_parent INTEGER DEFAULT 0," +
                "has_chronic_illness INTEGER DEFAULT 0," +
                "is_orphan INTEGER DEFAULT 0," +
                "is_indigenous INTEGER DEFAULT 0," +
                "is_homeless INTEGER DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id) ON DELETE CASCADE" +
                ")";
            stmt.execute(createVulnerabilitiesTable);
            
            // Insert default admin user with simple password (temporary)
            String passwordHash = "password"; // Temporary simple password
            
            String insertAdmin = useMysql ?
                "INSERT IGNORE INTO users (username, password_hash, full_name, role) " +
                "VALUES ('admin', '" + passwordHash + "', 'System Administrator', 'SUPER_ADMIN')" :
                "INSERT OR IGNORE INTO users (username, password_hash, full_name, role) " +
                "VALUES ('admin', '" + passwordHash + "', 'System Administrator', 'SUPER_ADMIN')";
            
            stmt.execute(insertAdmin);
            
            // Create inventory tables
            createInventoryTables(stmt);
            
            // Create distribution tables
            createDistributionTables(stmt);
            
            // Insert default barangays for Mati City
            insertDefaultBarangays(stmt);
            
            logger.info("Database tables created successfully");
            
        } catch (SQLException e) {
            logger.error("Error creating database tables", e);
            throw new RuntimeException("Database table creation failed", e);
        }
    }
    
    private static void insertDefaultBarangays(Statement stmt) throws SQLException {
        String[] barangays = {
            "Badas", "Bato", "Bohewan", "Cabuaya", "Central", "Culian", "Dahican", 
            "Dawan", "Don Enrique Lopez", "Don Martin Marundan", "Don Salvador Lopez Sr.", 
            "Langka", "Lawigan", "Libudon", "Luban", "Macambol", "Mamali", "Matiao", 
            "Mayo", "Sainz", "Tagabakid", "Tagbinonga", "Taguibo", "Tamisan", "Tarragona", "Wasi"
        };
        
        String insertBarangaySQL = useMysql ? "INSERT IGNORE INTO" : "INSERT OR IGNORE INTO";
        for (int i = 0; i < barangays.length; i++) {
            try {
                stmt.execute(String.format(
                    "%s barangays (name, code) VALUES ('%s', 'BRG%03d')",
                    insertBarangaySQL, barangays[i], i + 1
                ));
            } catch (SQLException e) {
                logger.warn("Failed to insert barangay: {}", barangays[i], e);
            }
        }
    }
    
    public static void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }
    
    private static void createInventoryTables(Statement stmt) throws SQLException {
        // Inventory categories
        String createCategoriesTable = useMysql ?
            "CREATE TABLE IF NOT EXISTS inventory_categories (" +
            "id INT PRIMARY KEY AUTO_INCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "code VARCHAR(20) UNIQUE NOT NULL," +
            "description TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")" :
            "CREATE TABLE IF NOT EXISTS inventory_categories (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(100) NOT NULL," +
            "code VARCHAR(20) UNIQUE NOT NULL," +
            "description TEXT," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
        stmt.execute(createCategoriesTable);
        
        // Suppliers
        String createSuppliersTable = useMysql ?
            "CREATE TABLE IF NOT EXISTS suppliers (" +
            "id INT PRIMARY KEY AUTO_INCREMENT," +
            "name VARCHAR(150) NOT NULL," +
            "contact_person VARCHAR(100)," +
            "contact_number VARCHAR(20)," +
            "email VARCHAR(100)," +
            "address TEXT," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")" :
            "CREATE TABLE IF NOT EXISTS suppliers (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name VARCHAR(150) NOT NULL," +
            "contact_person VARCHAR(100)," +
            "contact_number VARCHAR(20)," +
            "email VARCHAR(100)," +
            "address TEXT," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
        stmt.execute(createSuppliersTable);
        
        // Inventory items
        String createItemsTable = useMysql ?
            "CREATE TABLE IF NOT EXISTS inventory_items (" +
            "id INT PRIMARY KEY AUTO_INCREMENT," +
            "item_code VARCHAR(50) UNIQUE NOT NULL," +
            "item_name VARCHAR(200) NOT NULL," +
            "category_id INT NOT NULL," +
            "unit_of_measure VARCHAR(20) NOT NULL," +
            "current_stock DECIMAL(10,2) DEFAULT 0," +
            "minimum_stock_level DECIMAL(10,2) DEFAULT 0," +
            "maximum_stock_level DECIMAL(10,2) DEFAULT 0," +
            "unit_cost DECIMAL(10,2) DEFAULT 0," +
            "supplier_id INT," +
            "batch_number VARCHAR(50)," +
            "manufacture_date DATE," +
            "expiration_date DATE," +
            "storage_location VARCHAR(100)," +
            "storage_conditions TEXT," +
            "photo_path VARCHAR(500)," +
            "barcode VARCHAR(100)," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "FOREIGN KEY (category_id) REFERENCES inventory_categories(id)," +
            "FOREIGN KEY (supplier_id) REFERENCES suppliers(id)" +
            ")" :
            "CREATE TABLE IF NOT EXISTS inventory_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "item_code VARCHAR(50) UNIQUE NOT NULL," +
            "item_name VARCHAR(200) NOT NULL," +
            "category_id INTEGER NOT NULL," +
            "unit_of_measure VARCHAR(20) NOT NULL," +
            "current_stock REAL DEFAULT 0," +
            "minimum_stock_level REAL DEFAULT 0," +
            "maximum_stock_level REAL DEFAULT 0," +
            "unit_cost REAL DEFAULT 0," +
            "supplier_id INTEGER," +
            "batch_number VARCHAR(50)," +
            "manufacture_date DATE," +
            "expiration_date DATE," +
            "storage_location VARCHAR(100)," +
            "storage_conditions TEXT," +
            "photo_path VARCHAR(500)," +
            "barcode VARCHAR(100)," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (category_id) REFERENCES inventory_categories(id)," +
            "FOREIGN KEY (supplier_id) REFERENCES suppliers(id)" +
            ")";
        stmt.execute(createItemsTable);
        
        // Stock movements
        String createMovementsTable = useMysql ?
            "CREATE TABLE IF NOT EXISTS stock_movements (" +
            "id INT PRIMARY KEY AUTO_INCREMENT," +
            "item_id INT NOT NULL," +
            "movement_type VARCHAR(20) NOT NULL," +
            "quantity DECIMAL(10,2) NOT NULL," +
            "unit_cost DECIMAL(10,2)," +
            "reference_number VARCHAR(50)," +
            "reason VARCHAR(200)," +
            "performed_by INT NOT NULL," +
            "movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (item_id) REFERENCES inventory_items(id)," +
            "FOREIGN KEY (performed_by) REFERENCES users(id)" +
            ")" :
            "CREATE TABLE IF NOT EXISTS stock_movements (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "item_id INTEGER NOT NULL," +
            "movement_type VARCHAR(20) NOT NULL," +
            "quantity REAL NOT NULL," +
            "unit_cost REAL," +
            "reference_number VARCHAR(50)," +
            "reason VARCHAR(200)," +
            "performed_by INTEGER NOT NULL," +
            "movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (item_id) REFERENCES inventory_items(id)," +
            "FOREIGN KEY (performed_by) REFERENCES users(id)" +
            ")";
        stmt.execute(createMovementsTable);
        
        // Insert default categories
        String insertCategorySQL = useMysql ? "INSERT IGNORE INTO" : "INSERT OR IGNORE INTO";
        String[] categories = {
            "Food & Nutrition,FOOD,Food items and nutritional supplements",
            "Hygiene & Sanitation,HYGN,Personal hygiene and sanitation items",
            "Medical & Health,MEDL,Medical supplies and health-related items",
            "Clothing & Bedding,CLTH,Clothing blankets and bedding materials",
            "Shelter Materials,SHLT,Construction and shelter materials"
        };
        
        for (String category : categories) {
            String[] parts = category.split(",");
            stmt.execute(String.format(
                "%s inventory_categories (name, code, description) VALUES ('%s', '%s', '%s')",
                insertCategorySQL, parts[0], parts[1], parts[2]
            ));
        }
    }
    
    private static void createDistributionTables(Statement stmt) throws SQLException {
        // Distribution events
        String createEventsTable = useMysql ?
            "CREATE TABLE IF NOT EXISTS distribution_events (" +
            "id INT PRIMARY KEY AUTO_INCREMENT," +
            "event_name VARCHAR(200) NOT NULL," +
            "event_type VARCHAR(20) NOT NULL," +
            "description TEXT," +
            "planned_date DATE NOT NULL," +
            "status VARCHAR(20) DEFAULT 'PLANNED'," +
            "created_by INT NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (created_by) REFERENCES users(id)" +
            ")" :
            "CREATE TABLE IF NOT EXISTS distribution_events (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "event_name VARCHAR(200) NOT NULL," +
            "event_type VARCHAR(20) NOT NULL," +
            "description TEXT," +
            "planned_date DATE NOT NULL," +
            "status VARCHAR(20) DEFAULT 'PLANNED'," +
            "created_by INTEGER NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (created_by) REFERENCES users(id)" +
            ")";
        stmt.execute(createEventsTable);
        
        // Distributions
        String createDistributionsTable = useMysql ?
            "CREATE TABLE IF NOT EXISTS distributions (" +
            "id INT PRIMARY KEY AUTO_INCREMENT," +
            "distribution_event_id INT," +
            "beneficiary_id INT NOT NULL," +
            "distribution_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "distributed_by INT NOT NULL," +
            "total_value DECIMAL(10,2) DEFAULT 0," +
            "status VARCHAR(20) DEFAULT 'COMPLETED'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (distribution_event_id) REFERENCES distribution_events(id)," +
            "FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id)," +
            "FOREIGN KEY (distributed_by) REFERENCES users(id)" +
            ")" :
            "CREATE TABLE IF NOT EXISTS distributions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "distribution_event_id INTEGER," +
            "beneficiary_id INTEGER NOT NULL," +
            "distribution_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "distributed_by INTEGER NOT NULL," +
            "total_value REAL DEFAULT 0," +
            "status VARCHAR(20) DEFAULT 'COMPLETED'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (distribution_event_id) REFERENCES distribution_events(id)," +
            "FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id)," +
            "FOREIGN KEY (distributed_by) REFERENCES users(id)" +
            ")";
        stmt.execute(createDistributionsTable);
    }
    
    public static boolean isUsingMySQL() {
        return useMysql;
    }
}