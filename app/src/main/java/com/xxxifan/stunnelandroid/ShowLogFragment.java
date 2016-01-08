package com.xxxifan.stunnelandroid;

import android.view.View;
import android.widget.TextView;

import com.xxxifan.devbox.library.ui.BaseFragment;

import butterknife.Bind;

/**
 * Created by xifan on 16-1-8.
 */
public class ShowLogFragment extends BaseFragment {

    @Bind(R.id.log_text)
    TextView mLogText;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_log;
    }

    @Override
    protected void initView(View rootView) {

    }

    @Override
    protected String getSimpleName() {
        return getClass().getSimpleName();
    }
}
