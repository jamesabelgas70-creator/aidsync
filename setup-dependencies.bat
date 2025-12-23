@echo off
echo Setting up AIDSYNC 2.0 dependencies...

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher and add it to your PATH
    pause
    exit /b 1
)

echo Java found, proceeding with dependency setup...

REM Try to use Maven wrapper first
if exist mvnw.cmd (
    echo Using Maven wrapper...
    call mvnw.cmd clean compile
) else (
    REM Try system Maven
    mvn -version >nul 2>&1
    if errorlevel 1 (
        echo ERROR: Maven is not installed or not in PATH
        echo Please install Apache Maven and add it to your PATH
        echo Or download Maven from: https://maven.apache.org/download.cgi
        pause
        exit /b 1
    ) else (
        echo Using system Maven...
        mvn clean compile
    )
)

if errorlevel 1 (
    echo ERROR: Failed to compile project
    echo Please check the error messages above
    pause
    exit /b 1
)

echo.
echo SUCCESS: Dependencies downloaded and project compiled successfully!
echo You can now open the project in your IDE.
echo.
pause