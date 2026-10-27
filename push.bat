@echo off
setlocal enabledelayedexpansion
title NEXUS ORBIT - GitHub Push Utility
color 0B

echo ===================================================================
echo               NEXUS ORBIT - GITHUB SYNC AND PUSH TOOL
echo ===================================================================
echo Target Repo: https://github.com/Duckierpine3108/Nexus-Orbit
echo ===================================================================
echo.

:: 1. Check if Git is installed
where git >nul 2>nul
if %errorlevel% neq 0 (
    color 0C
    echo [ERROR] Git is not installed or not found in system PATH.
    echo Please install Git from https://git-scm.com/ and rerun this script.
    echo.
    pause
    exit /b 1
)

set REPO_URL=https://github.com/Duckierpine3108/Nexus-Orbit.git

:: 2. Initialize Git repo if .git doesn't exist yet
if not exist ".git" (
    echo [*] Local repository not detected. Initializing Git...
    git init
    git branch -M main
    git remote add origin %REPO_URL%
    echo [+] Remote origin configured to: %REPO_URL%
) else (
    git remote get-url origin >nul 2>nul
    if %errorlevel% neq 0 (
        git remote add origin %REPO_URL%
    ) else (
        git remote set-url origin %REPO_URL%
    )
)

:: 3. Warn if avatar is missing before pushing
if not exist "app\src\main\assets\models\nexus_avatar.glb" (
    color 0E
    echo [WARNING] nexus_avatar.glb was not found at:
    echo           app\src\main\assets\models\nexus_avatar.glb
    echo           (You can still push now and add it later)
    color 0B
    echo.
)

:: 4. Prompt for Commit Message
echo -------------------------------------------------------------------
set /p COMMIT_MSG="Enter commit message (Press Enter for default: 'Update NEXUS ORBIT'): "
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Update NEXUS ORBIT - %date% %time%
echo -------------------------------------------------------------------
echo.

:: 5. Stage files
echo [*] Staging changed files...
git add .

:: 6. Commit
echo [*] Committing with message: "%COMMIT_MSG%"
git commit -m "%COMMIT_MSG%"

:: 7. Push to GitHub
echo [*] Pushing to GitHub (origin/main)...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    color 0A
    echo ===================================================================
    echo [SUCCESS] Your code is live on GitHub!
    echo Visit: https://github.com/Duckierpine3108/Nexus-Orbit
    echo ===================================================================
) else (
    echo.
    color 0C
    echo ===================================================================
    echo [PUSH FAILED]
    echo Common reasons:
    echo  1. If GitHub rejected the push because the remote has commits:
    echo     Type 1 to run: git pull --rebase origin main and retry
    echo     Type 2 to Force Push (overwrites remote repository)
    echo     Type 3 to Exit
    echo ===================================================================
    set /p CHOICE="Selection (1, 2, or 3): "
    if "!CHOICE!"=="1" (
        git pull --rebase origin main
        git push -u origin main
    ) else if "!CHOICE!"=="2" (
        git push -u origin main --force
    )
)

echo.
pause