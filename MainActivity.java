package com.rs.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_SPEECH = 101;
    private static final int REQ_PERMS = 102;
    private TextView status;
    private TextToSpeech tts;
    private String pendingAction = "";
    private String pendingNumber = "";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("hi", "IN"));
        });
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS}, REQ_PERMS);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 44, 28, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFF101820);

        TextView title = new TextView(this);
        title.setText("RS");
        title.setTextSize(38);
        title.setTextColor(0xFF66CCFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("Tumhara Personal Voice Assistant");
        subtitle.setTextColor(0xFFFFFFFF);
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        status = new TextView(this);
        status.setText("Namaste! Mic dabao aur bolo.");
        status.setTextColor(0xFFFFFFFF);
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 32, 0, 32);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        Button listen = new Button(this);
        listen.setText("🎙 Bolo RS");
        listen.setTextSize(20);
        listen.setOnClickListener(v -> listen());
        root.addView(listen, new LinearLayout.LayoutParams(-1, -2));

        TextView examples = new TextView(this);
        examples.setText("\nTry karo:\n• Rahul ko call karo\n• WhatsApp kholo\n• Time kya hua hai\n• Bhajan chalao\n• Google kholo\n\nNote: Call aur message bhejne se pehle confirm karna zaroori hai.");
        examples.setTextColor(0xFFCFD8DC);
        examples.setTextSize(15);
        root.addView(examples, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private void listen() {
        try {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "RS ko command bolo");
            startActivityForResult(i, REQ_SPEECH);
        } catch (ActivityNotFoundException e) {
            say("Is phone par speech recognition available nahi hai.");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) handleCommand(results.get(0).toLowerCase(Locale.ROOT));
        }
    }

    private void handleCommand(String raw) {
        status.setText("Suna: " + raw);
        String s = raw.replace("कॉल", "call").replace("को", " ko ")
                .replace("खोलो", "kholo").replace("समय", "time").trim();

        if (s.contains("time") || s.contains("samay") || s.contains("वक्त")) {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault());
            say("Abhi time hai " + f.format(new java.util.Date()));
        } else if (s.contains("whatsapp")) {
            openApp("com.whatsapp", "WhatsApp nahi mila.");
        } else if (s.contains("youtube") || s.contains("यूट्यूब")) {
            openApp("com.google.android.youtube", "YouTube nahi mila.");
        } else if (s.contains("google") || s.contains("search") || s.contains("सर्च")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            startActivity(i);
            say("Google khol raha hoon.");
        } else if (s.contains("bhajan") || s.contains("gaana") || s.contains("song") || s.contains("भजन")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=bhajan"));
            startActivity(i);
            say("Bhajan search kar raha hoon.");
        } else if (s.contains("call") || s.contains("phone") || s.contains("फोन")) {
            say("Call ke liye contact ka number phone ke contacts mein select karke confirm karna hoga. Is demo mein contact matching abhi jodi nahi gayi hai.");
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));
            startActivity(i);
        } else if (s.contains("message") || s.contains("msg") || s.contains("मैसेज")) {
            say("Message bhejne se pehle recipient aur message confirm karna zaroori hai. Is demo mein auto-send enabled nahi hai.");
        } else {
            say("Maaf karo, ye command abhi samajh nahi aayi. WhatsApp kholo, Google kholo, ya time kya hua hai bolo.");
        }
    }

    private void openApp(String packageName, String failure) {
        Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
        if (i != null) {
            startActivity(i);
            say("App khol raha hoon.");
        } else {
            say(failure);
        }
    }

    private void say(String message) {
        status.setText(message);
        if (tts != null) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "RS_UTTERANCE");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
