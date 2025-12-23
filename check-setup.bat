@echo off
REM Diagnostic script to check AIDSYNC setup
echo ========================================
echo AIDSYNC Setup Diagnostic Tool
echo ========================================
echo.

set ERRORS=0
set WARNINGS=0

REM Check Java
echo [1/6] Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo    [ERROR] Java is not installed or not in PATH
    set /a ERRORS+=1
) else (
    echo    [OK] Java found
    java -version 2>&1 | findstr /C:"version"
)

REM Check Maven
echo.
echo [2/6] Checking Maven installation...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo    [WARNING] Maven is not installed or not in PATH
    set /a WARNINGS+=1
    echo    [INFO] Install Maven from: https://maven.apache.org/download.cgi
) else (
    echo    [OK] Maven found
    mvn -version 2>&1 | findstr /C:"Apache Maven"
)

REM Check Maven repository
echo.
echo [3/6] Checking Maven repository...
set MAVEN_REPO=%USERPROFILE%\.m2\repository
if not exist "%MAVEN_REPO%" (
    echo    [ERROR] Maven repository not found at: %MAVEN_REPO%
    echo    [INFO] Run 'mvn dependency:resolve' to create it
    set /a ERRORS+=1
) else (
    echo    [OK] Maven repository found: %MAVEN_REPO%
)

REM Check JavaFX dependencies
echo.
echo [4/6] Checking JavaFX dependencies...
set JAVAFX_VERSION=21.0.1
set JAVAFX_BASE=%MAVEN_REPO%\org\openjfx
set JAVAFX_CONTROLS=%JAVAFX_BASE%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar
set JAVAFX_FXML=%JAVAFX_BASE%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar
set JAVAFX_BASE_JAR=%JAVAFX_BASE%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar
set JAVAFX_GRAPHICS=%JAVAFX_BASE%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar

set MISSING=0
if not exist "%JAVAFX_CONTROLS%" (
    echo    [ERROR] javafx-controls not found
    set /a MISSING+=1
    set /a ERRORS+=1
)
if not exist "%JAVAFX_FXML%" (
    echo    [ERROR] javafx-fxml not found
    set /a MISSING+=1
    set /a ERRORS+=1
)
if not exist "%JAVAFX_BASE_JAR%" (
    echo    [ERROR] javafx-base not found
    set /a MISSING+=1
    set /a ERRORS+=1
)
if not exist "%JAVAFX_GRAPHICS%" (
    echo    [ERROR] javafx-graphics not found
    set /a MISSING+=1
    set /a ERRORS+=1
)

if %MISSING%==0 (
    echo    [OK] All JavaFX dependencies found
) else (
    echo    [INFO] Missing %MISSING% JavaFX module(s)
    echo    [INFO] Run 'mvn dependency:resolve' to download them
)

REM Check compiled classes
echo.
echo [5/6] Checking compiled classes...
if not exist "target\classes\com\aidsync\AidSyncApplication.class" (
    echo    [ERROR] Application classes not compiled
    echo    [INFO] Run 'mvn compile' to compile
    set /a ERRORS+=1
) else (
    echo    [OK] Application classes found
)

REM Check IDE configuration
echo.
echo [6/6] Checking IDE configuration...
if not exist ".vscode\launch.json" (
    echo    [WARNING] IDE configuration not found
    echo    [INFO] Run 'setup-ide-run.ps1' to create it
    set /a WARNINGS+=1
) else (
    echo    [OK] IDE configuration found
    REM Check if it has hardcoded paths
    findstr /C:"C:\\Users\\" ".vscode\launch.json" >nul 2>&1
    if not errorlevel 1 (
        echo    [WARNING] launch.json contains hardcoded user paths
        echo    [INFO] Run 'setup-ide-run.ps1' to regenerate with correct paths
        set /a WARNINGS+=1
    )
)

REM Summary
echo.
echo ========================================
echo Diagnostic Summary
echo ========================================
if %ERRORS%==0 (
    if %WARNINGS%==0 (
        echo [SUCCESS] All checks passed! You can run the application.
        echo.
        echo To run:
        echo   - From IDE: Press F5 or use Run and Debug panel
        echo   - From command line: run-javafx.bat
    ) else (
        echo [WARNING] Setup has %WARNINGS% warning(s) but should work
        echo.
        echo Recommendations:
        if %WARNINGS% GTR 0 (
            echo   - Run 'setup-ide-run.ps1' to fix IDE configuration
        )
    )
) else (
    echo [ERROR] Setup has %ERRORS% error(s) that must be fixed
    echo.
    echo To fix:
    echo   1. Run 'setup-and-run.bat' (recommended - fixes everything)
    echo   2. Or follow the instructions above for each error
)

echo.
pause

