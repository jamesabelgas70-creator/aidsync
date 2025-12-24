# VS Code Setup Guide for AIDSYNC

## 🎯 Quick Fix for "JavaFX runtime components missing" in VS Code

### The Problem
- ❌ Clicking "Run Java" button in VS Code → Error
- ✅ Running `run.bat` from command prompt → Works!

**Why?** VS Code uses `launch.json` with wrong paths. `run.bat` uses Maven Wrapper which handles everything automatically.

## ✅ Solution 1: Fix IDE Configuration (For Run Button)

### Step 1: Run Setup Script
```powershell
powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
```

### Step 2: Close VS Code Completely
- File → Exit (don't just close window)
- Reopen VS Code

### Step 3: Try Run Button Again
- Press `F5` or click "Run Java"
- Should work now!

## ✅ Solution 2: Use VS Code Tasks (Recommended)

**This uses Maven Wrapper (like run.bat), so it always works!**

### Step 1: Use the Task
1. Press `Ctrl+Shift+P`
2. Type: `Tasks: Run Task`
3. Select: `Run AIDSYNC`
4. Application starts!

### Step 2: (Optional) Add Keyboard Shortcut
1. Press `Ctrl+K Ctrl+S` (Keyboard Shortcuts)
2. Search: `Tasks: Run Task`
3. Add shortcut (e.g., `Ctrl+F5`)

## ✅ Solution 3: Use Terminal in VS Code

**Easiest method - always works!**

1. Open Terminal in VS Code: `` Ctrl+` ``
2. Type: `run.bat`
3. Press Enter
4. Application runs!

## 📋 What Each Method Does

| Method | How It Works | Pros | Cons |
|--------|-------------|------|------|
| **Run Button (F5)** | Uses `launch.json` | Integrated with VS Code | Needs setup per machine |
| **Tasks (Ctrl+Shift+P)** | Uses `run.bat` | Always works, uses Maven Wrapper | Requires task selection |
| **Terminal (`run.bat`)** | Direct command | Always works, simple | Not integrated with debugger |

## 🔧 Detailed Setup

### For Run Button to Work:

1. **Download Dependencies:**
   ```batch
   mvnw.cmd dependency:resolve
   ```

2. **Generate IDE Config:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
   ```

3. **Verify launch.json:**
   - Open `.vscode/launch.json`
   - Check `vmArgs` has paths with YOUR username
   - Should NOT have "Asus" or other usernames

4. **Restart VS Code:**
   - File → Exit
   - Reopen VS Code

### For Tasks to Work:

1. **Verify tasks.json exists:**
   - Should be in `.vscode/tasks.json`
   - Already created if you have the latest code

2. **Use the task:**
   - `Ctrl+Shift+P` → `Tasks: Run Task` → `Run AIDSYNC`

## 🐛 Troubleshooting

### "setup-ide-run.ps1 not found"
- Make sure you're in the project root folder
- File should be in same folder as `pom.xml`

### "PowerShell execution policy error"
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### "JavaFX still not found after setup"
1. Check dependencies downloaded:
   ```batch
   mvnw.cmd dependency:resolve
   ```

2. Verify JavaFX exists:
   - Check: `%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.1\`
   - Should see `.jar` files there

3. Regenerate config:
   ```powershell
   powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
   ```

### "VS Code still uses old config"
- **Close VS Code completely** (File → Exit)
- Don't just close the window
- Reopen VS Code
- Try again

## 💡 Best Practice

**For VS Code users, I recommend:**

1. **Use Tasks** (`Ctrl+Shift+P` → `Run AIDSYNC`)
   - Always works
   - Uses Maven Wrapper
   - No configuration needed

2. **Or use Terminal** (`run.bat`)
   - Simple and reliable
   - Same as command prompt

3. **Only use Run Button** if you need debugging features
   - Requires `setup-ide-run.ps1` first
   - Machine-specific configuration

## 📝 Summary

**Quick Answer:**
- Run: `powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1`
- Close and reopen VS Code
- Try Run Button again

**Better Answer:**
- Use Tasks: `Ctrl+Shift+P` → `Tasks: Run Task` → `Run AIDSYNC`
- Or Terminal: `` Ctrl+` `` → `run.bat`
- These always work, no setup needed!

---

**Remember:** `run.bat` works because it uses Maven Wrapper. For IDE integration, you need to configure VS Code once per machine.

