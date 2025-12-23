@echo off
REM Comprehensive setup and run script for AIDSYNC
echo ========================================
echo AIDSYNC 2.0 - Setup and Run
echo ========================================
echo.
echo This script will:
echo   1. Check Java and Maven
echo   2. Download all dependencies (including JavaFX)
echo   3. Compile the project
echo   4. Set up IDE configuration
echo   5. Run the application
echo.
pause

REM Check Java
echo [1/5] Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo    [ERROR] Java is not installed or not in PATH
    echo    Please install Java 11 or higher from https://adoptium.net/
    pause
    exit /b 1
)
echo    [OK] Java found
java -version 2>&1 | findstr /C:"version"

REM Check for Maven
echo.
echo [2/5] Checking Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo    [ERROR] Maven not found in PATH
    echo.
    echo    Please install Maven:
    echo    1. Download from: https://maven.apache.org/download.cgi
    echo    2. Extract to a folder (e.g., C:\Program Files\Apache\maven)
    echo    3. Add Maven's bin folder to your PATH
    echo    4. Restart Command Prompt
    echo.
    echo    Or run: troubleshoot.bat (for automated fixes)
    pause
    exit /b 1
)
echo    [OK] Maven found
mvn -version 2>&1 | findstr /C:"Apache Maven"

REM Download dependencies
echo.
echo [3/5] Downloading dependencies (this may take a few minutes)...
call mvn dependency:resolve
if errorlevel 1 (
    echo    [ERROR] Failed to download dependencies
    echo    Check your internet connection and try again
    pause
    exit /b 1
)
echo    [OK] Dependencies downloaded

REM Compile
echo.
echo [4/5] Compiling project...
call mvn compile
if errorlevel 1 (
    echo    [ERROR] Compilation failed
    echo    Check the error messages above
    pause
    exit /b 1
)
echo    [OK] Project compiled

REM Setup IDE config
echo.
echo [5/5] Setting up IDE configuration...
powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1
if errorlevel 1 (
    echo    [WARNING] IDE configuration setup had issues
    echo    [INFO] This is OK if you're not using an IDE
    echo    [INFO] You can run 'fix-ide-config.bat' later if needed
) else (
    echo    [OK] IDE configuration created
)

REM Run
echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo IMPORTANT: If running from IDE:
echo   1. CLOSE your IDE completely
echo   2. REOPEN your IDE
echo   3. Press F5 or use Run and Debug panel
echo.
echo Starting application now (from command line)...
echo.
pause

call run-direct.bat

:end

