package com.example.mpv4;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Locale;

public class DiscoverActivity extends AppCompatActivity implements DiscoverAdapter.OnMovieInteractionListener {

    private User currentUser;
    private DatabaseReference userRef;
    private DiscoverAdapter newReleasesAdapter;
    private DiscoverAdapter trendingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

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

        if (currentUser.getMovies() == null) {
            currentUser.setMovies(new ArrayList<>());
        }

        RecyclerView rvNewReleases = findViewById(R.id.rvNewReleases);
        rvNewReleases.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newReleasesAdapter = new DiscoverAdapter(new ArrayList<>(), this);
        rvNewReleases.setAdapter(newReleasesAdapter);

        RecyclerView rvTrendingNow = findViewById(R.id.rvTrendingNow);
        rvTrendingNow.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        trendingAdapter = new DiscoverAdapter(new ArrayList<>(), this);
        rvTrendingNow.setAdapter(trendingAdapter);

        boolean isMinor = "Minor".equalsIgnoreCase(currentUser.getUserType());

        MovieApiClient.fetchNewReleases(isMinor, movies -> newReleasesAdapter.updateItems(movies));
        MovieApiClient.fetchTrending(isMinor, movies -> trendingAdapter.updateItems(movies));

        // Bottom Navigation Bar
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        findViewById(R.id.navAccount).setOnClickListener(v -> {
            startActivity(new Intent(this, AccountActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    // SHORT PRESS: Only movie details
    @Override
    public void onMovieShortClick(DiscoverItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_movie_details, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        ImageView ivPoster = dialogView.findViewById(R.id.ivDialogPoster);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMeta = dialogView.findViewById(R.id.tvDialogMeta);
        TextView tvGenres = dialogView.findViewById(R.id.tvDialogGenres);
        TextView tvOverview = dialogView.findViewById(R.id.tvDialogOverview);
        Button btnClose = dialogView.findViewById(R.id.btnDialogClose);

        tvTitle.setText(item.getName());
        String meta = item.getYear() + "  •  ★ " + String.format(Locale.US, "%.1f/10", item.getRating());
        tvMeta.setText(meta);
        tvGenres.setText(item.getGenres() != null ? TextUtils.join(" • ", item.getGenres()) : "Movie");
        tvOverview.setText(item.getOverview());

        if (item.getPosterUrl() != null && !item.getPosterUrl().isEmpty()) {
            Glide.with(this)
                    .load(item.getPosterUrl())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivPoster);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // LONG PRESS: Less details + Watchlist / Schedule buttons
    @Override
    public void onMovieLongClick(DiscoverItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_movie_actions, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        ImageView ivPoster = dialogView.findViewById(R.id.ivActionPoster);
        TextView tvTitle = dialogView.findViewById(R.id.tvActionTitle);
        TextView tvMeta = dialogView.findViewById(R.id.tvActionMeta);
        Button btnWatchlist = dialogView.findViewById(R.id.btnActionWatchlist);
        Button btnSchedule = dialogView.findViewById(R.id.btnActionSchedule);
        Button btnCancel = dialogView.findViewById(R.id.btnActionCancel);

        tvTitle.setText(item.getName());
        String meta = item.getYear() + "  •  ★ " + String.format(Locale.US, "%.1f/10", item.getRating());
        tvMeta.setText(meta);

        if (item.getPosterUrl() != null && !item.getPosterUrl().isEmpty()) {
            Glide.with(this)
                    .load(item.getPosterUrl())
                    .placeholder(android.R.color.darker_gray)
                    .into(ivPoster);
        }

        btnWatchlist.setOnClickListener(v -> {
            dialog.dismiss();
            addMovieToWatchlist(item);
        });

        btnSchedule.setOnClickListener(v -> {
            dialog.dismiss();
            openScheduleScreen(item);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void addMovieToWatchlist(DiscoverItem item) {
        for (Movie m : currentUser.getMovies()) {
            if (m.getTitle() != null && m.getTitle().equalsIgnoreCase(item.getName())) {
                Toast.makeText(this, item.getName() + " is already in your Watchlist!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Movie newMovie = new Movie(
                item.getName(),
                item.getType(),
                item.getDurationInSeconds(),
                item.getGenres()
        );
        newMovie.setStatus("Watchlist");
        currentUser.addMovie(newMovie);

        userRef.setValue(currentUser).addOnSuccessListener(unused ->
                Toast.makeText(this, "Added \"" + item.getName() + "\" to Watchlist!", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // TODO: Replace this placeholder once your Schedule Watch screen/activity is built
    private void openScheduleScreen(DiscoverItem item) {
        Toast.makeText(this, "Schedule Watch coming soon for: " + item.getName(), Toast.LENGTH_SHORT).show();
        /*
         * When your ScheduleActivity is ready, uncomment and use:
         * Intent intent = new Intent(this, ScheduleActivity.class);
         * intent.putExtra("movie_name", item.getName());
         * intent.putExtra("movie_type", item.getType());
         * intent.putExtra("movie_duration", item.getDurationInSeconds());
         * intent.putStringArrayListExtra("movie_genres", new ArrayList<>(item.getGenres()));
         * startActivity(intent);
         */
    }
}