package com.xxxifan.stunnelandroid;

import android.view.View;

import com.xxxifan.devbox.library.ui.BaseFragment;
import com.xxxifan.stunnelandroid.utils.Commander;
import com.xxxifan.stunnelandroid.utils.LogObserver;

/**
 * Created by xifan on 16-1-8.
 */
public class LogFragment extends BaseFragment {

    private LogObserver mLogObserver;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_log;
    }

    @Override
    protected void initView(View rootView) {
        mLogObserver = new LogObserver(rootView);
        Commander.registerLogObserver(mLogObserver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLogObserver != null) {
            mLogObserver.onDestroy();
        }
    }

    @Override
    protected String getSimpleName() {
        return getClass().getSimpleName();
    }
}
