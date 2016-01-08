package com.xxxifan.stunnelandroid.model;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.xxxifan.devbox.library.AppPref;

/**
 * Created by xifan on 16-1-8.
 */
public class ServerInfo {

    private static final String PREF_NAME = "server";

    public String server;
    public String serverPort;
    public String localPort;
    public boolean start;

    public void loadInfo() {
        SharedPreferences prefs = AppPref.getPrefs(PREF_NAME);
        server = prefs.getString("server", "");
        serverPort = prefs.getString("serverPort", "");
        localPort = prefs.getString("localPort", "");
        start = AppPref.getPrefs().getBoolean("start", false);
    }

    public boolean hasConfig() {
        return !(TextUtils.isEmpty(server) || TextUtils.isEmpty(serverPort) || TextUtils.isEmpty(localPort));
    }

    public void save() {
        if (hasConfig()) {
            SharedPreferences.Editor editor = AppPref.getPrefs(PREF_NAME).edit();
            editor.putString("server", server);
            editor.putString("serverPort", serverPort);
            editor.putString("localPort", localPort);
            editor.apply();
        }
    }

    public void setServiceState(boolean start) {
        this.start = start;
        AppPref.putBoolean("start", start);
    }
}
