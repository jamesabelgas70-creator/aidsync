@echo off
echo Compiling AIDSYNC 2.0...

mkdir target\classes 2>nul

echo Compiling utility classes...
javac -cp "lib/*;C:\javafx-sdk-21.0.9\lib\*" -d target\classes src\main\java\com\aidsync\util\*.java

echo Compiling config classes...
javac -cp "lib/*;C:\javafx-sdk-21.0.9\lib\*;target\classes" -d target\classes src\main\java\com\aidsync\config\*.java

echo Compiling model classes...
javac -cp "lib/*;C:\javafx-sdk-21.0.9\lib\*;target\classes" -d target\classes src\main\java\com\aidsync\model\*.java

echo Compiling service classes...
javac -cp "lib/*;C:\javafx-sdk-21.0.9\lib\*;target\classes" -d target\classes src\main\java\com\aidsync\service\*.java

echo Compiling controller classes...
javac -cp "lib/*;C:\javafx-sdk-21.0.9\lib\*;target\classes" -d target\classes src\main\java\com\aidsync\controller\*.java

echo Compiling main application...
javac -cp "lib/*;C:\javafx-sdk-21.0.9\lib\*;target\classes" -d target\classes src\main\java\com\aidsync\AidSyncApplication.java

echo Copying resources...
xcopy /E /I /Y src\main\resources target\classes 2>nul

echo Running AIDSYNC 2.0...
java -cp "target\classes;lib/*;C:\javafx-sdk-21.0.9\lib\*" --module-path "C:\javafx-sdk-21.0.9\lib" --add-modules javafx.controls,javafx.fxml --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED com.aidsync.AidSyncApplication

pause