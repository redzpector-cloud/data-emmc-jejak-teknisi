package com.jejakteknisi.gradeemmc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerActivity extends AppCompatActivity {
    private static final int REQ_CAMERA = 10;
    private PreviewView preview;
    private TextView result;
    private ImageCapture capture;
    private ExecutorService executor;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_scanner);
        preview = findViewById(R.id.preview);
        result = findViewById(R.id.result);
        Button scan = findViewById(R.id.scan);
        executor = Executors.newSingleThreadExecutor();
        scan.setOnClickListener(v -> takePhoto());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) startCamera();
        else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview p = new Preview.Builder().build();
                p.setSurfaceProvider(preview.getSurfaceProvider());
                capture = new ImageCapture.Builder().setJpegQuality(95).build();
                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, p, capture);
            } catch (Exception e) {
                result.setText("Kamera gagal: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (capture == null) { Toast.makeText(this, "Kamera belum siap", Toast.LENGTH_SHORT).show(); return; }
        capture.takePicture(ContextCompat.getMainExecutor(this),
            new ImageCapture.OnImageCapturedCallback() {
                @Override public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
                    Bitmap bitmap = preview.getBitmap();
                    image.close();
                    if (bitmap == null) { result.setText("Gagal mengambil gambar"); return; }
                    InputImage input = InputImage.fromBitmap(bitmap, 0);
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        .process(input)
                        .addOnSuccessListener(text -> result.setText("Hasil OCR:\n" + text.getText()))
                        .addOnFailureListener(e -> result.setText("OCR gagal: " + e.getMessage()));
                }
                @Override public void onError(@NonNull ImageCaptureException e) {
                    result.setText("Kamera: " + e.getMessage());
                }
            });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_CAMERA && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) startCamera();
        else result.setText("Izin kamera diperlukan untuk Scan OCR.");
    }
}
