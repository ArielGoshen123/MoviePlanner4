package com.example.mpv4;

public class Adult extends User {

    // Empty constructor required by Firebase
    public Adult() {
        super();
        setUserType("Adult");
    }

    public Adult(String key, String username, int userId, String name, String password, boolean remindToWatch) {
        super(key, username, userId, name, password, remindToWatch, "Adult");
    }

    @Override
    public boolean canWatchMatureContent() {
        return true;
    }
}