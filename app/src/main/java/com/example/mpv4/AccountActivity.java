package com.example.mpv4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.io.ByteArrayOutputStream;

public class AccountActivity extends AppCompatActivity {

    private User currentUser;
    private DatabaseReference userRef;

    private ImageView ivProfileAvatar;
    private EditText etUsername;
    private Switch notificationSwitch;
    private String encodedPfp = "";

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        }
                        applyAndEncodeAvatar(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    applyAndEncodeAvatar(bitmap);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        currentUser = EnterActivity.currentUser;
        if (currentUser == null) {
            startActivity(new Intent(this, EnterActivity.class));
            finish();
            return;
        }

        FirebaseConfig.ensureInitialized(this);
        userRef = FirebaseConfig.getDatabase()
                .getReference("users")
                .child(currentUser.getKey());

        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        etUsername = findViewById(R.id.etUsername);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        Button galleryButton = findViewById(R.id.galleryButton);
        Button photoButton = findViewById(R.id.photoButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        etUsername.setText(currentUser.getUsername());
        notificationSwitch.setChecked(currentUser.isRemindToWatch());
        encodedPfp = currentUser.getProfilePicture() != null ? currentUser.getProfilePicture() : "";

        if (!encodedPfp.isEmpty()) {
            byte[] bytes = Base64.decode(encodedPfp, Base64.DEFAULT);
            Bitmap savedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (savedBitmap != null) {
                ivProfileAvatar.setImageTintList(null);
                ivProfileAvatar.setImageBitmap(savedBitmap);
            }
        }

        galleryButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        photoButton.setOnClickListener(v -> cameraLauncher.launch(null));

        saveButton.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newUsername.equals(currentUser.getUsername())) {
                saveUserUpdates(newUsername);
                return;
            }

            FirebaseConfig.getDatabase().getReference("users").get()
                    .addOnSuccessListener(snapshot -> {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String existingName = userSnap.child("username").getValue(String.class);
                            if (newUsername.equalsIgnoreCase(existingName)) {
                                Toast.makeText(this, "Username already taken!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        saveUserUpdates(newUsername);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        logoutButton.setOnClickListener(v -> {
            getSharedPreferences("WatchPartyPrefs", MODE_PRIVATE).edit().clear().apply();
            EnterActivity.currentUser = null;

            Intent intent = new Intent(this, EnterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // ===== ADDED: Navigate to DiscoverActivity =====
        findViewById(R.id.navDiscover).setOnClickListener(v -> {
            startActivity(new Intent(this, DiscoverActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        // ===== END ADDED =====
    }

    private void saveUserUpdates(String newUsername) {
        currentUser.setName(newUsername);
        currentUser.setUsername(newUsername);
        currentUser.setRemindToWatch(notificationSwitch.isChecked());
        currentUser.setProfilePicture(encodedPfp);

        userRef.setValue(currentUser).addOnSuccessListener(unused -> {
            getSharedPreferences("WatchPartyPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("saved_username", newUsername)
                    .apply();

            Toast.makeText(this, "Username & settings saved!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void applyAndEncodeAvatar(Bitmap bitmap) {
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        ivProfileAvatar.setImageTintList(null);
        ivProfileAvatar.setImageBitmap(scaled);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        encodedPfp = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}