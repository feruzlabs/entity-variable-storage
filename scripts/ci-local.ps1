# CI bilan bir xil muhitda build — GitHub Actions dan oldin lokal tekshirish
# Windows: Docker Desktop ishlashi kerak; WSL da unix socket ishlatish mumkin
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

$env:TESTCONTAINERS_RYUK_DISABLED = "true"
# WSL yoki Git Bash da Linux-style socket kerak bo'lsa: $env:DOCKER_HOST = "unix:///var/run/docker.sock"

Write-Host "Docker tekshiruvi..."
docker info
docker pull postgres:16-alpine

Write-Host "Gradle build..."
if (Test-Path ".\gradlew.bat") { .\gradlew.bat build } else { gradle build }
