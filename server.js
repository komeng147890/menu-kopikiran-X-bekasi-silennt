// Minimalist Zero-Dependency Localhost Server for Kopikiran X Bekasi Silent
// Run via CMD: node server.js

const http = require('http');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const PORT = process.env.PORT || 8000;
const ROOT_DIR = __dirname;

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.htm': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.webp': 'image/webp',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf'
};

const server = http.createServer((req, res) => {
  // Parse URL & prevent path traversal
  const safeUrl = decodeURI(req.url.split('?')[0]);
  let filePath = path.join(ROOT_DIR, safeUrl === '/' ? 'index.html' : safeUrl);

  // Security: prevent directory traversal outside root
  if (!filePath.startsWith(ROOT_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end('403 Forbidden');
    return;
  }

  // Check if file exists
  fs.stat(filePath, (err, stats) => {
    if (err) {
      // Fallback: if not found, serve index.html for SPA routes
      const fallbackPath = path.join(ROOT_DIR, 'index.html');
      fs.readFile(fallbackPath, (fallbackErr, data) => {
        if (fallbackErr) {
          res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
          res.end('404 Not Found: index.html tidak ditemukan');
        } else {
          res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
          res.end(data);
        }
      });
      return;
    }

    if (stats.isDirectory()) {
      filePath = path.join(filePath, 'index.html');
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.readFile(filePath, (readErr, content) => {
      if (readErr) {
        res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end(`500 Internal Server Error: ${readErr.message}`);
      } else {
        res.writeHead(200, {
          'Content-Type': contentType,
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Access-Control-Allow-Origin': '*'
        });
        res.end(content);
      }
    });
  });
});

server.listen(PORT, '0.0.0.0', () => {
  const url = `http://localhost:${PORT}`;
  console.log('\n======================================================');
  console.log('  KOPIKIRAN X BEKASI SILENT - LOCALHOST SERVER');
  console.log('======================================================');
  console.log(`\n  ✅ Server berhasil berjalan!`);
  console.log(`  🌐 Buka di browser: \x1b[36m${url}\x1b[0m`);
  console.log(`  📄 File yang dibuka: ${path.join(ROOT_DIR, 'index.html')}`);
  console.log(`\n  Tekan Ctrl + C di CMD untuk mematikan server.\n`);
  console.log('======================================================\n');

  // Attempt to open browser automatically
  const startCmd = process.platform === 'win32' ? `start ${url}` :
                   process.platform === 'darwin' ? `open ${url}` :
                   `xdg-open ${url}`;
  exec(startCmd, () => {
    // Ignore error if browser launch fails in headless environments
  });
});

process.on('SIGINT', () => {
  console.log('\nServer dimatikan. Sampai jumpa!');
  process.exit(0);
});
