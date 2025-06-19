@echo off
echo Starting File Hosting Application...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 21 or higher
    pause
    exit /b 1
)

REM Check if Maven wrapper exists
if not exist "mvnw.cmd" (
    echo ERROR: Maven wrapper not found
    echo Please run this from the project root directory
    pause
    exit /b 1
)

echo Building and starting the application...
echo This may take a few minutes on first run...
echo.

mvnw.cmd spring-boot:run

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start the application
    pause
    exit /b 1
)

pause
