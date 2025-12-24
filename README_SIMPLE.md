# AIDSYNC 2.0 - Simple Setup Guide

## 🚀 Quick Start

### Prerequisites
- **Java 11 or higher** (Download from [Adoptium](https://adoptium.net/))
- **That's it!** No Maven installation needed.

### Running the Application

**Windows:**
```batch
run.bat
```

**Or directly:**
```batch
mvnw.cmd javafx:run
```

**Linux/Mac:**
```bash
./mvnw javafx:run
```

### First Run
On first run, Maven Wrapper will automatically:
- Download Maven (if needed)
- Download all dependencies
- Compile the project
- Run the application

This may take 2-5 minutes on first run.

### Compiling Only
```batch
mvnw.cmd clean compile
```

Or use:
```batch
compile.bat
```

## 📁 Project Structure

```
aidsync/
├── mvnw.cmd          # Maven wrapper (Windows)
├── mvnw              # Maven wrapper (Linux/Mac)
├── run.bat           # Simple run script
├── compile.bat       # Simple compile script
├── pom.xml           # Maven configuration
└── src/              # Source code
```

## 🔧 Troubleshooting

### "mvnw.cmd not found"
- Make sure you're in the project root directory
- The `mvnw.cmd` file should be in the same folder as `pom.xml`

### "Java not found"
- Install Java 11+ from [Adoptium](https://adoptium.net/)
- Make sure Java is in your PATH
- Verify: `java -version`

### "Failed to download dependencies"
- Check your internet connection
- Maven needs to download dependencies on first run
- Firewall may be blocking Maven Central repository

### Application won't start
- First, compile: `mvnw.cmd clean compile`
- Then run: `mvnw.cmd javafx:run`
- Check for error messages in the console

## 💡 What is Maven Wrapper?

Maven Wrapper (`mvnw`) is a self-contained Maven installation that:
- ✅ No need to install Maven separately
- ✅ Ensures everyone uses the same Maven version
- ✅ Works out of the box on any machine
- ✅ Automatically downloads Maven on first use

## 🎯 Key Commands

| Command | Description |
|---------|-------------|
| `run.bat` | Run the application |
| `mvnw.cmd javafx:run` | Run using JavaFX plugin |
| `mvnw.cmd clean compile` | Clean and compile |
| `mvnw.cmd clean package` | Build JAR file |

## 📝 Notes

- **First run is slower** - Maven and dependencies are downloaded
- **No Maven installation needed** - Everything is self-contained
- **Works on any machine** - Just needs Java installed
- **IDE support** - Most IDEs recognize Maven Wrapper automatically

---

**That's it!** No complex setup, no multiple scripts, just `run.bat` and you're done! 🎉

