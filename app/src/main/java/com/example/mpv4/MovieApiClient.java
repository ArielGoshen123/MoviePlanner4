package com.example.mpv4;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class MovieApiClient {

    private static final String API_KEY = "8cb04949c19d99031dce0127b4420a7d";
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public interface Callback {
        void onResult(List<DiscoverItem> movies);
    }

    public static void fetchNewReleases(boolean isMinor, Callback callback) {
        String endpoint = isMinor
                ? BASE_URL + "/discover/movie?api_key=" + API_KEY
                  + "&certification_country=US&certification.lte=PG"
                  + "&primary_release_date.gte=2025-06-01"
                  + "&sort_by=popularity.desc"
                : BASE_URL + "/movie/now_playing?api_key=" + API_KEY + "&language=en-US&page=1";
        fetchFromUrl(endpoint, callback);
    }

    public static void fetchTrending(boolean isMinor, Callback callback) {
        String endpoint = isMinor
                ? BASE_URL + "/discover/movie?api_key=" + API_KEY
                  + "&certification_country=US&certification.lte=PG"
                  + "&with_genres=16&sort_by=popularity.desc"
                : BASE_URL + "/trending/movie/week?api_key=" + API_KEY;
        fetchFromUrl(endpoint, callback);
    }

    private static void fetchFromUrl(String urlString, Callback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        if ("YOUR_TMDB_API_KEY".equals(API_KEY) || API_KEY.isEmpty()) {
            mainHandler.post(() -> callback.onResult(getFallbackMovies()));
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<DiscoverItem> results = new ArrayList<>();
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONArray array = root.getJSONArray("results");

                for (int i = 0; i < Math.min(array.length(), 15); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String name = obj.optString("title", "Unknown");
                    String releaseDate = obj.optString("release_date", "");
                    String year = releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : "N/A";
                    double rating = obj.optDouble("vote_average", 0.0);
                    String posterPath = obj.optString("poster_path", "");
                    String posterUrl = posterPath.isEmpty() ? "" : IMAGE_BASE_URL + posterPath;
                    String overview = obj.optString("overview", "No overview available.");
                    if (overview.trim().isEmpty()) {
                        overview = "No overview available.";
                    }

                    List<String> genres = parseGenreIds(obj.optJSONArray("genre_ids"));
                    results.add(new DiscoverItem(name, year, rating, posterUrl, overview, "Movie", 7200, genres));
                }
            } catch (Exception e) {
                results = getFallbackMovies();
            }

            List<DiscoverItem> finalResults = results;
            mainHandler.post(() -> callback.onResult(finalResults));
        });
    }

    private static List<String> parseGenreIds(JSONArray genreIds) {
        if (genreIds == null || genreIds.length() == 0) {
            return Collections.singletonList("Movie");
        }
        Map<Integer, String> map = new HashMap<>();
        map.put(28, "Action");
        map.put(12, "Adventure");
        map.put(16, "Animation");
        map.put(35, "Comedy");
        map.put(80, "Crime");
        map.put(18, "Drama");
        map.put(10751, "Family");
        map.put(14, "Fantasy");
        map.put(27, "Horror");
        map.put(878, "Sci-Fi");
        map.put(53, "Thriller");

        List<String> names = new ArrayList<>();
        for (int i = 0; i < genreIds.length(); i++) {
            String g = map.get(genreIds.optInt(i));
            if (g != null) names.add(g);
        }
        return names.isEmpty() ? Collections.singletonList("Movie") : names;
    }

    private static List<DiscoverItem> getFallbackMovies() {
        List<DiscoverItem> list = new ArrayList<>();
        list.add(new DiscoverItem("Inside Out 2", "2024", 7.6,
                "https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
                "Teenager Riley's mind headquarters undergoes a sudden demolition to make room for something entirely unexpected: new Emotions.",
                "Movie", 5760, Arrays.asList("Animation", "Family")));
        list.add(new DiscoverItem("The Wild Robot", "2024", 8.4,
                "https://image.tmdb.org/t/p/w500/wTnV3PCVW5O92JMrFvvrRcV39RU.jpg",
                "After a shipwreck, an intelligent robot called Roz is stranded on an uninhabited island and must learn to adapt to the harsh surroundings.",
                "Movie", 6120, Arrays.asList("Animation", "Adventure")));
        return list;
    }
}