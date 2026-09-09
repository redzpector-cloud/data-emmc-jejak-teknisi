# Jejak Teknisi - Grade eMMC

Project Android baru khusus database Grade eMMC.

## Perbaikan build
Versi ini menggunakan dependency Material Components sehingga resource `Theme.Material3.DayNight.NoActionBar` tersedia saat AAPT melakukan resource linking.

## Fitur
- Pencarian kode/grade/kapasitas
- Filter grade
- Detail, tambah, edit, hapus
- Database SQLite offline
- Seed data dari tabel Grade eMMC

Build: `./gradlew :app:assembleDebug`
