@echo off
echo Building AIDSYNC 2.0...
echo.

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Apache Maven first
    pause
    exit /b 1
)

REM Clean and compile
echo Cleaning previous build...
mvn clean

echo Compiling project...
mvn compile

if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
echo Run 'run.bat' to start the application
pause