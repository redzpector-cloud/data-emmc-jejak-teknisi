# GRADE eMMC – JEJAK TEKNISI

Project Android offline, tanpa harga.

## Cara paling mudah membuat APK tanpa Android Studio

1. Buat akun/login GitHub.
2. Buat repository baru, misalnya `grade-emmc-jejak-teknisi`.
3. Upload SEMUA isi folder project ini ke repository tersebut.
   Pastikan file `.github/workflows/build-apk.yml` ikut ter-upload.
4. Buka tab **Actions** pada repository.
5. Pilih workflow **Build APK**.
6. Klik **Run workflow**.
7. Tunggu sampai status selesai.
8. Buka hasil workflow tersebut dan cari bagian **Artifacts**.
9. Download artifact `Grade-eMMC-Jejak-Teknisi`.
10. Di dalam ZIP artifact ada `app-debug.apk`. Pindahkan ke HP dan install.

Workflow otomatis memakai Java 17 dan Gradle 8.7. Tidak membutuhkan Android Studio di komputer Anda.

## Isi aplikasi
- Pencarian kode eMMC
- Grade
- Kapasitas
- Database offline
- 263 kode awal
- Tidak ada harga
- Tidak membutuhkan internet setelah terpasang

## Catatan
Data awal berasal dari gambar daftar eMMC yang diberikan. Karena sebagian tulisan pada gambar kecil/buram, periksa kembali kode yang penting sebelum dijadikan database resmi.
