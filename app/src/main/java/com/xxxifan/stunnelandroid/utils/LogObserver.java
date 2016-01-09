package com.xxxifan.stunnelandroid.utils;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xxxifan.stunnelandroid.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by xifan on 16-1-9.
 */
public class LogObserver {

    @Bind(R.id.log_text)
    TextView mLogText;
    @Bind(R.id.log_scroller)
    ScrollView mScrollView;

    private String mLog;

    public LogObserver(View view) {
        ButterKnife.bind(this, view);
        clear();
    }

    public void log(String logString, int type) {
        String level = getTypeLevel(type);
        mLog += level + logString + "\n";
        showLog();
    }

    private void showLog() {
        if (mLogText != null) {
            mLogText.post(() -> {
                mLogText.setText(mLog);
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            });
        }
    }

    public void clear() {
        mLog = "";
        showLog();
    }

    private String getTypeLevel(int type) {
        return type != 0 ? type > 0 ? "[✓] " : "[x] " : "[*] ";
    }


    public void onDestroy() {
        ButterKnife.unbind(this);
    }
}
