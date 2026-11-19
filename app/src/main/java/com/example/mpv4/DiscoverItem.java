package com.example.mpv4;

import java.util.List;

public class DiscoverItem {
    private final String name;
    private final String year;
    private final double rating;
    private final String posterUrl;
    private final String overview;
    private final String type;
    private final int durationInSeconds;
    private final List<String> genres;

    public DiscoverItem(String name, String year, double rating, String posterUrl,
                        String overview, String type, int durationInSeconds, List<String> genres) {
        this.name = name;
        this.year = year;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.overview = overview;
        this.type = type;
        this.durationInSeconds = durationInSeconds;
        this.genres = genres;
    }

    public String getName() { return name; }
    public String getYear() { return year; }
    public double getRating() { return rating; }
    public String getPosterUrl() { return posterUrl; }
    public String getOverview() { return overview; }
    public String getType() { return type; }
    public int getDurationInSeconds() { return durationInSeconds; }
    public List<String> getGenres() { return genres; }
}