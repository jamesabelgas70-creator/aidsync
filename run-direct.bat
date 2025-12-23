@echo off
REM Direct run script that doesn't rely on IDE configuration
REM This script finds JavaFX automatically and runs the application
echo ========================================
echo AIDSYNC 2.0 - Direct Run
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

REM Find Maven repository
set MAVEN_REPO=%USERPROFILE%\.m2\repository
if not exist "%MAVEN_REPO%" (
    echo [ERROR] Maven repository not found
    echo.
    echo Please run setup first:
    echo   setup-and-run.bat
    echo.
    pause
    exit /b 1
)

REM JavaFX version
set JAVAFX_VERSION=21.0.1
set JAVAFX_BASE=%MAVEN_REPO%\org\openjfx

REM Detect platform
set PLATFORM=win
if "%OS%"=="Windows_NT" set PLATFORM=win

REM JavaFX JARs
set JAVAFX_CONTROLS=%JAVAFX_BASE%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-%PLATFORM%.jar
set JAVAFX_FXML=%JAVAFX_BASE%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-%PLATFORM%.jar
set JAVAFX_BASE_JAR=%JAVAFX_BASE%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-%PLATFORM%.jar
set JAVAFX_GRAPHICS=%JAVAFX_BASE%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-%PLATFORM%.jar

REM Check if JavaFX exists
if not exist "%JAVAFX_CONTROLS%" (
    echo [ERROR] JavaFX not found
    echo.
    echo Please download dependencies first:
    echo   mvn dependency:resolve
    echo.
    echo Or run complete setup:
    echo   setup-and-run.bat
    echo.
    pause
    exit /b 1
)

REM Get directories for module path
for %%F in ("%JAVAFX_CONTROLS%") do set JAVAFX_CONTROLS_DIR=%%~dpF
for %%F in ("%JAVAFX_FXML%") do set JAVAFX_FXML_DIR=%%~dpF
for %%F in ("%JAVAFX_BASE_JAR%") do set JAVAFX_BASE_DIR=%%~dpF
for %%F in ("%JAVAFX_GRAPHICS%") do set JAVAFX_GRAPHICS_DIR=%%~dpF

REM Remove trailing backslashes
set JAVAFX_CONTROLS_DIR=%JAVAFX_CONTROLS_DIR:~0,-1%
set JAVAFX_FXML_DIR=%JAVAFX_FXML_DIR:~0,-1%
set JAVAFX_BASE_DIR=%JAVAFX_BASE_DIR:~0,-1%
set JAVAFX_GRAPHICS_DIR=%JAVAFX_GRAPHICS_DIR:~0,-1%

set MODULE_PATH=%JAVAFX_CONTROLS_DIR%;%JAVAFX_FXML_DIR%;%JAVAFX_BASE_DIR%;%JAVAFX_GRAPHICS_DIR%

REM Build classpath
set CLASSPATH=target\classes

REM Add JavaFX to classpath
set CLASSPATH=%CLASSPATH%;%JAVAFX_CONTROLS%
set CLASSPATH=%CLASSPATH%;%JAVAFX_FXML%
set CLASSPATH=%CLASSPATH%;%JAVAFX_BASE_JAR%
set CLASSPATH=%CLASSPATH%;%JAVAFX_GRAPHICS%

REM Add other dependencies (check if they exist)
if exist "%MAVEN_REPO%\org\mariadb\jdbc\mariadb-java-client\3.2.0\mariadb-java-client-3.2.0.jar" (
    set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\org\mariadb\jdbc\mariadb-java-client\3.2.0\mariadb-java-client-3.2.0.jar
)
if exist "%MAVEN_REPO%\org\xerial\sqlite-jdbc\3.44.1.0\sqlite-jdbc-3.44.1.0.jar" (
    set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\org\xerial\sqlite-jdbc\3.44.1.0\sqlite-jdbc-3.44.1.0.jar
)
if exist "%MAVEN_REPO%\at\favre\lib\bcrypt\0.10.2\bcrypt-0.10.2.jar" (
    set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\at\favre\lib\bcrypt\0.10.2\bcrypt-0.10.2.jar
)
if exist "%MAVEN_REPO%\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar" (
    set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar
)
if exist "%MAVEN_REPO%\ch\qos\logback\logback-classic\1.4.11\logback-classic-1.4.11.jar" (
    set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\ch\qos\logback\logback-classic\1.4.11\logback-classic-1.4.11.jar
)
if exist "%MAVEN_REPO%\ch\qos\logback\logback-core\1.4.11\logback-core-1.4.11.jar" (
    set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\ch\qos\logback\logback-core\1.4.11\logback-core-1.4.11.jar
)

REM Check if compiled
if not exist "target\classes\com\aidsync\AidSyncApplication.class" (
    echo [ERROR] Application not compiled
    echo.
    echo Please compile first:
    echo   mvn compile
    echo.
    echo Or run complete setup:
    echo   setup-and-run.bat
    echo.
    pause
    exit /b 1
)

REM Run the application
echo [OK] Starting AIDSYNC Application...
echo.
echo Module Path: %MODULE_PATH%
echo.

java --module-path "%MODULE_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     -cp "%CLASSPATH%" ^
     com.aidsync.AidSyncApplication

if errorlevel 1 (
    echo.
    echo [ERROR] Application failed to start
    echo.
    echo Troubleshooting:
    echo   1. Run: check-setup.bat
    echo   2. Make sure JavaFX dependencies are downloaded
    echo   3. Check error message above
    echo.
    pause
)

