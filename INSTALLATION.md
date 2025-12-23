# AIDSYNC 2.0 Installation Guide

## System Requirements

### Minimum Requirements
- **Operating System**: Windows 10 (64-bit)
- **Java**: Java 11 or higher
- **RAM**: 4GB
- **Storage**: 500MB free space
- **Display**: 1024x768 resolution

### Recommended Requirements
- **Operating System**: Windows 10/11 (64-bit)
- **Java**: Java 17 LTS
- **RAM**: 8GB or higher
- **Storage**: 1GB free space (for data and logs)
- **Display**: 1920x1080 resolution
- **Network**: For MySQL database (optional)

## Pre-Installation Steps

### 1. Install Java
1. Download Java 11 or higher from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
2. Run the installer and follow the setup wizard
3. Verify installation by opening Command Prompt and typing:
   ```
   java -version
   ```
   You should see Java version information

### 2. Install MySQL (Optional)
AIDSYNC can work with SQLite (no installation required) or MySQL for better performance.

**For MySQL:**
1. Download MySQL Community Server from [MySQL Website](https://dev.mysql.com/downloads/mysql/)
2. Install MySQL with default settings
3. Remember the root password you set during installation
4. Create AIDSYNC database:
   ```sql
   CREATE DATABASE aidsync;
   CREATE USER 'aidsync_user'@'localhost' IDENTIFIED BY 'aidsync_pass';
   GRANT ALL PRIVILEGES ON aidsync.* TO 'aidsync_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

## Installation Methods

### Method 1: Using Pre-built JAR File (Recommended)

1. **Download AIDSYNC 2.0**
   - Download the latest release from the official source
   - Extract the ZIP file to your desired location (e.g., `C:\AIDSYNC`)

2. **Run the Application**
   - Double-click `run.bat` to start the application
   - Or open Command Prompt in the AIDSYNC folder and run:
     ```
     java -jar aidsync-2.0.jar
     ```

### Method 2: Building from Source

1. **Install Maven**
   - Download Apache Maven from [Maven Website](https://maven.apache.org/download.cgi)
   - Extract and add Maven's `bin` directory to your PATH
   - Verify installation: `mvn -version`

2. **Download Source Code**
   - Extract the source code to your desired location

3. **Build the Application**
   ```bash
   cd AIDSYNC-2.0
   mvn clean compile
   ```

4. **Run the Application**
   ```bash
   mvn javafx:run
   ```

5. **Create Distribution Package (Optional)**
   ```bash
   mvn clean package
   ```

## First-Time Setup

### 1. Initial Launch
- Run the application using one of the methods above
- The login screen will appear

### 2. Default Login Credentials
- **Username**: `admin`
- **Password**: `password`
- **Role**: Super Administrator

⚠️ **IMPORTANT**: Change the default password immediately after first login!

### 3. Database Initialization
- The application will automatically create necessary database tables
- For MySQL: Ensure the database service is running
- For SQLite: A database file will be created automatically

### 4. Initial Configuration
1. **Change Admin Password**
   - Go to User Profile → Change Password
   - Use a strong password with at least 8 characters

2. **Organization Settings**
   - Navigate to Administration → System Settings
   - Update organization name, address, and contact information

3. **Create Additional Users**
   - Go to Administration → User Management
   - Create accounts for your staff with appropriate roles

## Configuration

### Database Configuration
The application automatically detects and connects to available databases:

1. **MySQL** (Primary choice)
   - Connection: `jdbc:mysql://localhost:3306/aidsync`
   - Username: `<database_username>`
   - Password: `<database_password>`

2. **SQLite** (Fallback)
   - File: `aidsync.db` in application directory
   - No additional configuration required

### Application Settings
Edit these settings through the Administration panel:

- **Session Timeout**: Default 30 minutes
- **Failed Login Attempts**: Default 3 attempts before lock
- **Backup Retention**: Default 30 days
- **Distribution Limits**: Configurable per beneficiary

## Troubleshooting

### Common Issues

#### 1. "Java not found" Error
**Solution**: 
- Ensure Java 11+ is installed
- Add Java to your system PATH
- Restart Command Prompt/Terminal

#### 2. Database Connection Failed
**For MySQL**:
- Verify MySQL service is running
- Check username/password in database configuration
- Ensure database `aidsync` exists

**For SQLite**:
- Check file permissions in application directory
- Ensure sufficient disk space

#### 3. Application Won't Start
**Solutions**:
- Check if port 3306 (MySQL) is available
- Verify all JAR dependencies are present
- Check logs in `logs/aidsync.log` for detailed errors

#### 4. Performance Issues
**Solutions**:
- Increase Java heap size: `java -Xmx2g -jar aidsync-2.0.jar`
- Use MySQL instead of SQLite for better performance
- Ensure adequate RAM and disk space

### Log Files
Check these log files for troubleshooting:
- `logs/aidsync.log` - Application logs
- `logs/audit.log` - User activity audit trail

## Security Considerations

### 1. Password Security
- Change default admin password immediately
- Enforce strong password policies
- Regular password updates

### 2. Database Security
- Use strong database passwords
- Limit database user privileges
- Regular database backups

### 3. File Permissions
- Restrict access to application directory
- Secure backup files
- Protect log files containing sensitive information

### 4. Network Security
- Use firewall to restrict database access
- Consider VPN for remote access
- Regular security updates

## Backup and Recovery

### 1. Database Backup
**MySQL**:
```bash
mysqldump -u aidsync_user -p aidsync > backup_YYYYMMDD.sql
```

**SQLite**:
- Simply copy the `aidsync.db` file

### 2. Application Data Backup
- Copy entire application directory
- Include logs and configuration files
- Store backups in secure location

### 3. Recovery Process
1. Stop the application
2. Restore database from backup
3. Replace application files if needed
4. Restart application
5. Verify data integrity

## Uninstallation

### 1. Stop Application
- Close AIDSYNC application
- Stop MySQL service if not needed for other applications

### 2. Backup Data (Optional)
- Export important data
- Save configuration files

### 3. Remove Files
- Delete application directory
- Remove database (if desired)
- Clean up log files

### 4. Remove Java/MySQL (Optional)
- Uninstall Java if not needed for other applications
- Uninstall MySQL if not needed for other applications

## Support

### Getting Help
1. Check this installation guide
2. Review application logs
3. Consult user manual
4. Contact system administrator

### Reporting Issues
When reporting issues, include:
- Operating system version
- Java version
- Database type and version
- Error messages from logs
- Steps to reproduce the issue

---

**AIDSYNC 2.0** - Aid Distribution Management System
© 2023 Mati City LGU, Davao Oriental