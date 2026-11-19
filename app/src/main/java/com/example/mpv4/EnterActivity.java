package com.example.mpv4;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class EnterActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private CheckBox rememberMeCheckBox;
    private Button loginButton;
    private Button signUpButton;

    private DatabaseReference usersRef;
    private SharedPreferences prefs;

    public static User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseConfig.ensureInitialized(this);
        usersRef = FirebaseConfig.getDatabase().getReference("users");

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        loginButton = findViewById(R.id.login);
        signUpButton = findViewById(R.id.btnSignUp);

        loginButton.setOnClickListener(v -> attemptLogin());
        signUpButton.setOnClickListener(v -> attemptSignUp());

        prefs = getSharedPreferences("WatchPartyPrefs", MODE_PRIVATE);
        boolean isRemembered = prefs.getBoolean("remember_me", false);
        if (isRemembered) {
            String savedUsername = prefs.getString("saved_username", "");
            String savedPassword = prefs.getString("saved_password", "");
            if (!savedUsername.isEmpty() && !savedPassword.isEmpty()) {
                usernameInput.setText(savedUsername);
                passwordInput.setText(savedPassword);
                rememberMeCheckBox.setChecked(true);
                attemptLogin();
            }
        }
    }

    private void handleRememberMe(String username, String password) {
        SharedPreferences.Editor editor = prefs.edit();
        if (rememberMeCheckBox.isChecked()) {
            editor.putBoolean("remember_me", true);
            editor.putString("saved_username", username);
            editor.putString("saved_password", password);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    private void attemptSignUp() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password first", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_type, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnAdult).setOnClickListener(v -> {
            dialog.dismiss();
            createUserInFirebase(username, password, true);
        });

        dialogView.findViewById(R.id.btnMinor).setOnClickListener(v -> {
            dialog.dismiss();
            createUserInFirebase(username, password, false);
        });

        dialog.show();
    }

    private void createUserInFirebase(String username, String password, boolean isAdult) {
        // Check if any user already has this login username
        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(EnterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Generate a unique top-level Firebase key
                        String uniqueKey = usersRef.push().getKey();
                        if (uniqueKey == null) uniqueKey = "user_" + System.currentTimeMillis();

                        int userId = (int) (System.currentTimeMillis() % 10000);
                        User newUser = isAdult
                                ? new Adult(uniqueKey, username, userId, username, password, true)
                                : new Minor(uniqueKey, username, userId, username, password, false);

                        Movie watchlistMovie = new Movie("Inception", "Movie", 8880, Arrays.asList("Action", "Sci-Fi"));
                        newUser.addMovie(watchlistMovie);

                        Movie watchedMovie = new Movie("The Dark Knight", "Movie", 9120, Arrays.asList("Action", "Crime"));
                        watchedMovie.MarkAsWatched();
                        newUser.addMovie(watchedMovie);

                        usersRef.child(uniqueKey).setValue(newUser).addOnSuccessListener(unused -> {
                            handleRememberMe(username, password);
                            currentUser = newUser;
                            startActivity(new Intent(EnterActivity.this, HomeActivity.class));
                            finish();
                        }).addOnFailureListener(e ->
                                Toast.makeText(EnterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EnterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);

        // Search all user keys for matching "username"
        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loginButton.setEnabled(true);

                        if (!snapshot.exists()) {
                            Toast.makeText(EnterActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Grab the matched user child under its unique key
                        DataSnapshot userSnap = snapshot.getChildren().iterator().next();
                        String dbPassword = userSnap.child("password").getValue(String.class);

                        if (dbPassword == null || !dbPassword.equals(password)) {
                            Toast.makeText(EnterActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userType = userSnap.child("userType").getValue(String.class);
                        if ("Minor".equals(userType)) {
                            currentUser = userSnap.getValue(Minor.class);
                        } else {
                            currentUser = userSnap.getValue(Adult.class);
                        }

                        // Ensure key is populated even for older nodes
                        if (currentUser != null && currentUser.getKey() == null) {
                            currentUser.setKey(userSnap.getKey());
                        }

                        handleRememberMe(username, password);

                        startActivity(new Intent(EnterActivity.this, HomeActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loginButton.setEnabled(true);
                        Toast.makeText(EnterActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}