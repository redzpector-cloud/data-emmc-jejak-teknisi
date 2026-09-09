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
        root.setBackgroundColor(Color.BLACK);

        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        root.addView(previewView, new FrameLayout.LayoutParams(-1, -1));

        // TOP BAR
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(8, 10, 8, 10);
        topBar.setBackgroundColor(0xDD07111F);

        Button close = new Button(this);
        close.setText("‹");
        close.setTextSize(32);
        close.setTextColor(Color.WHITE);
        close.setAllCaps(false);
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setOnClickListener(v -> finish());
        topBar.addView(close, new LinearLayout.LayoutParams(58, 58));

        TextView title = new TextView(this);
        title.setText("Scan eMMC");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        topBar.addView(title, new LinearLayout.LayoutParams(0, 58, 1));

        flashButton = new Button(this);
        flashButton.setText("💡");
        flashButton.setTextSize(21);
        flashButton.setTextColor(Color.WHITE);
        flashButton.setAllCaps(false);
        flashButton.setBackgroundColor(0xAA17263A);
        flashButton.setOnClickListener(v -> toggleFlash());
        topBar.addView(flashButton, new LinearLayout.LayoutParams(58, 58));

        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(-1, 78);
        topParams.gravity = Gravity.TOP;
        root.addView(topBar, topParams);

        status = new TextView(this);
        status.setText("Arahkan kamera ke tulisan eMMC");
        status.setTextColor(Color.WHITE);
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        status.setPadding(16, 8, 16, 8);
        status.setBackgroundColor(0xAA07111F);
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(-2, -2);
        statusParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        statusParams.topMargin = 80;
        root.addView(status, statusParams);

        // RIGHT-SIDE ZOOM: 0.5x steps, plus quick presets.
        LinearLayout zoomBar = new LinearLayout(this);
        zoomBar.setOrientation(LinearLayout.VERTICAL);
        zoomBar.setGravity(Gravity.CENTER);
        zoomBar.setPadding(6, 8, 6, 8);
        zoomBar.setBackgroundColor(0xCC101C2C);

        Button zoomIn = cameraButton("+");
        Button zoomOut = cameraButton("−");
        zoomLabel = new TextView(this);
        zoomLabel.setTextColor(Color.WHITE);
        zoomLabel.setTextSize(13);
        zoomLabel.setGravity(Gravity.CENTER);
        zoomLabel.setPadding(2, 4, 2, 4);
        updateZoomLabel();

        Button zoom05 = cameraButton("0.5×");
        Button zoom1 = cameraButton("1×");
        Button zoom2 = cameraButton("2×");
        Button zoom3 = cameraButton("3×");
        Button zoom4 = cameraButton("4×");

        zoomBar.addView(zoomIn, new LinearLayout.LayoutParams(76, 64));
        zoomBar.addView(zoomLabel, new LinearLayout.LayoutParams(76, 44));
        zoomBar.addView(zoomOut, new LinearLayout.LayoutParams(76, 64));
        zoomBar.addView(zoom05, new LinearLayout.LayoutParams(76, 52));
        zoomBar.addView(zoom1, new LinearLayout.LayoutParams(76, 52));
        zoomBar.addView(zoom2, new LinearLayout.LayoutParams(76, 52));
        zoomBar.addView(zoom3, new LinearLayout.LayoutParams(76, 52));
        zoomBar.addView(zoom4, new LinearLayout.LayoutParams(76, 52));

        FrameLayout.LayoutParams zoomParams = new FrameLayout.LayoutParams(90, 500);
        zoomParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        zoomParams.rightMargin = 12;
        root.addView(zoomBar, zoomParams);

        zoomOut.setOnClickListener(v -> changeZoom(-0.5f));
        zoomIn.setOnClickListener(v -> changeZoom(0.5f));
        zoom05.setOnClickListener(v -> setZoomPreset(0.5f));
        zoom1.setOnClickListener(v -> setZoomPreset(1.0f));
        zoom2.setOnClickListener(v -> setZoomPreset(2.0f));
        zoom3.setOnClickListener(v -> setZoomPreset(3.0f));
        zoom4.setOnClickListener(v -> setZoomPreset(4.0f));

        // BOTTOM CAMERA BUTTON: use a large Button with a solid background and high z-order.
        captureButton = new Button(this);
        captureButton.setText("📷  FOTO & BACA");
        captureButton.setTextSize(18);
        captureButton.setTextColor(Color.WHITE);
        captureButton.setAllCaps(false);
        captureButton.setGravity(Gravity.CENTER);
        captureButton.setBackgroundColor(0xFF078DFF);
        captureButton.setPadding(12, 0, 12, 0);
        captureButton.setMinHeight(86);
        captureButton.setOnClickListener(v -> takePhoto());

        FrameLayout.LayoutParams captureParams = new FrameLayout.LayoutParams(-1, 86);
        captureParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        captureParams.leftMargin = 24;
        captureParams.rightMargin = 24;
        captureParams.bottomMargin = 92;
        root.addView(captureButton, captureParams);

        TextView hint = new TextView(this);
        hint.setText("Tekan FOTO & BACA setelah tulisan eMMC fokus");
        hint.setTextColor(0xEEFFFFFF);
        hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(-1, 28);
        hintParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        hintParams.leftMargin = 70;
        hintParams.rightMargin = 70;
        hintParams.bottomMargin = 184;
        root.addView(hint, hintParams);

        setContentView(root);
    }

    private Button cameraButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundColor(0xFF1B314A);
        b.setPadding(0, 0, 0, 0);
        return b;
    }

    private void setZoomPreset(float value) {
        if (camera == null) {
            Toast.makeText(this, "Kamera belum siap.", Toast.LENGTH_SHORT).show();
            return;
        }
        Float maxValue = camera.getCameraInfo().getZoomState().getValue() == null
                ? 1.0f
                : camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio();
        float max = maxValue == null ? 1.0f : maxValue;
        zoomRatio = Math.max(1.0f, Math.min(max, value));
        camera.getCameraControl().setZoomRatio(zoomRatio);
        updateZoomLabel();
        getSharedPreferences("settings", MODE_PRIVATE).edit()
                .putFloat("camera_zoom", zoomRatio).apply();
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
