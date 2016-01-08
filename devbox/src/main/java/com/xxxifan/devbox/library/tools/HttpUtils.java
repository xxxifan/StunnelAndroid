package com.xxxifan.devbox.library.tools;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.xxxifan.devbox.library.Devbox;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.entity.BaseEntity;
import com.xxxifan.devbox.library.receivers.DownloadReceiver;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by xifan on 15-7-17.
 */
public class HttpUtils {
    public static final MediaType MEDIA_TYPE_URLENCODE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private static OkHttpClient sHttpClient;
    private static Gson mGson;
    private static DownloadReceiver mDownloadReceiver;

    private HttpUtils() {
    }

    public static OkHttpClient getHttpClient() {
        if (sHttpClient == null) {
            sHttpClient = new OkHttpClient();
            Cache cache = new Cache(Utils.getCacheDir(), 100 * 1024 * 1024);
            sHttpClient.setCache(cache);
            sHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
            sHttpClient.setReadTimeout(20, TimeUnit.SECONDS);
            sHttpClient.setWriteTimeout(20, TimeUnit.SECONDS);
            mGson = new Gson();
        }
        return sHttpClient;
    }

    private static Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public static Call get(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        Call call = getHttpClient().newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call post(String url, Object jsonBody, Callback callback) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, getGson().toJson(jsonBody, jsonBody
                .getClass()));
        return post(url, body, callback);
    }

    public static Call post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        return post(url, body, callback);
    }

    public static Call post(String url, BaseEntity entity, Callback callback) {
        RequestBody body = getRequestBody(entity);
        return post(url, body, callback);
    }

    public static Call post(String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = getHttpClient().newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call postImage(String url, File file, Callback callback) {
        RequestBody body = RequestBody.create(MEDIA_TYPE_PNG, file);
        return post(url, body, callback);
    }

    public static Call postForm(String url, FormEncodingBuilder form, Callback callback) {
        return post(url, form.build(), callback);
    }

    private static RequestBody getRequestBody(HashMap<String, Object> map) {
        RequestBody body;
        StringBuilder str = new StringBuilder();
        Set<String> keySet = map.keySet();
        try {
            for (String key : keySet) {
                if (str.length() > 0) {
                    str.append('&');
                }

                str.append(URLEncoder.encode(key, "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(map.get(key).toString(), "UTF-8"));
            }
            if (str.length() == 0) {
                throw new IllegalStateException("Form encoded body must have at least one part.");
            }
            body = RequestBody.create(MEDIA_TYPE_URLENCODE, str.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        return body;
    }

    public static long enqueueDownload(String url, String filename) {
        DownloadManager.Request request = getDownloadRequest(url, filename);
        if (request == null) {
            return -1;
        }

        try {
            if (mDownloadReceiver == null) {
                mDownloadReceiver = new DownloadReceiver(filename);
            }
            mDownloadReceiver.register(Devbox.getAppDelegate());
            DownloadManager manager = (DownloadManager) Devbox.getAppDelegate().getSystemService(Context.DOWNLOAD_SERVICE);
            request.setMimeType("application/vnd.android.package-archive");
            return manager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static DownloadManager.Request getDownloadRequest(String url, String filename) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        boolean foundFolder = folder.exists() && folder.isDirectory() || Environment.MEDIA_MOUNTED.equals
                (Environment.getExternalStorageState()) && folder.mkdirs();
        if (foundFolder) {
            File apkFile = new File(folder, filename);
            if (apkFile.exists()) {
                apkFile.delete();
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setTitle(filename);
            request.setDescription("Downloading");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            return request;
        } else {
            Toast.makeText(Devbox.getAppDelegate(), R.string.msg_cannot_download_update, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static void onAvosException(String e) {
        Toast.makeText(Devbox.getAppDelegate(), e, Toast.LENGTH_SHORT).show();
    }
}
