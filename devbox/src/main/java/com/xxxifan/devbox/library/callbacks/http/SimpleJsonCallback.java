package com.xxxifan.devbox.library.callbacks.http;

import com.google.gson.Gson;
import com.squareup.okhttp.Response;
import com.xxxifan.devbox.library.entity.BaseEntity;

import java.io.IOException;

/**
 * Created by xifan on 15-7-26.
 */
public class SimpleJsonCallback extends HttpCallback<BaseEntity> {
    @Override
    public void onResponse(Response response) throws IOException {
        BaseEntity result = new Gson().fromJson(response.body().string(), BaseEntity.class);
        if (result == null) {
            postResult(null, new IOException("No available result"));
        } else {
            postResult(result, null);
        }
    }
}
