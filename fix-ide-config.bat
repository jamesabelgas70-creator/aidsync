@echo off
REM Quick fix script for IDE configuration issues
echo ========================================
echo Fixing IDE Configuration
echo ========================================
echo.
echo This will regenerate the IDE run configuration with correct paths
echo for this machine.
echo.
pause

powershell -ExecutionPolicy Bypass -File setup-ide-run.ps1

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to create IDE configuration
    echo.
    echo Make sure:
    echo   1. Maven dependencies are downloaded (run 'mvn dependency:resolve')
    echo   2. PowerShell execution policy allows scripts
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Configuration Fixed!
echo ========================================
echo.
echo IMPORTANT: Close and reopen your IDE for changes to take effect.
echo.
echo Then:
echo   - Press F5, OR
echo   - Open Run and Debug panel
echo   - Select "Run AidSyncApplication"
echo.
pause

