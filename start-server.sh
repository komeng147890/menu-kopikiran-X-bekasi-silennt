#!/usr/bin/env bash
# KOPIKIRAN X BEKASI SILENT - Localhost Server Starter
cd "$(dirname "$0")"

echo "======================================================"
echo "  KOPIKIRAN X BEKASI SILENT - BUKU MENU LOCALHOST"
echo "======================================================"
echo ""

if command -v node >/dev/null 2>&1; then
    echo "[INFO] Menjalankan server menggunakan Node.js..."
    node server.js
elif command -v python3 >/dev/null 2>&1; then
    echo "[INFO] Menjalankan server menggunakan Python 3..."
    echo "Buka http://localhost:8000 di browser"
    python3 -m http.server 8000
elif command -v python >/dev/null 2>&1; then
    echo "[INFO] Menjalankan server menggunakan Python..."
    echo "Buka http://localhost:8000 di browser"
    python -m http.server 8000
else
    echo "[PERINGATAN] Node.js atau Python tidak ditemukan."
    echo "Membuka file index.html langsung di browser..."
    if command -v xdg-open >/dev/null 2>&1; then
        xdg-open index.html
    elif command -v open >/dev/null 2>&1; then
        open index.html
    fi
fi
