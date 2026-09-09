# Jejak Teknisi — eMMC Identifier

Project Android baru untuk identifikasi marking eMMC.

## Alur scan
1. Kamera live dengan tap-to-focus.
2. Zoom vertikal + / - dan slider.
3. Tekan FOTO.
4. Foto ditampilkan untuk diperiksa.
5. Tekan BACA TULISAN.
6. Gambar diproses dengan enhancement lokal (grayscale/kontras/scale) dan OCR dijalankan pada foto asli + versi enhanced.
7. Hasil OCR dicocokkan dengan database menggunakan pencocokan karakter.
8. Jika tidak ditemukan, tersedia CARI WEB.

## Data eMMC
- Kode
- Manufacturer
- Kapasitas
- eMMC Version
- **Grade eMMC**
- Package
- Sumber

Grade eMMC sengaja dipertahankan dan dapat diedit. Nilai yang belum tervalidasi ditampilkan sebagai "Belum ditentukan".

## Catatan AI
Versi ini menggunakan enhancement gambar lokal + OCR dan pencocokan database. Ini bukan layanan AI cloud dan tidak mengarang karakter yang tidak terlihat. Integrasi AI cloud dapat ditambahkan sebagai tahap berikutnya jika dibutuhkan.

## Build
GitHub Actions menggunakan JDK 17, Android Gradle Plugin 8.6.1, compileSdk 35, dan Gradle 8.7.


## FIX 4
Perbaikan compile: referensi CameraX menggunakan `androidx.camera.core.Camera` secara eksplisit untuk menghindari bentrok dengan `android.graphics.Camera`. Grade eMMC tetap dipertahankan.
