@echo off
setlocal enabledelayedexpansion

echo [INFO] Starting Customer CRM Application in H2 Mode (In-Memory)...

:: 1. Try to set Java 21 if it exists in standard path
set "JAVA21_PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin"
if exist "!JAVA21_PATH!\java.exe" (
    echo [INFO] Found Java 21 at !JAVA21_PATH!
    set "PATH=!JAVA21_PATH!;%PATH%"
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
)

:: 2. Build the project
echo [INFO] Building the project with Maven...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Maven build failed.
    exit /b 1
)

:: 3. Run with H2 overrides
echo [INFO] Running the application with H2 database (No Redis, No Kafka)...
java -jar target\customer-crud-1.0.0.jar ^
  --spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1 ^
  --spring.datasource.driver-class-name=org.h2.Driver ^
  --spring.datasource.username=sa ^
  --spring.datasource.password= ^
  --spring.jpa.database-platform=org.hibernate.dialect.H2Dialect ^
  --spring.flyway.enabled=false ^
  --spring.docker.compose.enabled=false ^
  --spring.cache.type=none ^
  --spring.autoconfigure.exclude=org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration

pause
