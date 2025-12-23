# AIDSYNC 2.0 - Quick Start Guide

## For New Machines / First Time Setup

If you're running AIDSYNC on a new machine and getting the "JavaFX runtime components are missing" error, follow these steps:

### Option 1: Automatic Setup (Recommended)

1. **Open Command Prompt or PowerShell** in the project directory

2. **Run the setup script:**
   ```batch
   setup-and-run.bat
   ```
   
   This script will:
   - Check if Java is installed
   - Download all Maven dependencies (including JavaFX)
   - Compile the project
   - Set up IDE configuration
   - Run the application

### Option 2: Manual Setup

#### Step 1: Install Maven (if not installed)

1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract and add to PATH
3. Verify: `mvn -version`

#### Step 2: Download Dependencies

```batch
mvn dependency:resolve
```

This downloads all required dependencies including JavaFX to your Maven repository.

#### Step 3: Compile the Project

```batch
mvn compile
```

#### Step 4: Set Up IDE Configuration

```powershell
powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
```

This creates the `.vscode/launch.json` file with the correct JavaFX module paths for your machine.

#### Step 5: Run the Application

**From IDE:**
- Press F5 or use Run and Debug panel
- Select "Run AidSyncApplication"

**From Command Line:**
```batch
run-javafx.bat
```

Or:
```powershell
powershell -ExecutionPolicy Bypass -File run-with-javafx.ps1
```

## Troubleshooting

### Error: "JavaFX runtime components are missing"

**Solution:** Run `setup-and-run.bat` or manually run `mvn dependency:resolve`

### Error: "Maven not found"

**Solution:** 
1. Install Maven from https://maven.apache.org/download.cgi
2. Add Maven to your system PATH
3. Restart your terminal/IDE

### Error: "Maven local repository not found"

**Solution:** 
1. Run `mvn dependency:resolve` to create the repository
2. Or manually create: `%USERPROFILE%\.m2\repository`

### IDE Run Configuration Not Working

**Solution:**
1. Run `setup-ide-run.ps1` to regenerate the launch configuration
2. Make sure the `.vscode/launch.json` file exists
3. Check that JavaFX dependencies are in your Maven repository

## File Structure

- `setup-and-run.bat` - Complete setup and run script (use this first!)
- `setup-ide-run.ps1` - Sets up IDE configuration for your machine
- `run-javafx.bat` - Runs the application (requires dependencies)
- `run-with-javafx.ps1` - PowerShell version of run script
- `.vscode/launch.json` - IDE run configuration (auto-generated)

## Requirements

- **Java**: 11 or higher (Java 21 recommended)
- **Maven**: 3.6+ (for dependency management)
- **OS**: Windows 10/11

## Notes

- The Maven repository is typically located at: `%USERPROFILE%\.m2\repository`
- JavaFX dependencies are automatically downloaded to: `%USERPROFILE%\.m2\repository\org\openjfx\`
- The IDE configuration is machine-specific and should be regenerated on each machine

