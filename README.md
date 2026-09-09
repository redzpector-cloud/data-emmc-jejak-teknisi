# Jejak Teknisi — Grade eMMC + Schematic

Project Android source baru karena source project lama tidak tersedia. Fitur versi 1:
1. Grade eMMC
2. Pencarian kode/grade/kapasitas
3. Scan OCR kamera (ML Kit)
4. Filter kategori grade
5. Tambah/Edit data (hapus tersedia; edit dapat dikembangkan)
6. Database SQLite offline
7. Modul Schematic: pencarian model/judul dan database schematic dasar

Catatan: APK referensi tidak menyertakan source code. Database 263 kode yang pernah terlihat pada APK tidak diekstrak otomatis dalam project ini; beberapa seed contoh disediakan agar aplikasi langsung dapat dibangun. Data asli dapat dimasukkan melalui fitur tambah atau migrasi database pada tahap berikutnya.

Build: buka folder ini di Android Studio, tunggu Gradle sync, lalu Build > Make Project / Run.
