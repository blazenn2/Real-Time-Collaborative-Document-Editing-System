@echo off
REM Set Docker Host for Podman
set DOCKER_HOST=npipe:////./pipe/docker_engine

REM Change to your compose directory
cd /d "D:\Practice projects\Real-Time-Collaborative-Document-Editing-System\src\main\docker"

REM Run Compose
docker-compose up -d

echo Done.
pause
