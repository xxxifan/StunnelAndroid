package com.xxxifan.devbox.library.tools;

import android.text.TextUtils;

/**
 * Created by xifan on 15-7-25.
 */
public class Log {

    public static boolean debugMode;

    private static long timestamp;

    public static void d(Object obj, String msg) {
        d(obj.getClass(), msg);
    }

    public static void d(Class<?> clazz, String msg) {
        if (debugMode) {
            android.util.Log.d(getName(clazz), msg);
        }
    }

    public static void e(Object obj, String msg) {
        e(obj.getClass(), msg);
    }

    public static void e(Class<?> clazz, String msg) {
        if (debugMode) {
            android.util.Log.e(getName(clazz), msg);
        }
    }

    public static void stamp() {
        stamp(false);
    }

    public static void stamp(boolean show) {
        timestamp = System.currentTimeMillis();
        if (show && debugMode) {
            e(Log.class, "Log timer started at: " + timestamp);
        }
    }

    public static void countStamp(Object obj, String msg) {
        countStamp(obj.getClass(), msg);
    }

    public static void countStamp(Class<?> clazz, String msg) {
        long end = System.currentTimeMillis();
        long counter = end - timestamp;
        if (debugMode) {
            e(clazz, "Tag: " + msg + "\nLog timer ended at: " + end + ", cost: " + counter);
        }
    }

    private static String getName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (TextUtils.isEmpty(name)) {
            name = clazz.getName();
            name = name.substring(name.lastIndexOf(".") + 1, name.length());
        }
        return name;
    }
}
