package com.example.mpv4;

public class Minor extends User {

    // Empty constructor required by Firebase
    public Minor() {
        super();
        setUserType("Minor");
    }

    public Minor(String key, String username, int userId, String name, String password, boolean remindToWatch) {
        super(key, username, userId, name, password, remindToWatch, "Minor");
    }

    @Override
    public boolean canWatchMatureContent() {
        return false;
    }
}