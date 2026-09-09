package com.jejakteknisi.gradeemmc;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScannerActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 3001;
    private PreviewView previewView;
    private TextView status;
    private ExecutorService executor;
    private TextRecognizer recognizer;
    private final AtomicBoolean locked = new AtomicBoolean(false);

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        FrameLayout root = new FrameLayout(this);
        previewView = new PreviewView(this);
        root.addView(previewView, new FrameLayout.LayoutParams(-1,-1));

        status = new TextView(this);
        status.setText("Arahkan kamera ke tulisan eMMC\nGunakan cahaya cukup dan fokus dekat");
        status.setTextColor(Color.WHITE);
        status.setTextSize(15);
        status.setGravity(Gravity.CENTER);
        status.setPadding(24,18,24,18);
        status.setBackgroundColor(0xAA111827);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1,-2);
        sp.gravity = Gravity.TOP; sp.topMargin = 40; sp.leftMargin = 20; sp.rightMargin = 20;
        root.addView(status, sp);

        Button close = new Button(this);
        close.setText("Tutup"); close.setTextColor(Color.WHITE); close.setBackgroundColor(0xDD111827);
        close.setOnClickListener(v -> finish());
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-2,-2);
        cp.gravity = Gravity.BOTTOM|Gravity.START; cp.bottomMargin=25; cp.leftMargin=20;
        root.addView(close, cp);

        setContentView(root);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera();
        else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(executor, imageProxy -> {
                    if (locked.get()) { imageProxy.close(); return; }
                    if (imageProxy.getImage() == null) { imageProxy.close(); return; }
                    InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                    recognizer.process(image)
                            .addOnSuccessListener(result -> {
                                String candidate = extractCandidate(result.getText());
                                if (candidate != null && candidate.length() >= 3) {
                                    locked.set(true);
                                    runOnUiThread(() -> status.setText("Terbaca: " + candidate + "\nMencari di database..."));
                                    Intent out = new Intent(); out.putExtra("ocr_text", candidate);
                                    setResult(RESULT_OK, out); finish();
                                }
                            })
                            .addOnFailureListener(e -> runOnUiThread(() -> status.setText("OCR gagal membaca. Coba lebih dekat / terang.")))
                            .addOnCompleteListener(t -> imageProxy.close());
                });
                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
            } catch (Exception e) {
                Toast.makeText(this, "Kamera tidak dapat dibuka", Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
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
}
