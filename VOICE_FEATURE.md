# Voice Feature — Grade eMMC

Fitur voice yang ditambahkan ke rancangan project:

1. 🎙️ Voice Search
   - Tekan ikon mikrofon di pencarian.
   - Ucapkan kode eMMC.
   - Hasil speech recognition dimasukkan ke kolom pencarian.
   - Contoh: "KLM8G1WEMB B031".

2. 🔊 Bacakan Hasil
   - Setelah OCR/database/web menghasilkan data, aplikasi dapat membacakan:
     kode eMMC, kapasitas, produsen, dan Grade.

3. 🎙️ Voice Input Tambah/Edit
   - Kolom kode dan keterangan dapat diisi menggunakan suara.

Permission:
RECORD_AUDIO ditambahkan ke AndroidManifest.xml.

Catatan implementasi:
Gunakan Android SpeechRecognizer untuk speech-to-text dan TextToSpeech
untuk membacakan hasil. Kamera/OCR tidak diubah.
