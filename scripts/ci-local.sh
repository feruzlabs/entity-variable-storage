#!/usr/bin/env bash
# CI bilan bir xil muhitda build — GitHub Actions dan oldin lokal tekshirish
set -e
cd "$(dirname "$0")/.."

export DOCKER_HOST="${DOCKER_HOST:-unix:///var/run/docker.sock}"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE="${TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE:-/var/run/docker.sock}"
export TESTCONTAINERS_RYUK_DISABLED=true

echo "Docker tekshiruvi..."
docker info
docker pull postgres:16-alpine

echo "Gradle build..."
java_available() {
  [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ] || command -v java &>/dev/null
}

if [ -x "./gradlew" ] && java_available; then
  ./gradlew build
elif command -v gradle &>/dev/null && java_available; then
  gradle build
  gradle publish -Pversion=1.0.0-SNAPSHOT
elif [ -x "./gradlew" ]; then
  echo "Java topilmadi. Docker orqali build (testlarsiz) ishga tushirilmoqda..."
  echo "To'liq testlar uchun tizimda Java 21 o'rnating va ./gradlew build ishlating."
  docker run --rm \
    -v "$(pwd):/project" \
    -w /project \
    gradle:8.5-jdk21 \
    ./gradlew build -x test
    echo "Build (testlarsiz) ishga tushirilmoqda. To'liq testlar uchun Java 21 o'rnating."
    docker run --rm \
    -v "$(pwd):/project" \
    -w /project \
    gradle:8.5-jdk21 \
    ./gradlew publish -Pversion=1.0.0-SNAPSHOT
else
  echo "Gradle wrapper topilmadi. Docker orqali wrapper yaratilmoqda va build..."
  docker run --rm -v "$(pwd):/project" -w /project gradle:8.5-jdk21 gradle wrapper --gradle-version 8.5
  chmod +x ./gradlew
  echo "Build (testlarsiz) ishga tushirilmoqda. To'liq testlar uchun Java 21 o'rnating."
  docker run --rm \
    -v "$(pwd):/project" \
    -w /project \
    gradle:8.5-jdk21 \
    ./gradlew build -x test
    echo "Build (testlarsiz) ishga tushirilmoqda. To'liq testlar uchun Java 21 o'rnating."
    docker run --rm \
    -v "$(pwd):/project" \
    -w /project \
    gradle:8.5-jdk21 \
    ./gradlew publish -Pversion=1.0.0-SNAPSHOT
fi
