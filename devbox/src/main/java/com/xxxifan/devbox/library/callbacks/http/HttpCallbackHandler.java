package com.xxxifan.devbox.library.callbacks.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

/**
 * Created by xifan on 15-7-25.
 */
public class HttpCallbackHandler extends Handler {
    private HttpCallback mHttpCallback;

    public HttpCallbackHandler(HttpCallback httpCallback) {
        super(Looper.getMainLooper());
        mHttpCallback = httpCallback;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 1:
                mHttpCallback.done(msg.obj, null);
                break;
            case -1:
                mHttpCallback.done(null, (IOException) msg.obj);
                break;
        }
    }
}
