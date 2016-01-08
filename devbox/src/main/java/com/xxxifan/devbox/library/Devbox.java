package com.xxxifan.devbox.library;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import com.xxxifan.devbox.library.tools.Log;

/**
 * Created by xifan on 15-7-16.
 */
public class Devbox {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DATA = "data";
    public static final String PREF_LAST_UPDATE = "last_update";
    public static final String PREF_READ_SET_TIP = "read_set_tip";

    private static Application sApplication;
    private static HandlerThread sWorkerThread;
    private static Handler sWorkerHandler;

    public static void install(Application application, boolean debugMode) {
        sApplication = application;
        Log.debugMode = debugMode;
    }

    public static Application getAppDelegate() {
        if (sApplication == null) {
            throw new IllegalStateException("Application instance is null, please check you have " +
                    "correct config");
        }
        return sApplication;
    }

    public static HandlerThread getWorkerThread() {
        if (sWorkerThread == null || (!sWorkerThread.isAlive() | sWorkerThread.isInterrupted()
                | sWorkerThread.getState() == Thread.State.TERMINATED)) {
            synchronized (Devbox.class) {
                sWorkerThread = new HandlerThread("DevBoxTask", 3);
                sWorkerThread.start();
            }
        }
        return sWorkerThread;
    }

    public static Handler getWorkerHandler() {
        if (sWorkerHandler == null) {
            synchronized (Devbox.class) {
                sWorkerHandler = new Handler(getWorkerThread().getLooper());
            }
        } else {
            if (sWorkerHandler.getLooper() == null) {
                synchronized (Devbox.class) {
                    sWorkerHandler = new Handler(getWorkerThread().getLooper());
                }
            }
        }
        return sWorkerHandler;
    }

}
