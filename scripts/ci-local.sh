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
if [ -x "./gradlew" ]; then
  ./gradlew build
elif command -v gradle &>/dev/null; then
  gradle build
else
  echo "Gradle topilmadi. Docker orqali wrapper yaratilmoqda..."
  docker run --rm -v "$(pwd):/project" -w /project gradle:8.5-jdk21 gradle wrapper --gradle-version 8.5
  chmod +x ./gradlew
  ./gradlew build
fi
