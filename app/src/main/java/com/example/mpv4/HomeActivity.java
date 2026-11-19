package com.example.mpv4;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private User currentUser;
    private DatabaseReference userRef;

    private Button btnAll, btnWatchlist, btnCompleted;
    private HomeAdapter adapter;
    private List<Movie> allMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

        allMovies = (currentUser.getMovies() != null)
                ? currentUser.getMovies()
                : new ArrayList<>();

        RecyclerView rvMovies = findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HomeAdapter(allMovies);
        rvMovies.setAdapter(adapter);

        btnAll = findViewById(R.id.btnAll);
        btnWatchlist = findViewById(R.id.btnWatchlist);
        btnCompleted = findViewById(R.id.btnCompleted);

        filterMovies("All", btnAll);

        btnAll.setOnClickListener(v -> filterMovies("All", btnAll));
        btnWatchlist.setOnClickListener(v -> filterMovies("Watchlist", btnWatchlist));
        btnCompleted.setOnClickListener(v -> filterMovies("Watched", btnCompleted));

        // ===== ADDED: Navigate to DiscoverActivity =====
        findViewById(R.id.navDiscover).setOnClickListener(v -> {
            startActivity(new Intent(this, DiscoverActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        // ===== END ADDED =====

        // Navigate to AccountActivity
        findViewById(R.id.navAccount).setOnClickListener(v -> {
            startActivity(new Intent(this, AccountActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        Toast.makeText(
                this,
                "Welcome " + currentUser.getName() + "! (" + allMovies.size() + " movies)",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void filterMovies(String filterStatus, Button selectedBtn) {
        int inactiveColor = Color.parseColor("#231F41");
        int activeColor = Color.parseColor("#7B32F1");

        btnAll.setBackgroundColor(inactiveColor);
        btnWatchlist.setBackgroundColor(inactiveColor);
        btnCompleted.setBackgroundColor(inactiveColor);

        selectedBtn.setBackgroundColor(activeColor);

        if ("All".equals(filterStatus)) {
            adapter.updateList(allMovies);
            return;
        }

        List<Movie> filtered = new ArrayList<>();
        for (Movie m : allMovies) {
            String status = (m.getStatus() != null) ? m.getStatus() : "Watchlist";
            if (filterStatus.equalsIgnoreCase(status)) {
                filtered.add(m);
            }
        }
        adapter.updateList(filtered);
    }

    public void syncUserToFirebase() {
        if (userRef != null && currentUser != null) {
            userRef.setValue(currentUser);
        }
    }
}