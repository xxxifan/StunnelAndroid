package com.xxxifan.devbox.library.callbacks.http;

import android.os.Message;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xxxifan.devbox.library.Devbox;

import java.io.IOException;

/**
 * Created by xifan on 15-7-25.
 */
public abstract class HttpCallback<T> implements Callback {
    private HttpCallbackHandler mResultHandler;

    public HttpCallback() {
        mResultHandler = new HttpCallbackHandler(this);
    }

    @Override
    public void onResponse(Response response) throws IOException {
        throw new IllegalStateException("onResponse is not override!");
    }

    @Override
    public void onFailure(Request request, IOException e) {
        postResult(null, e);
    }

    /**
     * postResult to switch to main thread
     */
    protected void postResult(Object result, IOException e) {
        Message message = mResultHandler.obtainMessage();
        if (result != null) {
            message.what = 1;
            message.obj = result;
        } else {
            message.what = -1;
            message.obj = e;
        }

        mResultHandler.sendMessage(message);
    }

    /**
     * callback on main thread so that we can do some UI work.
     */
    public void done(T result, IOException e) {
        if (e != null) {
            Toast.makeText(Devbox.getAppDelegate(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
