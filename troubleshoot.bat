@echo off
REM Comprehensive troubleshooting script
echo ========================================
echo AIDSYNC Troubleshooting Tool
echo ========================================
echo.

echo This script will help diagnose and fix common issues.
echo.

REM Step 1: Check Java
echo [Step 1/5] Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo    [FAIL] Java not found
    echo    [FIX] Install Java 11+ from https://adoptium.net/
    set FIXES_NEEDED=1
) else (
    echo    [OK] Java found
    java -version 2>&1 | findstr /C:"version"
)

REM Step 2: Check Maven
echo.
echo [Step 2/5] Checking Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo    [FAIL] Maven not found
    echo    [FIX] Install Maven from https://maven.apache.org/download.cgi
    set FIXES_NEEDED=1
) else (
    echo    [OK] Maven found
    mvn -version 2>&1 | findstr /C:"Apache Maven"
)

REM Step 3: Download dependencies if needed
echo.
echo [Step 3/5] Checking dependencies...
set MAVEN_REPO=%USERPROFILE%\.m2\repository
set JAVAFX_VERSION=21.0.1
set JAVAFX_CONTROLS=%MAVEN_REPO%\org\openjfx\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar

if not exist "%JAVAFX_CONTROLS%" (
    echo    [FAIL] JavaFX dependencies not found
    echo    [FIX] Downloading dependencies now...
    if exist "mvn" (
        call mvn dependency:resolve -q
        if errorlevel 1 (
            echo    [ERROR] Failed to download dependencies
            echo    [FIX] Run manually: mvn dependency:resolve
            set FIXES_NEEDED=1
        ) else (
            echo    [OK] Dependencies downloaded
        )
    ) else (
        echo    [ERROR] Maven not available to download dependencies
        set FIXES_NEEDED=1
    )
) else (
    echo    [OK] JavaFX dependencies found
)

REM Step 4: Compile if needed
echo.
echo [Step 4/5] Checking compilation...
if not exist "target\classes\com\aidsync\AidSyncApplication.class" (
    echo    [FAIL] Application not compiled
    echo    [FIX] Compiling now...
    if exist "mvn" (
        call mvn compile -q
        if errorlevel 1 (
            echo    [ERROR] Compilation failed
            echo    [FIX] Run manually: mvn compile
            set FIXES_NEEDED=1
        ) else (
            echo    [OK] Application compiled
        )
    ) else (
        echo    [ERROR] Maven not available to compile
        set FIXES_NEEDED=1
    )
) else (
    echo    [OK] Application compiled
)

REM Step 5: Fix IDE config
echo.
echo [Step 5/5] Checking IDE configuration...
if not exist ".vscode\launch.json" (
    echo    [WARN] IDE configuration not found
    echo    [FIX] Creating IDE configuration...
    powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1 >nul 2>&1
    if errorlevel 1 (
        echo    [WARN] Could not create IDE config (this is OK if not using IDE)
    ) else (
        echo    [OK] IDE configuration created
    )
) else (
    echo    [OK] IDE configuration exists
)

REM Summary
echo.
echo ========================================
echo Summary
echo ========================================
if defined FIXES_NEEDED (
    echo [WARNING] Some issues were found and attempted fixes
    echo.
    echo Try running the application now:
    echo   run-direct.bat
    echo.
    echo If it still doesn't work, check the errors above.
) else (
    echo [SUCCESS] All checks passed!
    echo.
    echo You can now run the application:
    echo   - From command line: run-direct.bat
    echo   - From IDE: Press F5 (after closing/reopening IDE)
)

echo.
pause

