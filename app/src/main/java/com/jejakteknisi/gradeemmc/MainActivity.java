package com.jejakteknisi.gradeemmc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Button;
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
        web.loadUrl("file:///android_asset/index.html");
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));

        Button scan = new Button(this);
        scan.setText("📷 SCAN eMMC");
        scan.setTextColor(Color.WHITE);
        scan.setTextSize(13);
        scan.setAllCaps(false);
        scan.setBackgroundColor(Color.rgb(17,24,39));
        scan.setOnClickListener(v -> startActivityForResult(new Intent(this, ScannerActivity.class), SCAN_REQUEST));
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-2, -2);
        bp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        bp.bottomMargin = 18;
        root.addView(scan, bp);
        setContentView(root);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_REQUEST && resultCode == RESULT_OK && data != null) {
            String text = data.getStringExtra("ocr_text");
            if (text == null) return;
            String safe = text.replace("\\", "\\\\").replace("'", "\\'");
            web.evaluateJavascript("(function(){var q=document.getElementById('q'); if(q){q.value='" + safe + "'; search(); q.scrollIntoView();}})();", null);
            Toast.makeText(this, "Hasil OCR: " + text, Toast.LENGTH_LONG).show();
        }
    }
}
