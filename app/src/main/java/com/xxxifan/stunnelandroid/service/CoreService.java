package com.xxxifan.stunnelandroid.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xxxifan.stunnelandroid.R;

/**
 * Created by xifan on 16-1-9.
 */
public class CoreService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        Notification notification = new Notification.Builder(this)
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(android.R.color.transparent)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.running))
                .build();
        startForeground(R.string.app_name, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
