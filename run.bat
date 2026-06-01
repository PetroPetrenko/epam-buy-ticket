@echo off
setlocal enabledelayedexpansion

echo [INFO] Starting Customer CRM Application...

:: 1. Try to set Java 21 if it exists in standard path
set "JAVA21_PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin"
if exist "!JAVA21_PATH!\java.exe" (
    echo [INFO] Found Java 21 at !JAVA21_PATH!
    set "PATH=!JAVA21_PATH!;%PATH%"
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
)

:: 2. Check Java Version
java -version
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    exit /b 1
)

:: 3. Build the project
echo [INFO] Building the project with Maven...
:: Используем системный mvn, но он подхватит наш PATH с Java 21
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Maven build failed.
    exit /b 1
)

:: 3. Find the JAR file
for %%f in (target\*.jar) do (
    set JAR_FILE=%%f
)

if not defined JAR_FILE (
    echo [ERROR] Could not find the generated JAR file in target directory.
    exit /b 1
)

:: 4. Run the application
echo [INFO] Running the application: !JAR_FILE!
java -jar !JAR_FILE!

pause
