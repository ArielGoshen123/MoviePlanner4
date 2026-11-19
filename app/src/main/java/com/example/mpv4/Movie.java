package com.example.mpv4;

import java.util.ArrayList;
import java.util.List;

public class Movie implements Content {
    private String title;
    private String type;
    private int durationInSeconds;
    private List<String> genres;
    private String status; // "Watchlist" or "Watched"

    // Empty constructor required by Firebase Realtime Database
    public Movie() {
        this.genres = new ArrayList<>();
        this.status = "Watchlist";
    }

    public Movie(String name, String type, int durationInSeconds, List<String> genres) {
        this.title = name;
        this.type = type;
        this.durationInSeconds = durationInSeconds;
        this.genres = (genres != null) ? genres : new ArrayList<>();
        this.status = "Watchlist";
    }

    @Override
    public void MarkAsWatched() {
        this.status = "Watched";
    }

    public String getTitle() { return title; }
    public void setTitle(String name) { this.title = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDurationInSeconds() { return durationInSeconds; }
    public void setDurationInSeconds(int durationInSeconds) { this.durationInSeconds = durationInSeconds; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}