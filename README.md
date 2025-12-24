# AIDSYNC 2.0 - Aid Distribution Management System

## Overview
AIDSYNC is a comprehensive desktop application designed for Local Government Units (LGUs), Barangay Staff, and Disaster Response Teams to efficiently manage aid distribution during emergencies and regular relief operations.

## 🚀 Quick Start

### ⭐ Simple Method (Recommended):
**Just run:**
```batch
run.bat
```

**Or directly:**
```batch
mvnw.cmd javafx:run
```

**That's it!** No setup needed. Maven Wrapper handles everything automatically.

### 📋 Prerequisites
- **Java 11 or higher** - Download from [Adoptium](https://adoptium.net/)
- **No Maven installation needed** - Maven Wrapper is included!

### 🎯 First Run
On first run, Maven Wrapper will:
- Automatically download Maven (if needed)
- Download all dependencies
- Compile the project
- Run the application

This takes 2-5 minutes on first run.

### 📖 For More Details
- [README_SIMPLE.md](README_SIMPLE.md) - **Start here!** Simple guide with Maven Wrapper
- [SIMPLE_INSTRUCTIONS.txt](SIMPLE_INSTRUCTIONS.txt) - Quick reference
- [HOW_TO_USE_SETUP_AND_RUN.txt](HOW_TO_USE_SETUP_AND_RUN.txt) - Detailed guide (legacy method)

### 🔧 Troubleshooting
- **Java not found:** Install Java 11+ from [Adoptium](https://adoptium.net/)
- **First run slow:** Normal! Maven and dependencies are downloading
- **Still having issues:** See [README_SIMPLE.md](README_SIMPLE.md) troubleshooting section

## Features

### 🔐 Authentication & Security
- Multi-factor authentication support
- Role-based access control (Super Admin, LGU Admin, Barangay Captain, Distribution Staff, Viewer)
- Failed login protection with account locking
- Session management with auto-logout
- Audit trail for all system activities

### 👥 Beneficiary Management
- Comprehensive beneficiary profiles with vulnerability assessment
- Household management and family grouping
- Advanced search and filtering capabilities
- Bulk operations (import/export, batch updates)
- Photo capture and document management
- Priority-based categorization

### 📦 Inventory Management
- Multi-category aid package tracking
- Stock level monitoring with alerts
- Supplier and donation management
- Batch/lot tracking with expiration dates
- Barcode/QR code support
- Automated stock movements

### 🚚 Distribution Management
- Distribution event planning and execution
- Beneficiary selection with criteria-based filtering
- Real-time distribution recording
- QR code scanning and digital signatures
- Offline capability with sync
- Receipt generation and verification

### 📊 Reporting & Analytics
- Standard and custom reports
- Interactive dashboards with charts
- Geographic coverage analysis
- Distribution efficiency metrics
- Donor accountability reports
- Scheduled report generation

### 🗺️ GIS & Mapping
- Interactive mapping with beneficiary plotting
- Coverage visualization
- Route optimization for distribution
- Hazard zone mapping
- Population density analysis

### ⚙️ System Administration
- User and role management
- System configuration
- Database backup and restore
- Data import/export utilities
- Performance monitoring

### 🆘 Emergency Mode
- Simplified crisis interface
- Rapid beneficiary registration
- Bulk distribution capabilities
- Offline operation support
- Priority tagging system

## Technical Specifications

### Requirements
- **OS**: Windows 10 or higher
- **Java**: Java 11 or higher
- **RAM**: 4GB minimum, 8GB recommended
- **Storage**: 500MB minimum, 1GB recommended
- **Database**: MySQL 5.7+ (primary) or SQLite 3 (fallback)

### Technology Stack
- **Frontend**: JavaFX 17
- **Backend**: Java 11
- **Database**: MySQL/SQLite
- **Security**: BCrypt password hashing
- **Build Tool**: Maven
- **Logging**: SLF4J with Logback

## Installation

### Prerequisites
1. Install Java 11 or higher
2. Install MySQL (optional, SQLite fallback available)
3. Download AIDSYNC 2.0 release

### Database Setup (MySQL)
```sql
CREATE DATABASE aidsync;
CREATE USER 'aidsync_user'@'localhost' IDENTIFIED BY 'aidsync_pass';
GRANT ALL PRIVILEGES ON aidsync.* TO 'aidsync_user'@'localhost';
FLUSH PRIVILEGES;
```

### Running the Application
```bash
# Using Maven
mvn javafx:run

# Or using JAR file
java -jar aidsync-2.0.jar
```

### Default Login
- **Username**: admin
- **Password**: password
- **Role**: Super Administrator

## Configuration

### Database Configuration
The application automatically detects and connects to:
1. MySQL (primary) - `jdbc:mysql://localhost:3306/aidsync`
2. SQLite (fallback) - `aidsync.db` in application directory

### Barangays (Pre-configured for Mati City)
The system comes pre-loaded with all 26 barangays of Mati City, Davao Oriental:
- Badas, Bato, Bohewan, Cabuaya, Central, Culian, Dahican
- Dawan, Don Enrique Lopez, Don Martin Marundan, Don Salvador Lopez Sr.
- Langka, Lawigan, Libudon, Luban, Macambol, Mamali, Matiao
- Mayo, Sainz, Tagabakid, Tagbinonga, Taguibo, Tamisan, Tarragona, Wasi

## User Roles & Permissions

### Super Admin
- Full system access
- User management
- System configuration
- All modules and features

### LGU Admin
- Manage staff and beneficiaries
- View all reports
- Distribution management
- Inventory oversight

### Barangay Captain
- Manage own barangay only
- Limited reporting access
- Distribution for assigned area
- Beneficiary management

### Distribution Staff
- Record distributions only
- Basic beneficiary lookup
- Inventory viewing
- Limited reporting

### Viewer
- Read-only access
- View reports only
- No data modification
- Dashboard viewing

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/aidsync/
│   │   ├── config/          # Database and system configuration
│   │   ├── controller/      # JavaFX controllers
│   │   ├── model/          # Data models
│   │   ├── service/        # Business logic services
│   │   ├── dao/            # Data access objects
│   │   ├── util/           # Utility classes
│   │   ├── security/       # Security components
│   │   └── module/         # Feature modules
│   └── resources/
│       ├── fxml/           # JavaFX FXML files
│       ├── css/            # Stylesheets
│       └── images/         # Application images
```

### Building from Source
```bash
git clone <repository-url>
cd aidsync-2.0
mvn clean compile
mvn javafx:run
```

### Creating Distribution Package
```bash
mvn clean package
# Creates target/aidsync-2.0.jar
```

## Implementation Phases

### Phase 1 (MVP) ✅
- Basic authentication
- Beneficiary management
- Simple distribution recording
- Inventory tracking
- Essential reports

### Phase 2 (Enhanced) 🚧
- Advanced beneficiary profiling
- Distribution planning
- GIS mapping
- Mobile data collection
- Advanced reporting

### Phase 3 (Complete) 📋
- Financial tracking
- Supplier management
- API integration
- Advanced analytics
- Emergency mode

### Phase 4 (Future) 🔮
- Cloud synchronization
- Mobile app
- AI-powered analytics
- Blockchain transparency
- National system integration

## Support & Documentation

### Getting Help
- Check the built-in help system (F1)
- Review user manual (included)
- Contact system administrator
- Submit bug reports via GitHub issues

### Training Materials
- User training videos
- Administrator guide
- Quick reference cards
- Best practices documentation

## License
Copyright © 2023 AIDSYNC Development Team
All rights reserved.

## Contributing
This is a government project for Mati City, Davao Oriental. 
For contributions or modifications, please contact the development team.

---

**AIDSYNC 2.0** - Empowering communities through efficient aid distribution management.