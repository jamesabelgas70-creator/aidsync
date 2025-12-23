# AIDSYNC Setup Instructions for New Machines

## Problem
When running AIDSYNC on a new machine, you may encounter:
```
Error: JavaFX runtime components are missing, and are required to run this application
```

## Solution

### Method 1: Automatic Setup (Easiest) ⭐

1. **Open Command Prompt** in the AIDSYNC project folder
2. **Run:**
   ```batch
   setup-and-run.bat
   ```
3. **Wait for it to complete** - it will:
   - Check Java installation
   - Download all dependencies (including JavaFX)
   - Compile the project
   - Set up IDE configuration
   - Run the application

### Method 2: Step-by-Step Manual Setup

#### Step 1: Install Maven (if not installed)
- Download from: https://maven.apache.org/download.cgi
- Extract and add to PATH
- Verify: `mvn -version`

#### Step 2: Download Dependencies
```batch
mvn dependency:resolve
```
This downloads JavaFX and all other dependencies to: `%USERPROFILE%\.m2\repository`

#### Step 3: Compile Project
```batch
mvn compile
```

#### Step 4: Set Up IDE Configuration
```powershell
powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
```
This creates `.vscode/launch.json` with correct paths for your machine.

#### Step 5: Run Application

**Option A: From IDE (VS Code/Cursor)**
- Press `F5` or open Run and Debug panel
- Select "Run AidSyncApplication"
- Click Run

**Option B: From Command Line**
```batch
run-javafx.bat
```

## Important Notes

1. **Maven Repository Location**
   - Default: `C:\Users\[YourUsername]\.m2\repository`
   - JavaFX is stored at: `%USERPROFILE%\.m2\repository\org\openjfx\`

2. **IDE Configuration**
   - The `.vscode/launch.json` file contains machine-specific paths
   - **Must be regenerated on each new machine** using `setup-ide-run.ps1`
   - Do NOT commit this file with hardcoded paths to git

3. **Dependencies**
   - All dependencies are downloaded automatically by Maven
   - No manual JAR file downloads needed
   - JavaFX version: 21.0.1

## Troubleshooting

### "Maven not found"
- Install Maven and add to PATH
- Or use `setup-and-run.bat` which will guide you

### "JavaFX JARs not found"
- Run `mvn dependency:resolve` to download
- Check: `%USERPROFILE%\.m2\repository\org\openjfx\`

### "Application classes not found"
- Run `mvn compile` to compile the project
- Check: `target\classes\com\aidsync\AidSyncApplication.class`

### IDE Run Configuration Not Working
- Run `setup-ide-run.ps1` to regenerate
- Make sure JavaFX dependencies are downloaded first

## Files Reference

| File | Purpose |
|------|---------|
| `setup-and-run.bat` | Complete automated setup (use this first!) |
| `setup-ide-run.ps1` | Generates IDE configuration for your machine |
| `run-javafx.bat` | Runs the application (requires setup first) |
| `run-with-javafx.ps1` | PowerShell version of run script |
| `QUICK_START.md` | Detailed quick start guide |

## Verification

After setup, verify everything works:

1. ✅ JavaFX dependencies exist: `%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.1\`
2. ✅ Project compiled: `target\classes\com\aidsync\AidSyncApplication.class` exists
3. ✅ IDE config exists: `.vscode\launch.json` exists
4. ✅ Application runs: No "JavaFX runtime components missing" error

## Need Help?

1. Check `QUICK_START.md` for detailed instructions
2. Review error messages - they usually tell you what's missing
3. Run `setup-and-run.bat` - it handles most issues automatically

