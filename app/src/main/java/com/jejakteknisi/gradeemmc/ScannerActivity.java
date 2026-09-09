package com.jejakteknisi.gradeemmc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
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
 * Manual eMMC camera:
 * camera preview -> user presses FOTO & BACA -> photo -> OCR.
 * No continuous OCR and no target-IC overlay.
 */
public class ScannerActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 3001;

    private PreviewView previewView;
    private TextView status;
    private Button captureButton;
    private Button flashButton;
    private TextView zoomLabel;

    private TextRecognizer recognizer;
    private ImageCapture imageCapture;
    private Camera camera;
    private float zoomRatio = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // IMPORTANT: read preferences only after Activity is attached.
        zoomRatio = getSharedPreferences("settings", MODE_PRIVATE)
                .getFloat("camera_zoom", 1.0f);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        buildCameraUi();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST
            );
        }
    }

    private void buildCameraUi() {
        FrameLayout root = new FrameLayout(this);

        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        root.addView(previewView, new FrameLayout.LayoutParams(-1, -1));

        TextView title = new TextView(this);
        title.setText("Scan eMMC");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(20, 0, 20, 0);
        title.setBackgroundColor(0xCC07111F);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(-1, 64);
        titleParams.gravity = Gravity.TOP;
        root.addView(title, titleParams);

        status = new TextView(this);
        status.setText("Arahkan kamera ke tulisan eMMC.\nTekan FOTO & BACA setelah tulisan terlihat jelas.");
        status.setTextColor(Color.WHITE);
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(16, 12, 16, 12);
        status.setBackgroundColor(0xCC111827);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(-1, -2);
        statusParams.gravity = Gravity.TOP;
        statusParams.topMargin = 72;
        statusParams.leftMargin = 12;
        statusParams.rightMargin = 12;
        root.addView(status, statusParams);

        LinearLayout zoomBar = new LinearLayout(this);
        zoomBar.setOrientation(LinearLayout.HORIZONTAL);
        zoomBar.setGravity(Gravity.CENTER);

        Button zoomOut = new Button(this);
        zoomOut.setText("−");
        zoomOut.setTextSize(22);
        zoomOut.setTextColor(Color.WHITE);
        zoomOut.setBackgroundColor(0xDD111827);

        zoomLabel = new TextView(this);
        zoomLabel.setTextColor(Color.WHITE);
        zoomLabel.setTextSize(14);
        zoomLabel.setGravity(Gravity.CENTER);
        zoomLabel.setBackgroundColor(0xDD111827);
        updateZoomLabel();

        Button zoomIn = new Button(this);
        zoomIn.setText("+");
        zoomIn.setTextSize(22);
        zoomIn.setTextColor(Color.WHITE);
        zoomIn.setBackgroundColor(0xDD111827);

        zoomBar.addView(zoomOut, new LinearLayout.LayoutParams(60, 56));
        zoomBar.addView(zoomLabel, new LinearLayout.LayoutParams(84, 56));
        zoomBar.addView(zoomIn, new LinearLayout.LayoutParams(60, 56));

        FrameLayout.LayoutParams zoomParams = new FrameLayout.LayoutParams(-2, 56);
        zoomParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        zoomParams.bottomMargin = 154;
        root.addView(zoomBar, zoomParams);

        zoomOut.setOnClickListener(v -> changeZoom(-0.5f));
        zoomIn.setOnClickListener(v -> changeZoom(0.5f));

        captureButton = new Button(this);
        captureButton.setText("📷  FOTO & BACA");
        captureButton.setTextSize(17);
        captureButton.setTextColor(Color.WHITE);
        captureButton.setAllCaps(false);
        captureButton.setBackgroundColor(0xFF078DFF);
        captureButton.setOnClickListener(v -> takePhoto());

        FrameLayout.LayoutParams captureParams = new FrameLayout.LayoutParams(-1, 64);
        captureParams.gravity = Gravity.BOTTOM;
        captureParams.leftMargin = 22;
        captureParams.rightMargin = 22;
        captureParams.bottomMargin = 76;
        root.addView(captureButton, captureParams);

        flashButton = new Button(this);
        flashButton.setText("💡 Flash");
        flashButton.setTextSize(13);
        flashButton.setTextColor(Color.WHITE);
        flashButton.setAllCaps(false);
        flashButton.setBackgroundColor(0xDD111827);
        flashButton.setOnClickListener(v -> toggleFlash());

        FrameLayout.LayoutParams flashParams = new FrameLayout.LayoutParams(-2, 52);
        flashParams.gravity = Gravity.BOTTOM | Gravity.END;
        flashParams.rightMargin = 16;
        flashParams.bottomMargin = 16;
        root.addView(flashButton, flashParams);

        Button close = new Button(this);
        close.setText("Tutup");
        close.setTextColor(Color.WHITE);
        close.setAllCaps(false);
        close.setBackgroundColor(0xDD111827);
        close.setOnClickListener(v -> finish());

        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(-2, 52);
        closeParams.gravity = Gravity.BOTTOM | Gravity.START;
        closeParams.leftMargin = 16;
        closeParams.bottomMargin = 16;
        root.addView(close, closeParams);

        setContentView(root);
    }

    private void startCamera() {
        status.setText("Menyiapkan kamera...");

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setJpegQuality(100)
                        .build();

                provider.unbindAll();

                camera = provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );

                float max = camera.getCameraInfo().getZoomState().getValue() != null
                        ? camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio()
                        : 1.0f;

                zoomRatio = Math.max(1.0f, Math.min(zoomRatio, max));
                camera.getCameraControl().setZoomRatio(zoomRatio);
                updateZoomLabel();

                flashButton.setEnabled(camera.getCameraInfo().hasFlashUnit());
                status.setText("Kamera siap.\nArahkan ke tulisan eMMC lalu tekan FOTO & BACA.");
            } catch (Exception e) {
                status.setText("Kamera gagal dibuka.\n" + safeMessage(e));
                Toast.makeText(
                        this,
                        "Kamera gagal dibuka: " + safeMessage(e),
                        Toast.LENGTH_LONG
                ).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void changeZoom(float delta) {
        if (camera == null) {
            Toast.makeText(this, "Kamera belum siap.", Toast.LENGTH_SHORT).show();
            return;
        }

        Float maxValue = camera.getCameraInfo().getZoomState().getValue() == null
                ? 1.0f
                : camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();

        float max = maxValue == null ? 1.0f : maxValue;
        zoomRatio = Math.max(1.0f, Math.min(max, zoomRatio + delta));
        camera.getCameraControl().setZoomRatio(zoomRatio);
        updateZoomLabel();

        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putFloat("camera_zoom", zoomRatio)
                .apply();
    }

    private void updateZoomLabel() {
        if (zoomLabel != null) {
            zoomLabel.setText(String.format(Locale.US, "%.1fx", zoomRatio));
        }
    }

    private void toggleFlash() {
        if (camera == null || !camera.getCameraInfo().hasFlashUnit()) {
            Toast.makeText(this, "Flash tidak tersedia.", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer state = camera.getCameraInfo().getTorchState().getValue();
        boolean isOn = state != null && state == TorchState.ON;
        camera.getCameraControl().enableTorch(!isOn);
        flashButton.setText(isOn ? "💡 Flash" : "💡 Flash ON");
    }

    private void takePhoto() {
        if (camera == null || imageCapture == null) {
            Toast.makeText(this, "Kamera belum siap. Tunggu sebentar.", Toast.LENGTH_SHORT).show();
            return;
        }

        captureButton.setEnabled(false);
        captureButton.setText("⏳ MEMPROSES...");
        status.setText("Mengambil foto...\nJangan gerakkan kamera.");

        String fileName = "emmc_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) +
                ".jpg";

        File photoFile = new File(getCacheDir(), fileName);

        ImageCapture.OutputFileOptions output =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                output,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {
                        readTextFromPhoto(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        resetCaptureButton("📷  FOTO & BACA LAGI");
                        status.setText("Foto gagal.\nCoba lagi dengan fokus dan cahaya lebih baik.");
                        Toast.makeText(
                                ScannerActivity.this,
                                "Gagal mengambil foto: " + safeMessage(exception),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void readTextFromPhoto(File photoFile) {
        try {
            InputImage image = InputImage.fromFilePath(this, Uri.fromFile(photoFile));

            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        String candidate = extractCandidate(result.getText());

                        if (candidate != null && candidate.length() >= 3) {
                            Intent out = new Intent();
                            out.putExtra("ocr_text", candidate);
                            setResult(RESULT_OK, out);
                            photoFile.delete();
                            finish();
                        } else {
                            resetCaptureButton("📷  FOTO & BACA LAGI");
                            status.setText(
                                    "Tulisan belum terbaca.\n" +
                                    "Coba zoom, dekatkan kamera, lalu foto lagi."
                            );
                            photoFile.delete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        resetCaptureButton("📷  FOTO & BACA LAGI");
                        status.setText("OCR gagal.\nCoba foto lagi dengan tulisan lebih jelas.");
                        photoFile.delete();
                    });
        } catch (Exception e) {
            resetCaptureButton("📷  FOTO & BACA LAGI");
            status.setText("Foto tidak dapat diproses.\nCoba lagi.");
            photoFile.delete();
        }
    }

    private void resetCaptureButton(String label) {
        captureButton.setEnabled(true);
        captureButton.setText(label);
    }

    private String extractCandidate(String text) {
        if (text == null) return null;

        String cleaned = text.toUpperCase(Locale.US)
                .replaceAll("[^A-Z0-9\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) return null;

        String best = null;
        for (String part : cleaned.split(" ")) {
            if (part.length() < 3) continue;
            if (part.equals("EMMC") ||
                    part.equals("SAMSUNG") ||
                    part.equals("MICRON") ||
                    part.equals("HYNIX") ||
                    part.equals("SK")) {
                continue;
            }
            if (best == null || part.length() > best.length()) {
                best = part;
            }
        }
        return best;
    }

    private String safeMessage(Exception e) {
        String msg = e.getMessage();
        return msg == null || msg.trim().isEmpty()
                ? e.getClass().getSimpleName()
                : msg;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(
                        this,
                        "Izin kamera diperlukan untuk Scan eMMC.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.close();
        super.onDestroy();
    }
}
