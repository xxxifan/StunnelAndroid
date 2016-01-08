package com.xxxifan.stunnelandroid.model;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.xxxifan.devbox.library.AppPref;

/**
 * Created by xifan on 16-1-8.
 */
public class ServerInfo {
    public String server;
    public String serverPort;
    public String localPort;

    public void loadInfo() {
        SharedPreferences prefs = AppPref.getPrefs("server");
        server = prefs.getString("server", "");
        serverPort = prefs.getString("serverPort", "");
        localPort = prefs.getString("localPort", "");
    }

    public boolean hasConfig() {
        return !(TextUtils.isEmpty(server) || TextUtils.isEmpty(serverPort) || TextUtils.isEmpty(localPort));
    }
}
