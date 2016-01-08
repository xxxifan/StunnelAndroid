package com.xxxifan.devbox.library.helpers;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.xxxifan.devbox.library.AppPref;
import com.xxxifan.devbox.library.Devbox;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.callbacks.http.JsonCallback;
import com.xxxifan.devbox.library.callbacks.http.SimpleJsonCallback;
import com.xxxifan.devbox.library.entity.BaseEntity;
import com.xxxifan.devbox.library.entity.FirAppInfo;
import com.xxxifan.devbox.library.tools.HttpUtils;
import com.xxxifan.devbox.library.tools.Log;
import com.xxxifan.devbox.library.tools.Utils;

import java.io.IOException;

/**
 * Created by xifan on 15-7-25.
 */
public class FirUpdater {
    public static final int DEFAULT_INTERVAL = 3 * 60 * 60 * 1000; // 3 hours

    public static final String CHECK_LATEST = "http://api.fir.im/apps/latest/%s";
    public static final String GET_DOWNLOAD_TOKEN = "http://api.fir.im/apps/%s/download_token?api_token=%s";
    public static final String DOWNLOAD_URL = "http://download.fir.im/apps/%s/install";

    private static String sAppId;
    private static String sUserToken;

    public static void init(String appId, String userToken) {
        sAppId = appId;
        sUserToken = userToken;
    }

    /**
     * if less than interval, check will not be executed.
     *
     * @param context
     * @param checkInterval in milliseconds.
     */
    public static void checkUpdate(Context context, int checkInterval) {
        if (System.currentTimeMillis() - AppPref.getLong(Devbox.PREF_LAST_UPDATE, 0) >
                checkInterval) {
            checkUpdate(context);
        }
    }

    public static void checkUpdate(final Context context) {
        if (sAppId == null || sUserToken == null) {
            throw new IllegalArgumentException("No available Fir.im appId or token, please ensure you have init FirUpdater in your Application");
        }
        HttpUtils.get(String.format(CHECK_LATEST, sAppId), new JsonCallback<FirAppInfo>(FirAppInfo.class) {
            @Override
            public void done(FirAppInfo result, IOException e) {
                if (e == null) {
                    if (result.version > Utils.getVersionCode()) {
                        showUpdateDialog(context, result);
                    } else {
                        Log.d(getClass(), "No newer version was found");
                    }
                }

                // update check time
                AppPref.putLong(Devbox.PREF_LAST_UPDATE, System.currentTimeMillis());
            }
        });
    }

    public static void getDownloadUrl(final SimpleJsonCallback callback) {
        HttpUtils.get(String.format(GET_DOWNLOAD_TOKEN, sAppId, sUserToken), new SimpleJsonCallback() {
            @Override
            public void done(BaseEntity result, IOException e) {
                super.done(result, e);
                if (result != null) {
                    if (result.containsKey("download_token")) {
                        HttpUtils.post(String.format(DOWNLOAD_URL, sAppId), result, callback);
                    }
                }
            }
        });
    }

    private static void showUpdateDialog(Context context, FirAppInfo appInfo) {
        if (context == null) {
            return;
        }

        View view = View.inflate(context, R.layout.view_update_dialog, null);
        TextView version = (TextView) view.findViewById(R.id.update_version_name);
        TextView description = (TextView) view.findViewById(R.id.update_version_description);

        version.setText(appInfo.versionShort + "(" + appInfo.version + ")");
        description.setText(appInfo.changelog);
        new MaterialDialog.Builder(context)
                .title(R.string.title_update_available)
                .customView(view, true)
                .positiveText(R.string.btn_download)
                .negativeText(R.string.btn_ignore)
                .callback(new FirUpdateButtonCallback())
                .build()
                .show();
    }

    private static class FirUpdateButtonCallback extends MaterialDialog.ButtonCallback {
        @Override
        public void onPositive(final MaterialDialog dialog) {
            getDownloadUrl(new SimpleJsonCallback() {
                @Override
                public void done(BaseEntity result, IOException e) {
                    super.done(result, e);
                    if (result != null && result.containsKey("url")) {
                        String url = result.getString("url", "");
                        long id = HttpUtils.enqueueDownload(url, "com.xxxifan.pieceofwallpaer.apk");
                        Toast.makeText(dialog.getContext(), id > 0 ? R.string.msg_download_update
                                : R.string.msg_cannot_download_update, Toast.LENGTH_SHORT).show();
                    } else {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}
