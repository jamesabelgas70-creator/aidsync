@echo off
REM Batch file to run AIDSYNC with JavaFX from Maven dependencies
echo AIDSYNC 2.0 - Starting application...

REM Find Maven local repository
set MAVEN_REPO=%USERPROFILE%\.m2\repository
if not exist "%MAVEN_REPO%" (
    echo ERROR: Maven local repository not found
    echo Please run 'mvn dependency:resolve' first to download dependencies
    pause
    exit /b 1
)

REM JavaFX version
set JAVAFX_VERSION=21.0.1
set JAVAFX_BASE=%MAVEN_REPO%\org\openjfx

REM JavaFX JARs (Windows platform-specific)
set JAVAFX_CONTROLS=%JAVAFX_BASE%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar
set JAVAFX_FXML=%JAVAFX_BASE%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar
set JAVAFX_BASE_JAR=%JAVAFX_BASE%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar
set JAVAFX_GRAPHICS=%JAVAFX_BASE%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar

REM Check if JavaFX JARs exist
if not exist "%JAVAFX_CONTROLS%" (
    echo ERROR: JavaFX JARs not found. Please download dependencies first.
    echo Run: mvn dependency:resolve
    pause
    exit /b 1
)

REM Build module path (directories containing module JARs)
for %%F in ("%JAVAFX_CONTROLS%") do set JAVAFX_CONTROLS_DIR=%%~dpF
for %%F in ("%JAVAFX_FXML%") do set JAVAFX_FXML_DIR=%%~dpF
for %%F in ("%JAVAFX_BASE_JAR%") do set JAVAFX_BASE_DIR=%%~dpF
for %%F in ("%JAVAFX_GRAPHICS%") do set JAVAFX_GRAPHICS_DIR=%%~dpF

REM Remove trailing backslashes and build module path
set JAVAFX_CONTROLS_DIR=%JAVAFX_CONTROLS_DIR:~0,-1%
set JAVAFX_FXML_DIR=%JAVAFX_FXML_DIR:~0,-1%
set JAVAFX_BASE_DIR=%JAVAFX_BASE_DIR:~0,-1%
set JAVAFX_GRAPHICS_DIR=%JAVAFX_GRAPHICS_DIR:~0,-1%

set MODULE_PATH=%JAVAFX_CONTROLS_DIR%;%JAVAFX_FXML_DIR%;%JAVAFX_BASE_DIR%;%JAVAFX_GRAPHICS_DIR%

REM Build classpath
set CLASSPATH=target\classes
set CLASSPATH=%CLASSPATH%;%JAVAFX_CONTROLS%
set CLASSPATH=%CLASSPATH%;%JAVAFX_FXML%
set CLASSPATH=%CLASSPATH%;%JAVAFX_BASE_JAR%
set CLASSPATH=%CLASSPATH%;%JAVAFX_GRAPHICS%

REM Add other dependencies
set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\org\mariadb\jdbc\mariadb-java-client\3.2.0\mariadb-java-client-3.2.0.jar
set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\org\xerial\sqlite-jdbc\3.44.1.0\sqlite-jdbc-3.44.1.0.jar
set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\at\favre\lib\bcrypt\0.10.2\bcrypt-0.10.2.jar
set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar
set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\ch\qos\logback\logback-classic\1.4.11\logback-classic-1.4.11.jar
set CLASSPATH=%CLASSPATH%;%MAVEN_REPO%\ch\qos\logback\logback-core\1.4.11\logback-core-1.4.11.jar

REM Check if classes are compiled
if not exist "target\classes\com\aidsync\AidSyncApplication.class" (
    echo ERROR: Application classes not found. Please compile first.
    pause
    exit /b 1
)

REM Run the application
echo Running AIDSYNC Application...
echo.

java --module-path "%MODULE_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     -cp "%CLASSPATH%" ^
     com.aidsync.AidSyncApplication

if errorlevel 1 (
    echo.
    echo Application exited with error.
    pause
)

