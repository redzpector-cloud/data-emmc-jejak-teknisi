package com.jejakteknisi.gradeemmc;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Kamera manual: pengguna menentukan kapan mengambil foto, lalu ML Kit membaca
 * tulisan pada foto tersebut. Tidak ada OCR otomatis saat kamera bergerak.
 */
public class ScannerActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 3001;
    private PreviewView previewView;
    private TextView status;
    private Button captureButton;
    private ExecutorServiceCompat executor;
    private TextRecognizer recognizer;
    private ImageCapture imageCapture;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = new ExecutorServiceCompat();
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        FrameLayout root = new FrameLayout(this);
        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        root.addView(previewView, new FrameLayout.LayoutParams(-1, -1));

        TextView title = new TextView(this);
        title.setText("SCAN eMMC • FOTO & BACA");
        title.setTextColor(Color.WHITE);
        title.setTextSize(19);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(20, 18, 20, 18);
        title.setBackgroundColor(0xCC07111F);
        FrameLayout.LayoutParams tp = new FrameLayout.LayoutParams(-1, -2);
        tp.gravity = Gravity.TOP;
        root.addView(title, tp);

        status = new TextView(this);
        status.setText("Arahkan kamera ke tulisan pada IC eMMC\nSetelah fokus, tekan tombol FOTO & BACA");
        status.setTextColor(Color.WHITE);
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        status.setPadding(24, 18, 24, 18);
        status.setBackgroundColor(0xCC111827);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1, -2);
        sp.gravity = Gravity.TOP;
        sp.topMargin = 78;
        sp.leftMargin = 18;
        sp.rightMargin = 18;
        root.addView(status, sp);

        TextView guide = new TextView(this);
        guide.setText("┌────────────────────────┐\n│   LETAKKAN TULISAN IC  │\n│       DI DALAM KOTAK   │\n└────────────────────────┘");
        guide.setTextColor(0xFF38BDF8);
        guide.setTextSize(18);
        guide.setGravity(Gravity.CENTER);
        guide.setTypeface(null, android.graphics.Typeface.BOLD);
        FrameLayout.LayoutParams gp = new FrameLayout.LayoutParams(-1, -2);
        gp.gravity = Gravity.CENTER;
        gp.leftMargin = 30;
        gp.rightMargin = 30;
        root.addView(guide, gp);

        captureButton = new Button(this);
        captureButton.setText("📷  FOTO & BACA");
        captureButton.setTextSize(17);
        captureButton.setTextColor(Color.WHITE);
        captureButton.setAllCaps(false);
        captureButton.setBackgroundColor(0xFF078DFF);
        captureButton.setOnClickListener(v -> takePhoto());
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-1, 62);
        bp.gravity = Gravity.BOTTOM;
        bp.leftMargin = 28;
        bp.rightMargin = 28;
        bp.bottomMargin = 78;
        root.addView(captureButton, bp);

        Button close = new Button(this);
        close.setText("Tutup");
        close.setTextColor(Color.WHITE);
        close.setBackgroundColor(0xDD111827);
        close.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-2, -2);
        cp.gravity = Gravity.BOTTOM | Gravity.START;
        cp.bottomMargin = 18;
        cp.leftMargin = 20;
        root.addView(close, cp);

        setContentView(root);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (Exception e) {
                Toast.makeText(this, "Kamera tidak dapat dibuka: " + e.getClass().getSimpleName() + " - " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Kamera belum siap. Tunggu sebentar.", Toast.LENGTH_SHORT).show();
            return;
        }

        captureButton.setEnabled(false);
        captureButton.setText("⏳ MEMPROSES FOTO...");
        status.setText("Foto diambil. Sedang membaca tulisan eMMC...\nJangan tutup aplikasi.");

        String fileName = "emmc_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";
        File photoFile = new File(getCacheDir(), fileName);
        ImageCapture.OutputFileOptions output = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(output, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                readTextFromPhoto(photoFile);
            }

            @Override public void onError(@NonNull ImageCaptureException exception) {
                captureButton.setEnabled(true);
                captureButton.setText("📷  FOTO & BACA");
                status.setText("Foto gagal diambil. Coba lagi dengan posisi dan cahaya yang lebih baik.");
                Toast.makeText(ScannerActivity.this, "Gagal mengambil foto: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void readTextFromPhoto(File photoFile) {
        try {
            InputImage image = InputImage.fromFilePath(this, Uri.fromFile(photoFile));
            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        String candidate = extractCandidate(result.getText());
                        if (candidate != null && candidate.length() >= 3) {
                            status.setText("Terbaca: " + candidate + "\nMencari di database...");
                            Intent out = new Intent();
                            out.putExtra("ocr_text", candidate);
                            setResult(RESULT_OK, out);
                            photoFile.delete();
                            finish();
                        } else {
                            captureButton.setEnabled(true);
                            captureButton.setText("📷  FOTO & BACA LAGI");
                            status.setText("Tulisan belum terbaca.\nDekatkan kamera, pastikan IC fokus, lalu foto lagi.");
                            photoFile.delete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        captureButton.setEnabled(true);
                        captureButton.setText("📷  FOTO & BACA LAGI");
                        status.setText("OCR gagal membaca foto. Coba foto lagi dengan cahaya lebih terang.");
                        photoFile.delete();
                    });
        } catch (Exception e) {
            captureButton.setEnabled(true);
            captureButton.setText("📷  FOTO & BACA LAGI");
            status.setText("Foto tidak dapat diproses. Coba lagi.");
            photoFile.delete();
        }
    }

    private String extractCandidate(String text) {
        if (text == null) return null;
        String cleaned = text.toUpperCase().replaceAll("[^A-Z0-9\\-]", " ").replaceAll("\\s+", " ").trim();
        if (cleaned.isEmpty()) return null;
        String[] parts = cleaned.split(" ");
        String best = null;
        for (String p : parts) {
            if (p.length() < 3 || p.equals("EMMC") || p.equals("SAMSUNG") || p.equals("MICRON") || p.equals("HYNIX") || p.equals("SK")) continue;
            if (best == null || p.length() > best.length()) best = p;
        }
        return best;
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startCamera();
        else { Toast.makeText(this, "Izin kamera diperlukan untuk Scan eMMC", Toast.LENGTH_LONG).show(); finish(); }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) recognizer.close();
        if (executor != null) executor.shutdown();
    }

    // Wrapper kecil agar lifecycle executor tidak perlu dikelola di UI thread.
    private static class ExecutorServiceCompat {
        private final java.util.concurrent.ExecutorService service = java.util.concurrent.Executors.newSingleThreadExecutor();
        void shutdown() { service.shutdown(); }
    }
}
