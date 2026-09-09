package com.jejakteknisi.gradeemmc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Opens a web search for OCR-selected eMMC text.
 * The app should call this only after checking the local database.
 */
public final class WebSearchHelper {
    private WebSearchHelper() {}

    public static void search(Context context, String query) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) return;
        String url = "https://www.google.com/search?q=" + Uri.encode(q);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
