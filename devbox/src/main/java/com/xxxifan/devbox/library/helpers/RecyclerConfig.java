package com.xxxifan.devbox.library.helpers;

import com.xxxifan.devbox.library.R;

/**
 * Visual configs for BaseRecyclerFragment
 */
public class RecyclerConfig {
    private boolean mEnableScrollListener;
    private int mLayoutResId;

    private RecyclerConfig() {
    }

    public static RecyclerConfig newInstance() {
        RecyclerConfig config = new RecyclerConfig();
        config.setLayoutResId(R.layout.fragment_base_recycler);
        config.enableRefreshLayout(false);
        config.enableScrollListener(true);
        return config;
    }

    public boolean isEnableScrollListener() {
        return mEnableScrollListener;
    }

    public RecyclerConfig enableScrollListener(boolean enableScrollListener) {
        mEnableScrollListener = enableScrollListener;
        return this;
    }

    public RecyclerConfig enableRefreshLayout(boolean enableRefreshLayout) {
        if (getLayoutResId() == 0) {
            setLayoutResId(enableRefreshLayout ? R.layout.fragment_base_recycler_swipe : R.layout
                    .fragment_base_recycler);
        }
        return this;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

    public RecyclerConfig setLayoutResId(int layoutResId) {
        mLayoutResId = layoutResId;
        return this;
    }
}
