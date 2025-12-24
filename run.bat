@echo off
REM Simple run script using Maven Wrapper
echo ========================================
echo AIDSYNC 2.0 - Running Application
echo ========================================
echo.

REM Check if mvnw exists
if not exist "mvnw.cmd" (
    echo [ERROR] Maven wrapper not found
    echo Please make sure you're in the project root directory
    pause
    exit /b 1
)

REM Run using Maven wrapper and JavaFX plugin
call mvnw.cmd javafx:run

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to run application
    echo.
    echo Troubleshooting:
    echo   1. Make sure Java 11+ is installed
    echo   2. Check internet connection (first run downloads Maven)
    echo   3. Run: mvnw.cmd clean compile (to compile first)
    echo.
    pause
)
