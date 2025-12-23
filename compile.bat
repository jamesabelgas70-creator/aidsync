@echo off
cd /d "c:\Users\nashd\OneDrive\Documents\AIDSYNC 2.0"
mkdir target\classes 2>nul
javac -cp "lib\*" -d target\classes src\main\java\com\aidsync\*.java src\main\java\com\aidsync\*\*.java src\main\java\com\aidsync\*\*\*.java
echo Compilation complete
pause