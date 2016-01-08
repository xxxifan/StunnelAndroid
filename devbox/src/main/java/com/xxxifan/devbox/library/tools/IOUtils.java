package com.xxxifan.devbox.library.tools;

import com.xxxifan.devbox.library.callbacks.CommandCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by xifan on 15-7-22.
 */
public class IOUtils {
    public static boolean saveToDisk(InputStream source, File targetFile) {
        return saveToDisk(Okio.buffer(Okio.source(source)), targetFile);
    }

    public static boolean saveToDisk(BufferedSource source, File targetFile) {
        BufferedSink sink = null;
        try {
            sink = Okio.buffer(Okio.sink(targetFile));
            source.readAll(sink);
            sink.emit();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (sink != null) {
                try {
                    sink.close();
                } catch (IOException ignore) {
                }
            }
            if (source != null) {
                try {
                    source.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public static byte[] getFileBytes(File file) {
        try {
            BufferedSource source = Okio.buffer(Okio.source(file));
            return source.readByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void runCmd(String[] cmd, CommandCallback callback) {
        Process p;
        String result;
        try {
            p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            BufferedSource source = Okio.buffer(Okio.source(p.getInputStream()));
            result = source.readUtf8().trim();
            if (callback != null) {
                callback.done(result, null);
            }
            p.destroy();
            source.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.done(null, e);
            }
        }
    }
}
