# Script to set up IDE run configuration for AIDSYNC with JavaFX
# This script automatically detects the user's Maven repository and creates the IDE configuration
$ErrorActionPreference = "Stop"

Write-Host "Setting up IDE run configuration for AIDSYNC..." -ForegroundColor Cyan

# Get user profile path
$userProfile = $env:USERPROFILE
$mavenRepo = "$userProfile\.m2\repository"

# Check if Maven repository exists
if (-not (Test-Path $mavenRepo)) {
    Write-Host "`n[WARNING] Maven repository not found at: $mavenRepo" -ForegroundColor Yellow
    Write-Host "Please run 'mvn dependency:resolve' first to download dependencies" -ForegroundColor Yellow
    Write-Host "Or run 'setup-and-run.bat' to automatically set up everything" -ForegroundColor Cyan
    exit 1
}

# JavaFX paths
$javafxVersion = "21.0.1"
$javafxControlsDir = "$mavenRepo\org\openjfx\javafx-controls\$javafxVersion"
$javafxFxmlDir = "$mavenRepo\org\openjfx\javafx-fxml\$javafxVersion"
$javafxBaseDir = "$mavenRepo\org\openjfx\javafx-base\$javafxVersion"
$javafxGraphicsDir = "$mavenRepo\org\openjfx\javafx-graphics\$javafxVersion"

# Check if JavaFX dependencies exist
$missingDeps = @()
if (-not (Test-Path $javafxControlsDir)) { $missingDeps += "javafx-controls" }
if (-not (Test-Path $javafxFxmlDir)) { $missingDeps += "javafx-fxml" }
if (-not (Test-Path $javafxBaseDir)) { $missingDeps += "javafx-base" }
if (-not (Test-Path $javafxGraphicsDir)) { $missingDeps += "javafx-graphics" }

if ($missingDeps.Count -gt 0) {
    Write-Host "`n[ERROR] JavaFX dependencies not found:" -ForegroundColor Red
    foreach ($dep in $missingDeps) {
        Write-Host "  - $dep" -ForegroundColor Yellow
    }
    Write-Host "`nPlease run one of the following:" -ForegroundColor Yellow
    Write-Host "  1. mvn dependency:resolve" -ForegroundColor Cyan
    Write-Host "  2. setup-and-run.bat (recommended)" -ForegroundColor Cyan
    exit 1
}

# Build module path (escape backslashes for JSON)
$modulePath = "$javafxControlsDir;$javafxFxmlDir;$javafxBaseDir;$javafxGraphicsDir"

# Create .vscode directory if it doesn't exist
if (-not (Test-Path ".vscode")) {
    New-Item -ItemType Directory -Path ".vscode" -Force | Out-Null
}

# Create launch.json with proper escaping
$vmArgs = "--module-path `"$modulePath`" --add-modules javafx.controls,javafx.fxml --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED"

$launchConfig = @{
    version = "0.2.0"
    configurations = @(
        @{
            type = "java"
            name = "Run AidSyncApplication"
            request = "launch"
            mainClass = "com.aidsync.AidSyncApplication"
            projectName = "aidsync-system"
            vmArgs = $vmArgs
            args = ""
            console = "internalConsole"
        }
    )
}

# Convert to JSON and write to file
$launchConfig | ConvertTo-Json -Depth 10 | Out-File -FilePath ".vscode\launch.json" -Encoding UTF8 -NoNewline

Write-Host "`n[SUCCESS] IDE run configuration created!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. Close and reopen your IDE (VS Code/Cursor)" -ForegroundColor Yellow
Write-Host "  2. Press F5 or open Run and Debug panel" -ForegroundColor Yellow
Write-Host "  3. Select 'Run AidSyncApplication' and click Run" -ForegroundColor Yellow
Write-Host ""
Write-Host "Module Path configured: $modulePath" -ForegroundColor Gray
Write-Host ""
Write-Host "IMPORTANT: If you still get JavaFX errors after reopening IDE," -ForegroundColor Yellow
Write-Host "make sure the IDE is using the updated launch.json file." -ForegroundColor Yellow
