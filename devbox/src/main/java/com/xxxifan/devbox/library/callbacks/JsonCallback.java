package com.xxxifan.devbox.library.callbacks;

import com.google.gson.Gson;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by xifan on 15-7-25.
 */
public class JsonCallback<T> extends HttpCallback<T> {

    private Class<T> type;

    public JsonCallback(Class<T> type) {
        this.type = type;
    }

    @Override
    public void onResponse(Response response) throws IOException {
        T result = new Gson().fromJson(response.body().string(), type);
        if (result == null) {
            postResult(null, new IOException("No available result"));
        } else {
            postResult(result, null);
        }
    }
}
