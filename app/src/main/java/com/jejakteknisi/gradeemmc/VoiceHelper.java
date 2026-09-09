package com.jejakteknisi.gradeemmc;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import java.util.ArrayList;
import java.util.Locale;

/** Lightweight helper for voice search and spoken scan results. */
public class VoiceHelper {
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private float speechRate = 1.0f;

    public interface Listener {
        void onResult(String text);
        void onError(String message);
    }

    public void startListening(Context context, final Listener listener) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Pengenalan suara tidak tersedia di perangkat ini.");
            return;
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(new android.speech.RecognitionListener() {
            public void onReadyForSpeech(android.os.Bundle p) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float r) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() {}
            public void onPartialResults(android.os.Bundle b) {}
            public void onEvent(int t, android.os.Bundle b) {}
            public void onError(int e) { listener.onError("Suara tidak terbaca. Coba lagi."); }
            public void onResults(android.os.Bundle b) {
                ArrayList<String> r = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (r != null && !r.isEmpty()) listener.onResult(r.get(0));
                else listener.onError("Tidak ada hasil suara.");
            }
        });
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID");
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizer.startListening(i);
    }

    public void setRate(float rate) {
        if (rate >= 0.5f && rate <= 2.0f) speechRate = rate;
        if (tts != null) tts.setSpeechRate(speechRate);
    }

    public void speak(Context context, String text) {
        if (tts == null) {
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("id", "ID"));
                    tts.setSpeechRate(speechRate);
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "grade-emmc-result");
                }
            });
        } else {
            tts.setSpeechRate(speechRate);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "grade-emmc-result");
        }
    }

    public void release() {
        if (recognizer != null) { recognizer.destroy(); recognizer = null; }
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
    }
}
