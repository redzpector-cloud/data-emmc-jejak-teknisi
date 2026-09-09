package com.jejakteknisi.gradeemmc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    private WebView web;
    private static final int SCAN_REQUEST = 2001;
    private static final int VOICE_REQUEST = 2002;
    private VoiceHelper voiceHelper;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(false);
        web.addJavascriptInterface(new AndroidBridge(), "Android");
        web.loadUrl("file:///android_asset/index.html");
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        voiceHelper = new VoiceHelper();
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void startScanner() {
            runOnUiThread(() -> startActivityForResult(new Intent(MainActivity.this, ScannerActivity.class), SCAN_REQUEST));
        }

        @JavascriptInterface
        public void startVoiceSearch() {
            runOnUiThread(() -> {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO}, VOICE_REQUEST);
                    return;
                }
                listenForVoice();
            });
        }

        @JavascriptInterface
        public void setVoiceRate(float rate) {
            getSharedPreferences("settings", MODE_PRIVATE).edit().putFloat("voice_rate", rate).apply();
            if (voiceHelper != null) voiceHelper.setRate(rate);
        }

        @JavascriptInterface
        public void setCameraZoom(float zoom) {
            getSharedPreferences("settings", MODE_PRIVATE).edit().putFloat("camera_zoom", zoom).apply();
        }

        @JavascriptInterface
        public void openWebSearch(String url) {
            runOnUiThread(() -> {
                try { startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))); }
                catch (Exception e) { Toast.makeText(MainActivity.this, "Browser tidak tersedia", Toast.LENGTH_SHORT).show(); }
            });
        }

        @JavascriptInterface
        public void speakResult(String text) {
            runOnUiThread(() -> {
                if (voiceHelper != null && text != null && !text.trim().isEmpty()) {
                    voiceHelper.speak(MainActivity.this, text);
                }
            });
        }
    }

    
    private void listenForVoice() {
        voiceHelper.startListening(this, new VoiceHelper.Listener() {
            @Override public void onResult(String text) {
                String safe = text == null ? "" : text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("\r", " ");
                web.evaluateJavascript("setVoiceResult('" + safe + "');", null);
                Toast.makeText(MainActivity.this, "Suara: " + text, Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VOICE_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            listenForVoice();
        }
    }

    @Override protected void onDestroy() {
        if (voiceHelper != null) voiceHelper.release();
        super.onDestroy();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST && resultCode == RESULT_OK && data != null) {
            String text = data.getStringExtra("ocr_text");
            if (text == null || text.trim().isEmpty()) return;
            String safe = text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
            web.evaluateJavascript("showOcrResult('" + safe + "');", null);
            Toast.makeText(this, "Tulisan terdeteksi. Pilih teks lalu Copy atau Cari.", Toast.LENGTH_SHORT).show();
        }
    }
}
