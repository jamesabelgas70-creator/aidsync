# AIDSYNC - Quick Start (New & Improved!)

## 🎉 What's New?

**Maven Wrapper is now included!** No more Maven installation hassles.

## 🚀 Running the Application

### Windows:
```batch
run.bat
```

### Or directly:
```batch
mvnw.cmd javafx:run
```

### Linux/Mac:
```bash
./mvnw javafx:run
```

## ✅ Prerequisites

**Only one thing needed:**
- Java 11 or higher ([Download here](https://adoptium.net/))

**That's it!** No Maven installation needed anymore.

## 📝 First Run

On first run:
1. Maven Wrapper downloads Maven automatically (if needed)
2. Downloads all dependencies
3. Compiles the project
4. Runs the application

**Takes 2-5 minutes on first run** (downloading everything)

## 🎯 Common Commands

| Command | What it does |
|---------|-------------|
| `run.bat` | Run the application |
| `mvnw.cmd javafx:run` | Run using JavaFX plugin |
| `mvnw.cmd clean compile` | Compile only |
| `mvnw.cmd clean package` | Build JAR file |

## 🔧 Troubleshooting

### "mvnw.cmd not found"
- Make sure you're in the project root (where `pom.xml` is)
- The `mvnw.cmd` file should be in the same folder

### "Java not found"
- Install Java 11+ from [Adoptium](https://adoptium.net/)
- Verify: `java -version`

### First run is slow
- **Normal!** Maven and dependencies are downloading
- Subsequent runs are much faster

### Application won't start
- Try: `mvnw.cmd clean compile` first
- Then: `mvnw.cmd javafx:run`
- Check console for error messages

## 💡 Why This is Better

**Before (Old Way):**
- ❌ Need to install Maven
- ❌ Need to configure PATH
- ❌ Multiple setup scripts
- ❌ Machine-specific configurations

**Now (With Maven Wrapper):**
- ✅ No Maven installation needed
- ✅ Works out of the box
- ✅ Single command: `run.bat`
- ✅ Same setup on every machine

## 📚 More Information

- [README_SIMPLE.md](README_SIMPLE.md) - Detailed guide
- [README.md](README.md) - Full documentation

---

**That's it!** Just run `run.bat` and you're done! 🎉

