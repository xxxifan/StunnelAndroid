package com.xxxifan.devbox.library.callbacks.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xifan on 15-7-22.
 */
public class ImageDownloadCallback extends DownloadCallback<Bitmap> {

    public ImageDownloadCallback(File targetFile) {
        super(targetFile);
    }

    @Override
    public void onResponse(Response response) throws IOException {
        super.onResponse(response);
        onLoadImage();
    }

    public void onLoadImage() {
        InputStream stream;
        try {
            stream = new FileInputStream(getTargetFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            postResult(null, e);
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        postResult(BitmapFactory.decodeStream(stream, null, options), null);
    }
}
