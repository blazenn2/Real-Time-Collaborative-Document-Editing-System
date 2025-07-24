@echo off
setlocal
set CONTAINER_NAME=redis-server
set IMAGE_NAME=redis:latest

REM Check if Podman is available
where podman >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Podman CLI is not installed or not in PATH.
    pause
    exit /b 1
)

REM Start podman machine if not already running
echo Checking Podman machine status...
podman machine info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Podman machine not running. Starting...
    podman machine start
    if %ERRORLEVEL% NEQ 0 (
        echo Failed to start Podman machine.
        pause
        exit /b 1
    )
)

REM Check if container exists
echo Checking for existing container named "%CONTAINER_NAME%"...
podman container exists %CONTAINER_NAME%
IF %ERRORLEVEL% EQU 0 (
    echo Container "%CONTAINER_NAME%" already exists.
    echo Starting existing container...
    podman start %CONTAINER_NAME%
) ELSE (
    echo Container "%CONTAINER_NAME%" does not exist.
    echo Pulling Redis image: %IMAGE_NAME% ...
    podman pull %IMAGE_NAME%
    echo Running new Redis container...
    podman run -d --name %CONTAINER_NAME% -p 6379:6379 %IMAGE_NAME%
)

echo.
echo Container status:
podman ps -a --filter name=%CONTAINER_NAME%
echo.

pause
