package com.example.mpv4;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConfig {

    public static final String API_KEY = "AIzaSyBJJs47pTjnZiNHNC5Ez34q-bH0lfuqPf4";
    public static final String APPLICATION_ID = "1:284743142117:android:43d4b5372e04ec58ea620c";
    public static final String DATABASE_URL = "https://movieplanner-100-default-rtdb.europe-west1.firebasedatabase.app/";

    public static boolean isConfigured() {
        return !API_KEY.isEmpty() && !APPLICATION_ID.isEmpty() && !DATABASE_URL.isEmpty();
    }

    public static void ensureInitialized(Context context) {
        if (!FirebaseApp.getApps(context).isEmpty()) {
            return;
        }
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey(API_KEY)
                .setApplicationId(APPLICATION_ID)
                .setDatabaseUrl(DATABASE_URL)
                .build();
        FirebaseApp.initializeApp(context.getApplicationContext(), options);
    }

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance(DATABASE_URL);
    }
}