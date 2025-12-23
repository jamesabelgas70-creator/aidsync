@echo off
cd /d "c:\Users\nashd\OneDrive\Documents\AIDSYNC 2.0"
java --module-path "C:\Program Files\Java\javafx-21.0.1\lib" --add-modules javafx.controls,javafx.fxml --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED -cp "target\classes;lib\*" com.aidsync.AidSyncApplication
pause