@echo off
REM Script to commit Maven Wrapper setup
echo ========================================
echo Committing Maven Wrapper Setup
echo ========================================
echo.

echo Adding Maven Wrapper files...
git add mvnw.cmd mvnw .mvn/wrapper/maven-wrapper.properties

echo Adding updated build files...
git add pom.xml run.bat compile.bat .gitignore

echo Adding documentation...
git add README.md README_SIMPLE.md QUICK_START_NEW.md MAVEN_WRAPPER_SETUP.md COMMIT_AND_PUSH.md

echo.
echo Files staged. Review with: git status
echo.
echo Ready to commit? (Y/N)
set /p confirm=
if /i "%confirm%" NEQ "Y" (
    echo Cancelled.
    exit /b 0
)

echo.
echo Committing...
git commit -m "Add Maven Wrapper - Simplify setup, no Maven installation needed"

if errorlevel 1 (
    echo [ERROR] Commit failed
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Committed!
echo.
echo Ready to push? (Y/N)
set /p confirm=
if /i "%confirm%" NEQ "Y" (
    echo You can push later with: git push
    pause
    exit /b 0
)

echo.
echo Pushing to remote...
git push

if errorlevel 1 (
    echo [ERROR] Push failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo [SUCCESS] All done!
echo ========================================
echo.
echo On other machines, users can now just:
echo   1. git pull
echo   2. run.bat
echo   3. That's it!
echo.
pause

