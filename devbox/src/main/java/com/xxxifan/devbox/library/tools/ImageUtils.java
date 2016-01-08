package com.xxxifan.devbox.library.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xxxifan.devbox.library.callbacks.http.ImageDownloadCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by xifan on 15-12-23.
 */
public class ImageUtils {

    public static void loadBitmap(ImageDownloadCallback callback) {
        callback.onLoadImage();
    }

    public static BitmapFactory.Options getSampleSizeOptions(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;
        return options;
    }

    /**
     * @return saved file, null if failed
     */
    public static File saveBitmap(Bitmap bitmap, File target, Bitmap.CompressFormat format, boolean recycle) {
        if (bitmap != null && target != null) {
            try {
                if (target.exists()) {
                    target.delete();
                }

                FileOutputStream stream = new FileOutputStream(target);
                bitmap.compress(format, 90, stream);
                stream.flush();
                stream.close();
                if (recycle) {
                    bitmap.recycle();
                }
                return target;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
