package com.xxxifan.stunnelandroid;

import android.app.Application;

import com.xxxifan.devbox.library.Devbox;

/**
 * Created by xifan on 16-1-8.
 */
public class App extends Application {
    private static App sApp;

    public static App get() {
        return sApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        Devbox.install(this, true);
    }
}
