package com.example.myflex;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME      = "myflex";
    private static final String KEY_TOKEN      = "token";
    private static final String KEY_NAME       = "name";
    private static final String KEY_USER_NAME  = "user_name"; // ✅ للتوافق
    private static final String KEY_ROLE       = "role";
    private static final String KEY_LOGIN_TIME = "login_time";
    private static final long   SESSION_DURATION = 24 * 60 * 60 * 1000L;

    private final SharedPreferences        prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void login(String token, String name, String role) {
        editor.putString(KEY_TOKEN,     token);
        editor.putString(KEY_NAME,      name);
        editor.putString(KEY_USER_NAME, name);  // ✅ يحفظ بالمفتاحين
        editor.putString(KEY_ROLE,      role);
        editor.putLong(KEY_LOGIN_TIME,  System.currentTimeMillis());
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        String token     = prefs.getString(KEY_TOKEN, null);
        long   loginTime = prefs.getLong(KEY_LOGIN_TIME, 0);
        boolean tokenValid   = token != null && !token.isEmpty();
        boolean sessionValid = (System.currentTimeMillis() - loginTime) < SESSION_DURATION;
        return tokenValid && sessionValid;
    }

    public String getToken()     { return prefs.getString(KEY_TOKEN,  null); }
    public String getName()      { return prefs.getString(KEY_NAME,   "User"); }
    public String getRole()      { return prefs.getString(KEY_ROLE,   "patient"); }
    public long   getLoginTime() { return prefs.getLong(KEY_LOGIN_TIME, 0); }

    public void refreshSession() {
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public boolean isSessionExpired() {
        return (System.currentTimeMillis() - prefs.getLong(KEY_LOGIN_TIME, 0)) >= SESSION_DURATION;
    }
}