package com.jejakteknisi.gradeemmc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView web;
    private static final int SCAN_REQUEST = 2001;

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
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void startScanner() {
            runOnUiThread(() -> startActivityForResult(new Intent(MainActivity.this, ScannerActivity.class), SCAN_REQUEST));
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST && resultCode == RESULT_OK && data != null) {
            String text = data.getStringExtra("ocr_text");
            if (text == null || text.trim().isEmpty()) return;
            String safe = text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ").replace("\r", " ");
            web.evaluateJavascript("setScanResult('" + safe + "');", null);
            Toast.makeText(this, "Hasil OCR: " + text, Toast.LENGTH_SHORT).show();
        }
    }
}
