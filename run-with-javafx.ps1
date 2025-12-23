# PowerShell script to run AIDSYNC with JavaFX from Maven dependencies
$ErrorActionPreference = "Stop"

Write-Host "AIDSYNC 2.0 - Starting application..." -ForegroundColor Cyan

# Find Maven local repository
$mavenRepo = "$env:USERPROFILE\.m2\repository"
if (-not (Test-Path $mavenRepo)) {
    Write-Host "ERROR: Maven local repository not found at $mavenRepo" -ForegroundColor Red
    Write-Host "Please run 'mvn dependency:resolve' first to download dependencies" -ForegroundColor Yellow
    exit 1
}

# JavaFX version from pom.xml
$javafxVersion = "21.0.1"
$javafxBase = "$mavenRepo\org\openjfx"

# Find JavaFX JARs (use platform-specific Windows JARs)
$platform = "win"
$javafxControls = "$javafxBase\javafx-controls\$javafxVersion\javafx-controls-$javafxVersion-$platform.jar"
$javafxFxml = "$javafxBase\javafx-fxml\$javafxVersion\javafx-fxml-$javafxVersion-$platform.jar"
$javafxBaseJar = "$javafxBase\javafx-base\$javafxVersion\javafx-base-$javafxVersion-$platform.jar"
$javafxGraphics = "$javafxBase\javafx-graphics\$javafxVersion\javafx-graphics-$javafxVersion-$platform.jar"

# Check if JavaFX JARs exist
$missingJars = @()
if (-not (Test-Path $javafxControls)) { $missingJars += "javafx-controls" }
if (-not (Test-Path $javafxFxml)) { $missingJars += "javafx-fxml" }
if (-not (Test-Path $javafxBaseJar)) { $missingJars += "javafx-base" }
if (-not (Test-Path $javafxGraphics)) { $missingJars += "javafx-graphics" }

if ($missingJars.Count -gt 0) {
    Write-Host "ERROR: JavaFX JARs not found in Maven repository:" -ForegroundColor Red
    foreach ($jar in $missingJars) {
        Write-Host "  - $jar" -ForegroundColor Yellow
    }
    Write-Host "`nPlease download dependencies first:" -ForegroundColor Yellow
    Write-Host "  mvn dependency:resolve" -ForegroundColor Cyan
    Write-Host "`nOr download JavaFX SDK from: https://openjfx.io/" -ForegroundColor Yellow
    exit 1
}

# Build module path - point to directories containing each JavaFX module JAR
# Each module directory should contain the module JAR file
$javafxControlsDir = Split-Path $javafxControls -Parent
$javafxFxmlDir = Split-Path $javafxFxml -Parent
$javafxBaseDir = Split-Path $javafxBaseJar -Parent
$javafxGraphicsDir = Split-Path $javafxGraphics -Parent

# Module path should contain all directories with module JARs (semicolon-separated on Windows)
$modulePath = "$javafxControlsDir;$javafxFxmlDir;$javafxBaseDir;$javafxGraphicsDir"

# Build classpath
$classpath = "target\classes"
$classpath += ";$javafxControls"
$classpath += ";$javafxFxml"
$classpath += ";$javafxBaseJar"
$classpath += ";$javafxGraphics"

# Add other dependencies from Maven repository
$dependencies = @(
    "org\mariadb\jdbc\mariadb-java-client\3.2.0\mariadb-java-client-3.2.0.jar",
    "org\xerial\sqlite-jdbc\3.44.1.0\sqlite-jdbc-3.44.1.0.jar",
    "at\favre\lib\bcrypt\0.10.2\bcrypt-0.10.2.jar",
    "com\fasterxml\jackson\core\jackson-databind\2.16.1\jackson-databind-2.16.1.jar",
    "com\fasterxml\jackson\core\jackson-core\2.16.1\jackson-core-2.16.1.jar",
    "com\fasterxml\jackson\core\jackson-annotations\2.16.1\jackson-annotations-2.16.1.jar",
    "org\apache\poi\poi-ooxml\5.2.5\poi-ooxml-5.2.5.jar",
    "org\apache\poi\poi\5.2.5\poi-5.2.5.jar",
    "org\apache\poi\poi-ooxml-lite\5.2.5\poi-ooxml-lite-5.2.5.jar",
    "org\apache\xmlbeans\xmlbeans\5.2.0\xmlbeans-5.2.0.jar",
    "org\apache\commons\commons-compress\1.24.0\commons-compress-1.24.0.jar",
    "org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar",
    "ch\qos\logback\logback-classic\1.4.11\logback-classic-1.4.11.jar",
    "ch\qos\logback\logback-core\1.4.11\logback-core-1.4.11.jar"
)

foreach ($dep in $dependencies) {
    $depPath = "$mavenRepo\$dep"
    if (Test-Path $depPath) {
        $classpath += ";$depPath"
    }
}

# Check if classes are compiled
if (-not (Test-Path "target\classes\com\aidsync\AidSyncApplication.class")) {
    Write-Host "ERROR: Application classes not found. Please compile first:" -ForegroundColor Red
    Write-Host "  javac -cp ... (or use Maven: mvn compile)" -ForegroundColor Yellow
    exit 1
}

# Run the application
Write-Host "Running AIDSYNC Application..." -ForegroundColor Green
Write-Host "Module Path: $modulePath" -ForegroundColor Gray
Write-Host ""

java --module-path "$modulePath" `
     --add-modules javafx.controls,javafx.fxml `
     --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED `
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED `
     -cp "$classpath" `
     com.aidsync.AidSyncApplication

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nApplication exited with error code: $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

