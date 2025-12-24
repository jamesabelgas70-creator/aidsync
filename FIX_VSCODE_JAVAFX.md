# Fix: JavaFX Error in VS Code IDE

## Problem
When clicking "Run Java" or play button in VS Code, you get:
```
Error: JavaFX runtime components are missing
```

But `run.bat` works fine from command prompt.

## Why This Happens
VS Code's Java extension uses `.vscode/launch.json` which has **hardcoded paths from another machine**. The paths don't match your machine.

## Solution (Quick Fix)

### Step 1: Run the IDE Setup Script
Open PowerShell or Command Prompt in the project folder and run:
```powershell
powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
```

This will:
- Detect your Maven repository location
- Generate correct paths for YOUR machine
- Create/update `.vscode/launch.json`

### Step 2: Close and Reopen VS Code
**IMPORTANT:** VS Code must be completely closed and reopened for changes to take effect.

### Step 3: Run from VS Code
- Press `F5`, OR
- Click "Run Java" button, OR
- Open Run and Debug panel (Ctrl+Shift+D)
- Select "Run AidSyncApplication"
- Click Run

## Alternative: Use Maven Wrapper (Recommended)

Since `run.bat` works, you can configure VS Code to use Maven Wrapper:

### Option A: Use Tasks (Easier)
1. Create `.vscode/tasks.json` (see below)
2. Press `Ctrl+Shift+P`
3. Type "Tasks: Run Task"
4. Select "Run AIDSYNC"

### Option B: Use Terminal in VS Code
1. Open Terminal in VS Code (`Ctrl+`` `)
2. Run: `run.bat`
3. Works every time!

## Why run.bat Works But IDE Doesn't

**run.bat:**
- Uses Maven Wrapper (`mvnw.cmd`)
- Uses JavaFX Maven plugin (`javafx:run`)
- Automatically handles JavaFX module path
- No manual configuration needed

**VS Code Java Extension:**
- Uses `launch.json` configuration
- Needs explicit JavaFX module paths
- Paths are machine-specific
- Must be configured per machine

## Verification

After running `setup-ide-run.ps1`, check:
1. `.vscode/launch.json` exists
2. `vmArgs` contains paths with YOUR username (not "Asus" or other)
3. Paths point to: `C:\Users\[YourUsername]\.m2\repository\org\openjfx\`

## Still Not Working?

1. **Check if dependencies are downloaded:**
   ```batch
   mvnw.cmd dependency:resolve
   ```

2. **Verify JavaFX exists:**
   Check: `%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.1\`

3. **Regenerate launch.json:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
   ```

4. **Restart VS Code completely** (not just reload window)

5. **Check VS Code Java Extension:**
   - Make sure "Extension Pack for Java" is installed
   - Reload VS Code after installing

## Quick Reference

| Method | Command | Works? |
|--------|---------|--------|
| VS Code Run Button | Needs `setup-ide-run.ps1` first | ✅ After setup |
| Command: `run.bat` | `run.bat` | ✅ Always works |
| Command: Maven | `mvnw.cmd javafx:run` | ✅ Always works |
| VS Code Task | See tasks.json below | ✅ After setup |

---

**Remember:** `run.bat` always works because it uses Maven Wrapper. For IDE integration, run `setup-ide-run.ps1` once per machine.

