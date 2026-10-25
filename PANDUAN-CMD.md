# Cara Menjalankan Menu di Localhost via CMD (Command Prompt)

Aplikasi buku menu **KOPIKIRAN X BEKASI SILENT** menggunakan file `index.html` yang sama persis (8 menu resmi, fitur pemesanan WhatsApp, struk digital, QRIS, pencarian, dan filter).

Berikut beberapa pilihan cara menjalankan melalui **CMD (Command Prompt Windows)**:

---

### Cara 1: Menggunakan File Batch Otomatis (Paling Mudah)
1. Buka folder proyek ini di Windows Explorer.
2. Cukup **dobel-klik file `start-server.bat`** (atau ketik `start-server.bat` di jendela CMD).
3. Browser akan otomatis terbuka dan menampilkan:
   ```
   http://localhost:8000
   ```

---

### Cara 2: Menggunakan Node.js (CMD)
Jika di komputer Anda sudah terpasang **Node.js**:
1. Buka CMD di folder proyek ini.
2. Jalankan perintah:
   ```cmd
   node server.js
   ```
   *atau bisa juga:*
   ```cmd
   npm start
   ```
3. Buka browser Anda (Chrome/Edge/Firefox) ke alamat:
   ```
   http://localhost:8000
   ```

---

### Cara 3: Menggunakan Python (CMD)
Jika di komputer Anda sudah terpasang **Python**:
1. Buka CMD di folder proyek ini.
2. Jalankan perintah:
   ```cmd
   python -m http.server 8000
   ```
   *(atau `py -m http.server 8000`)*
3. Buka browser Anda ke:
   ```
   http://localhost:8000
   ```

---

### Cara 4: Tanpa Web Server (Langsung di Browser)
Jika belum ada Node.js maupun Python, di CMD ketik:
```cmd
start index.html
```
File akan langsung terbuka di browser Anda dengan tampilan yang 100% sama persis dan berfungsi normal.

---

### Cara Menghentikan Server di CMD:
Tekan tombol **`Ctrl + C`** di jendela CMD Anda.
