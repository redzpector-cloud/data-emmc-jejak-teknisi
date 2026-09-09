# Jejak Teknisi — Grade eMMC

Project Android baru untuk:
- Kamera seperti Google Lens
- Tap-to-focus pada tulisan eMMC
- Autofocus
- Zoom vertikal
- Flash
- Foto manual
- OCR setelah foto
- Edit/pilih teks
- Copy
- Cek database lokal
- Jika tidak ada, buka pencarian web
- Database awal eMMC

## Build
Gunakan Gradle 8.7 dengan JDK 17.
Perintah:
`./gradlew :app:assembleDebug --no-daemon`

## GitHub Actions
Workflow contoh ada di `.github/workflows/android.yml`.


## Fokus fitur versi ini
Alur scan dibuat manual seperti Google Lens:
Foto → fokus/crop tulisan → OCR → koreksi → cocokkan database → tampilkan kapasitas + **Grade eMMC**.
Grade tidak dihapus dan dapat diedit dari hasil pencarian atau menu database.
Logo Jejak Teknisi sudah dipasang di beranda.
