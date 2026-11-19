package com.example.mpv4;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String key;
    private String username;
    private int userId;
    private String name;
    private String password;
    private String userType; // "Adult" or "Minor"
    private boolean remindToWatch;
    private String profilePicture;
    private List<Movie> movies;
    private List<User> friends;

    // Empty constructor required by Firebase Realtime Database
    public User() {
        this.movies = new ArrayList<>();
        this.friends = new ArrayList<>();
    }

    public User(String key, String username, int userId, String name, String password, boolean remindToWatch, String userType) {
        this.key = key;
        this.username = username;
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.remindToWatch = remindToWatch;
        this.userType = userType;
        this.profilePicture = "";
        this.movies = new ArrayList<>();
        this.friends = new ArrayList<>();
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isRemindToWatch() { return remindToWatch; }
    public void setRemindToWatch(boolean remindToWatch) { this.remindToWatch = remindToWatch; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public List<Movie> getMovies() {
        if (movies == null) {
            movies = new ArrayList<>();
        }
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = (movies != null) ? movies : new ArrayList<>();
    }

    public void addMovie(Movie movie) {
        if (this.movies == null) {
            this.movies = new ArrayList<>();
        }
        this.movies.add(movie);
    }

    public List<User> getFriends() {
        if (friends == null) {
            friends = new ArrayList<>();
        }
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = (friends != null) ? friends : new ArrayList<>();
    }

    public void addFriend(User friend) {
        if (this.friends == null) {
            this.friends = new ArrayList<>();
        }
        this.friends.add(friend);
    }

    public boolean canWatchMatureContent() {
        return "Adult".equalsIgnoreCase(userType);
    }
}