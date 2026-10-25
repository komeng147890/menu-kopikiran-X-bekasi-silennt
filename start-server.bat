@echo off
title KOPIKIRAN X BEKASI SILENT - Localhost Server
cls
echo ======================================================
echo   KOPIKIRAN X BEKASI SILENT - BUKU MENU LOCALHOST
echo ======================================================
echo.

:: Check Node.js
where node >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo [INFO] Menjalankan server menggunakan Node.js...
    echo [INFO] Membuka http://localhost:8000 di browser...
    start http://localhost:8000
    node server.js
    goto end
)

:: Check Python
where python >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo [INFO] Node.js tidak ditemukan, menjalankan dengan Python 3...
    echo [INFO] Membuka http://localhost:8000 di browser...
    start http://localhost:8000
    python -m http.server 8000
    goto end
)

:: Check Py launcher (Windows standard)
where py >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo [INFO] Menjalankan dengan Python Launcher...
    echo [INFO] Membuka http://localhost:8000 di browser...
    start http://localhost:8000
    py -m http.server 8000
    goto end
)

:: Fallback if neither Node nor Python is installed: open index.html directly in browser
echo [PERINGATAN] Node.js atau Python tidak terdeteksi di CMD Anda.
echo Membuka index.html langsung di browser default...
start "" "%~dp0index.html"

:end
pause
