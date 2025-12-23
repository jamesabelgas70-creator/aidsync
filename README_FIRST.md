# ⚠️ READ THIS FIRST - Setup Instructions

## If you're getting "JavaFX runtime components are missing" error:

### Quick Fix (Run this first!):
```batch
setup-and-run.bat
```

This one command will:
- ✅ Download all dependencies
- ✅ Compile the project  
- ✅ Set up IDE configuration
- ✅ Run the application

### Or check what's wrong:
```batch
check-setup.bat
```

This diagnostic tool will tell you exactly what needs to be fixed.

---

## For New Machines / First Time Setup

**IMPORTANT:** Each machine needs to run the setup once!

### Step 1: Run Setup
```batch
setup-and-run.bat
```

### Step 2: If running from IDE
After setup completes:
1. Close and reopen your IDE (VS Code/Cursor)
2. Press F5 or use Run and Debug panel
3. Select "Run AidSyncApplication"

### Step 3: If still getting errors
Run the diagnostic:
```batch
check-setup.bat
```

---

## Why Setup is Needed

The IDE configuration (`.vscode/launch.json`) contains paths specific to each user's machine:
- Your machine: `C:\Users\Asus\...`
- Other machine: `C:\Users\OtherUser\...`

The `setup-ide-run.ps1` script automatically detects the correct paths for each machine.

---

## Files You Need

| File | When to Use |
|------|-------------|
| `setup-and-run.bat` | **First time setup** - Run this first! |
| `check-setup.bat` | **Diagnostic** - Check what's wrong |
| `setup-ide-run.ps1` | **Fix IDE config** - Regenerate launch.json |
| `run-javafx.bat` | **Run app** - After setup is complete |

---

## Still Having Issues?

1. Run `check-setup.bat` to see what's wrong
2. Read `FIX_JAVAFX_ERROR.md` for detailed solutions
3. Make sure Maven is installed: https://maven.apache.org/download.cgi

---

**Remember:** Run `setup-and-run.bat` on each new machine before running the application!

