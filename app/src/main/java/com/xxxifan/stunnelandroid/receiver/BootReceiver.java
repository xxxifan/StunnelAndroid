package com.xxxifan.stunnelandroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xxxifan.stunnelandroid.utils.Commander;

/**
 * Created by xifan on 16-1-8.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Commander.startStunnel();
    }
}
