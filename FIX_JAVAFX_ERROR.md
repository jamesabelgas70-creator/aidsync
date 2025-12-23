# Fix: "JavaFX runtime components are missing" Error

## Quick Fix (2 minutes)

**On the other machine, run this:**

```batch
setup-and-run.bat
```

This will automatically:
1. Download all dependencies (including JavaFX)
2. Compile the project
3. Set up IDE configuration for that machine
4. Run the application

## Why This Error Happens

The error occurs because:
1. **JavaFX dependencies are not downloaded** to the Maven repository on that machine
2. **IDE configuration has wrong paths** - the `.vscode/launch.json` file contains paths from YOUR machine, not the other machine

## Step-by-Step Fix

### Step 1: Download Dependencies
```batch
mvn dependency:resolve
```
This downloads JavaFX to: `C:\Users\[OtherUser]\.m2\repository\org\openjfx\`

### Step 2: Compile Project
```batch
mvn compile
```

### Step 3: Fix IDE Configuration
```powershell
powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
```
This creates `.vscode/launch.json` with the CORRECT paths for that machine.

### Step 4: Run Application
- Press F5 in the IDE, OR
- Run `run-javafx.bat`

## Diagnostic Tool

To check what's wrong, run:
```batch
check-setup.bat
```

This will tell you exactly what's missing or misconfigured.

## Common Issues

### Issue 1: "Maven not found"
**Solution:** Install Maven from https://maven.apache.org/download.cgi and add to PATH

### Issue 2: "JavaFX JARs not found"
**Solution:** Run `mvn dependency:resolve` to download them

### Issue 3: "IDE configuration has wrong paths"
**Solution:** Run `setup-ide-run.ps1` to regenerate with correct paths

### Issue 4: "Application classes not found"
**Solution:** Run `mvn compile` to compile the project

## Important Notes

1. **Each machine needs its own setup** - The Maven repository path is different for each user
2. **Don't commit `.vscode/launch.json`** - It contains machine-specific paths
3. **Always run `setup-ide-run.ps1`** on a new machine before running from IDE

## Verification

After setup, verify:
1. ✅ JavaFX exists: `%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.1\`
2. ✅ Classes compiled: `target\classes\com\aidsync\AidSyncApplication.class` exists
3. ✅ IDE config exists: `.vscode\launch.json` exists
4. ✅ No hardcoded paths: `launch.json` should have paths for the current user

## Still Not Working?

Run the diagnostic:
```batch
check-setup.bat
```

It will tell you exactly what's wrong and how to fix it.

