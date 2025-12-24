@echo off
REM Simple compile script using Maven Wrapper
echo Compiling AIDSYNC 2.0...

if not exist "mvnw.cmd" (
    echo [ERROR] Maven wrapper not found
    pause
    exit /b 1
)

call mvnw.cmd clean compile

if errorlevel 1 (
    echo [ERROR] Compilation failed
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Compilation complete!
pause
