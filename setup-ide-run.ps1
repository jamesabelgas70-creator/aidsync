# Script to set up IDE run configuration for AIDSYNC with JavaFX
$ErrorActionPreference = "Stop"

Write-Host "Setting up IDE run configuration for AIDSYNC..." -ForegroundColor Cyan

# Get user profile path
$userProfile = $env:USERPROFILE
$mavenRepo = "$userProfile\.m2\repository"

# JavaFX paths
$javafxVersion = "21.0.1"
$javafxControlsDir = "$mavenRepo\org\openjfx\javafx-controls\$javafxVersion"
$javafxFxmlDir = "$mavenRepo\org\openjfx\javafx-fxml\$javafxVersion"
$javafxBaseDir = "$mavenRepo\org\openjfx\javafx-base\$javafxVersion"
$javafxGraphicsDir = "$mavenRepo\org\openjfx\javafx-graphics\$javafxVersion"

# Build module path (escape backslashes for JSON)
$modulePath = "$javafxControlsDir;$javafxFxmlDir;$javafxBaseDir;$javafxGraphicsDir"
$modulePathEscaped = $modulePath -replace '\\', '\\'

# Create .vscode directory if it doesn't exist
if (-not (Test-Path ".vscode")) {
    New-Item -ItemType Directory -Path ".vscode" -Force | Out-Null
}

# Create launch.json
$launchJson = @{
    version = "0.2.0"
    configurations = @(
        @{
            type = "java"
            name = "Run AidSyncApplication"
            request = "launch"
            mainClass = "com.aidsync.AidSyncApplication"
            projectName = "aidsync-system"
            vmArgs = "--module-path `"$modulePath`" --add-modules javafx.controls,javafx.fxml --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED"
            args = ""
            console = "internalConsole"
        }
    )
} | ConvertTo-Json -Depth 10

$launchJson | Out-File -FilePath ".vscode\launch.json" -Encoding UTF8

Write-Host "`nIDE run configuration created successfully!" -ForegroundColor Green
Write-Host "You can now run 'AidSyncApplication' from the Run and Debug panel (F5)" -ForegroundColor Yellow
Write-Host "`nModule Path: $modulePath" -ForegroundColor Gray

