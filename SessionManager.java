package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — Saves and loads the logged-in user's info.
 */
public class SessionManager {

    private static final String PREF_NAME = "BMASession";
    private static final String KEY_EMAIL      = "email";
    private static final String KEY_ROLE       = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
