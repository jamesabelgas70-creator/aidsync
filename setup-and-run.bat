@echo off
REM Comprehensive setup and run script for AIDSYNC
echo ========================================
echo AIDSYNC 2.0 - Setup and Run
echo ========================================
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)
echo [OK] Java found

REM Check for Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [WARNING] Maven not found in PATH
    echo Attempting to download dependencies using alternative method...
    goto :download_deps_manual
)

echo [OK] Maven found
echo.
echo Downloading dependencies...
call mvn dependency:resolve -q
if errorlevel 1 (
    echo [ERROR] Failed to download dependencies
    pause
    exit /b 1
)
echo [OK] Dependencies downloaded

:compile
echo.
echo Compiling project...
call mvn compile -q
if errorlevel 1 (
    echo [ERROR] Compilation failed
    pause
    exit /b 1
)
echo [OK] Project compiled

:run
echo.
echo Setting up IDE configuration...
powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
echo.
echo ========================================
echo Starting AIDSYNC Application...
echo ========================================
call run-javafx.bat
goto :end

:download_deps_manual
echo.
echo [INFO] Please install Maven or run 'mvn dependency:resolve' manually
echo You can download Maven from: https://maven.apache.org/download.cgi
echo.
echo Alternatively, you can download JavaFX SDK manually from:
echo https://openjfx.io/
echo.
pause
exit /b 1

:end
pause

