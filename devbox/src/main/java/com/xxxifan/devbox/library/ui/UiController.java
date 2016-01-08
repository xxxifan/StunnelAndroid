package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.view.View;

/**
 * Created by xifan on 15-7-22.
 * UiController, support a set of default life circle control. Use {@link BaseActivity#registerUiController(UiController)} to control.
 */
public abstract class UiController {
    private View mView;

    public UiController(View view) {
        if (view == null) {
            throw new IllegalArgumentException("view cannot be null");
        }
        mView = view;
        initView(view);
    }

    public View getView() {
        return mView;
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestroy() {
        mView = null;
    }


    protected Context getContext() {
        return mView == null ? null : mView.getContext();
    }

    protected abstract void initView(View view);
}
