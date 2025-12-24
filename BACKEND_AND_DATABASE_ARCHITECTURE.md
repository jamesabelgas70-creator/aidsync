# AIDSYNC Backend & Database Architecture

## 📊 Overview

AIDSYNC uses a **3-tier architecture** with:
- **Presentation Layer**: JavaFX Controllers (FXML-based UI)
- **Business Logic Layer**: Service classes
- **Data Access Layer**: Direct JDBC with DatabaseConfig

## 🗄️ Database Architecture

### Database Support

**Dual Database Support:**
- **Primary**: MySQL/MariaDB (for production)
- **Fallback**: SQLite (for development/standalone)

**Auto-Detection:**
- Tries MySQL first
- Falls back to SQLite if MySQL unavailable
- Automatically creates tables on first run

### Database Configuration

**Location**: `src/main/java/com/aidsync/config/DatabaseConfig.java`

**Connection Strategy:**
```java
1. Try MySQL connection (jdbc:mysql://localhost:3306/aidsync)
2. If fails → Try SQLite (jdbc:sqlite:aidsync.db)
3. Auto-create all tables if they don't exist
4. Insert default data (barangays, categories, admin user)
```

**Configuration Sources** (in priority order):
1. System properties (`-Ddb.mysql.url=...`)
2. Environment variables (`DB_MYSQL_URL`, `DB_USERNAME`, etc.)
3. Default values (localhost MySQL, SQLite fallback)

### Database Schema

#### Core Tables

**1. Users & Authentication**
```sql
users
├── id (PK)
├── username (UNIQUE)
├── password_hash (BCrypt)
├── email
├── full_name
├── role (SUPER_ADMIN, LGU_ADMIN, BARANGAY_CAPTAIN, DISTRIBUTION_STAFF, VIEWER)
├── status (ACTIVE, INACTIVE, LOCKED)
├── failed_login_attempts
├── last_login
├── created_at
└── updated_at
```

**2. Geographic Data**
```sql
barangays
├── id (PK)
├── name
├── code (UNIQUE)
└── population

puroks
├── id (PK)
├── barangay_id (FK → barangays)
├── name
└── code
```

**3. Beneficiary Management**
```sql
beneficiaries
├── id (PK)
├── beneficiary_id (UNIQUE, e.g., "BEN00001")
├── full_name
├── birth_date
├── gender (MALE, FEMALE, PREFER_NOT_TO_SAY)
├── civil_status (SINGLE, MARRIED, WIDOWED, etc.)
├── contact_number
├── email
├── barangay_id (FK → barangays)
├── purok_id (FK → puroks)
├── street_sitio
├── is_household_head
├── family_size
├── monthly_income_range
├── employment_status
├── priority_level (1-5)
├── status (ACTIVE, INACTIVE, TRANSFERRED, DECEASED)
├── photo_path
├── created_by (FK → users)
├── created_at
└── updated_at

beneficiary_vulnerabilities
├── id (PK)
├── beneficiary_id (FK → beneficiaries, CASCADE DELETE)
├── is_pwd (Person with Disability)
├── is_senior_citizen
├── is_pregnant_lactating
├── is_solo_parent
├── has_chronic_illness
├── is_orphan
├── is_indigenous
└── is_homeless
```

**4. Inventory Management**
```sql
inventory_categories
├── id (PK)
├── name
├── code (UNIQUE)
└── description

suppliers
├── id (PK)
├── name
├── contact_person
├── contact_number
├── email
├── address
└── status

inventory_items
├── id (PK)
├── item_code (UNIQUE, e.g., "FOOD-001")
├── item_name
├── category_id (FK → inventory_categories)
├── unit_of_measure
├── current_stock (DECIMAL)
├── minimum_stock_level
├── maximum_stock_level
├── unit_cost
├── supplier_id (FK → suppliers)
├── batch_number
├── manufacture_date
├── expiration_date
├── storage_location
├── storage_conditions
├── photo_path
├── barcode
├── status (ACTIVE, INACTIVE, DISCONTINUED)
├── created_at
└── updated_at

stock_movements
├── id (PK)
├── item_id (FK → inventory_items)
├── movement_type (IN, OUT, ADJUSTMENT, TRANSFER, WRITEOFF)
├── quantity
├── unit_cost
├── reference_number
├── reason
├── performed_by (FK → users)
└── movement_date
```

**5. Distribution Management**
```sql
distribution_events
├── id (PK)
├── event_name
├── event_type (REGULAR, EMERGENCY, SCHEDULED)
├── description
├── planned_date
├── status (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)
├── created_by (FK → users)
└── created_at

distributions
├── id (PK)
├── distribution_event_id (FK → distribution_events)
├── beneficiary_id (FK → beneficiaries)
├── distribution_date
├── distributed_by (FK → users)
├── total_value
└── status
```

### Default Data

**Pre-loaded on first run:**
- 26 Barangays of Mati City, Davao Oriental
- 5 Inventory Categories (Food, Hygiene, Medical, Clothing, Shelter)
- Default admin user (username: `admin`, password: `password`)

## 🏗️ Backend Architecture

### Architecture Pattern

**Layered Architecture:**
```
┌─────────────────────────────────────┐
│   Controllers (JavaFX/FXML)        │  ← Presentation Layer
├─────────────────────────────────────┤
│   Services (Business Logic)         │  ← Business Logic Layer
├─────────────────────────────────────┤
│   DatabaseConfig (JDBC)             │  ← Data Access Layer
├─────────────────────────────────────┤
│   MySQL / SQLite Database           │  ← Persistence Layer
└─────────────────────────────────────┘
```

### Service Layer

**Location**: `src/main/java/com/aidsync/service/`

**Services:**

1. **AuthenticationService**
   - User authentication
   - Password hashing (BCrypt)
   - Failed login tracking
   - Account locking (after 3 failed attempts)
   - Password change

2. **UserService**
   - User CRUD operations
   - Username/email validation
   - User creation with role assignment

3. **BeneficiaryService**
   - Beneficiary CRUD
   - Search and filtering
   - Vulnerability assessment management
   - Beneficiary ID generation (BEN00001, BEN00002, etc.)
   - Geographic data integration

4. **InventoryService**
   - Inventory item management
   - Stock tracking
   - Stock movements (IN/OUT/ADJUSTMENT)
   - Low stock alerts
   - Expiration tracking
   - Category and supplier management

5. **DashboardService**
   - Statistics aggregation
   - Report generation
   - Dashboard metrics

### Data Access Pattern

**Direct JDBC (No ORM):**
- Uses `PreparedStatement` for all queries (SQL injection protection)
- Manual ResultSet mapping to model objects
- Connection pooling via singleton pattern
- Transaction management with `setAutoCommit(false)`

**Example Pattern:**
```java
try (Connection conn = DatabaseConfig.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.setString(1, parameter);
    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            // Map to model object
        }
    }
}
```

### Transaction Management

**Manual Transaction Control:**
```java
Connection conn = DatabaseConfig.getConnection();
conn.setAutoCommit(false);
try {
    // Multiple operations
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
} finally {
    conn.setAutoCommit(true);
    conn.close();
}
```

**Used for:**
- Creating beneficiaries (beneficiary + vulnerabilities)
- Stock movements (movement record + stock update)
- Multi-table operations

### Model Layer

**Location**: `src/main/java/com/aidsync/model/`

**Models:**
- `User.java` - User entity with roles and status
- `Beneficiary.java` - Beneficiary with vulnerability flags
- `InventoryItem.java` - Inventory items with stock tracking

**Features:**
- Enum types for status/role fields
- Utility methods (e.g., `getAge()`, `isVulnerable()`)
- Display name mappings for UI

### Security Features

**Password Security:**
- BCrypt hashing (12 rounds)
- No plain text passwords stored
- Password verification on login

**Account Security:**
- Failed login attempt tracking
- Automatic account locking (3 attempts)
- Account status management (ACTIVE, INACTIVE, LOCKED)

**SQL Injection Protection:**
- All queries use `PreparedStatement`
- Parameterized queries only
- No string concatenation in SQL

### Database Compatibility

**Cross-Database Support:**
- MySQL-specific: `AUTO_INCREMENT`, `SUBSTRING()`, `CURDATE()`
- SQLite-specific: `AUTOINCREMENT`, `SUBSTR()`, `date('now')`
- Conditional SQL generation based on `DatabaseConfig.isUsingMySQL()`

**Example:**
```java
String sql = DatabaseConfig.isUsingMySQL() ?
    "SELECT LAST_INSERT_ID() as id" :
    "SELECT last_insert_rowid() as id";
```

## 📈 Data Flow

### Typical Request Flow

```
1. User Action (UI)
   ↓
2. Controller (LoginController, BeneficiaryController, etc.)
   ↓
3. Service Layer (AuthenticationService, BeneficiaryService, etc.)
   ↓
4. DatabaseConfig.getConnection()
   ↓
5. JDBC Query Execution
   ↓
6. ResultSet Mapping to Model
   ↓
7. Return to Controller
   ↓
8. Update UI
```

### Example: Creating a Beneficiary

```
1. User fills form → BeneficiaryController.handleCreate()
2. Controller validates → BeneficiaryService.createBeneficiary()
3. Service generates ID → "BEN00001"
4. Service starts transaction
5. Insert into beneficiaries table
6. Get generated database ID
7. Insert into beneficiary_vulnerabilities table
8. Commit transaction
9. Return success to controller
10. Controller shows success message
```

## 🔍 Key Features

### 1. Automatic Table Creation
- Tables created on first run
- No manual database setup needed
- Works with both MySQL and SQLite

### 2. Default Data Seeding
- Barangays pre-loaded
- Inventory categories pre-loaded
- Default admin user created

### 3. Connection Management
- Singleton connection pattern
- Auto-reconnection on failure
- Connection reuse across requests

### 4. Error Handling
- SQLException caught and logged
- User-friendly error messages
- Transaction rollback on errors

### 5. Logging
- SLF4J with Logback
- All database operations logged
- Security events logged (login attempts)

## 📝 Database Queries

### Common Query Patterns

**Search with Filters:**
```java
StringBuilder sql = new StringBuilder("SELECT ... WHERE 1=1");
List<Object> params = new ArrayList<>();

if (filter != null) {
    sql.append(" AND column = ?");
    params.add(filter);
}
```

**Join Queries:**
```java
SELECT b.*, br.name as barangay_name, p.name as purok_name
FROM beneficiaries b
LEFT JOIN barangays br ON b.barangay_id = br.id
LEFT JOIN puroks p ON b.purok_id = p.id
```

**Aggregation:**
```java
SELECT COUNT(*) as total,
       SUM(total_value) as total_value
FROM distributions
WHERE distribution_date BETWEEN ? AND ?
```

## 🎯 Design Decisions

### Why Direct JDBC?
- **Simplicity**: No ORM overhead
- **Control**: Full SQL control
- **Lightweight**: No additional dependencies
- **Performance**: Direct database access

### Why Dual Database Support?
- **Flexibility**: Works with or without MySQL
- **Development**: SQLite for local development
- **Production**: MySQL for better performance
- **Deployment**: No database setup required

### Why Manual Transaction Management?
- **Explicit Control**: Clear transaction boundaries
- **Error Handling**: Explicit rollback on errors
- **Debugging**: Easier to trace transaction flow

## 🔐 Security Considerations

**Implemented:**
- ✅ Password hashing (BCrypt)
- ✅ SQL injection protection (PreparedStatement)
- ✅ Account locking mechanism
- ✅ Role-based access control

**Could Be Improved:**
- Connection pooling (currently singleton)
- Prepared statement caching
- Database connection encryption (SSL)
- Audit logging (currently basic)

## 📊 Performance Considerations

**Current:**
- Single connection (could be bottleneck)
- No connection pooling
- No query caching
- Direct JDBC (no ORM overhead)

**Optimizations:**
- Indexes on frequently queried columns
- Prepared statements (reusable)
- Efficient joins for related data
- Transaction batching for bulk operations

---

## Summary

**Backend Type**: Java Desktop Application (JavaFX)
**Database**: MySQL (primary) / SQLite (fallback)
**Data Access**: Direct JDBC with PreparedStatement
**Architecture**: 3-tier (Controller → Service → Database)
**Transaction Management**: Manual (setAutoCommit)
**Security**: BCrypt passwords, SQL injection protection
**Compatibility**: Cross-database (MySQL/SQLite)

The architecture is **simple, straightforward, and functional** - perfect for a desktop application managing aid distribution with moderate complexity.

