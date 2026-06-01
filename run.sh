#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}[INFO] Starting Customer CRM Application...${NC}"

# 1. Попытка найти Java 21 в стандартных путях
JAVA21_PATH="/usr/lib/jvm/java-21-openjdk-amd64/bin"
if [ -d "$JAVA21_PATH" ]; then
    echo -e "${GREEN}[INFO] Found Java 21 at $JAVA21_PATH${NC}"
    export PATH="$JAVA21_PATH:$PATH"
    export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
fi

# 2. Проверка Java
if ! command -v java &> /dev/null
then
    echo -e "${RED}[ERROR] Java is not installed or not in PATH.${NC}"
    exit 1
fi

# 2. Сборка проекта
echo -e "${GREEN}[INFO] Building the project with Maven...${NC}"
./mvnw clean package -DskipTests || mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] Maven build failed.${NC}"
    exit 1
fi

# 3. Поиск JAR файла
JAR_FILE=$(ls target/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}[ERROR] Could not find the generated JAR file in target directory.${NC}"
    exit 1
fi

# 4. Запуск приложения
echo -e "${GREEN}[INFO] Running the application: $JAR_FILE${NC}"
java -jar "$JAR_FILE"
