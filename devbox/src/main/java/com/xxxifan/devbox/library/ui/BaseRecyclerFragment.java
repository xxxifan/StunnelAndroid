package com.xxxifan.devbox.library.ui;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.helpers.RecyclerConfig;


/**
 * Created by Bob Peng on 2015/5/12.
 */
public abstract class BaseRecyclerFragment extends BaseFragment {

    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_SPACER = 2;
    public static final int VIEW_TYPE_OTHER = -1;

    protected static final int REQUEST_PAGE_SIZE = 20;
    protected static final int REQUEST_NO_ID = -1;
    protected static final int REQUEST_LOAD_MORE = 1;
    protected static final int REQUEST_REFRESH = 0;


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    private RecyclerConfig mConfig;

    private int mLastId = REQUEST_NO_ID;
    private boolean mIsDataEnd;
    private boolean mPostRefresh;

    protected RecyclerConfig getConfig() {
        if (mConfig == null) {
            mConfig = RecyclerConfig.newInstance();
        }
        return mConfig;
    }

    @Override
    protected int getLayoutId() {
        return getConfig().getLayoutResId();
    }

    @Override
    protected void initView(View rootView) {
        // setup recycler view
        View childView = rootView.findViewById(R.id.fragment_recycler_view);
        if (childView == null) {
            throw new IllegalStateException("RecyclerView not found");
        }
        setupRecyclerView(mRecyclerView = (RecyclerView) childView);

        // setup refresh layout
        View layoutView = rootView.findViewById(R.id.fragment_recycler_swipe_layout);
        if (layoutView != null) {
            mRefreshLayout = (SwipeRefreshLayout) layoutView;
            mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mLastId = getTopPagerId();
                    onDataRefresh(REQUEST_REFRESH);
                }
            });
            if (mPostRefresh) {
                mPostRefresh = false;
                mRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(true);
                    }
                });
            }
        }
    }

    protected void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getConfig().isEnableScrollListener()) {
            recyclerView.addOnScrollListener(new ScrollListener());
        }

        if (mAdapter == null) {
            mAdapter = new BaseRecyclerAdapter();
        }
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnRefreshListener(null);
            mRefreshLayout = null;
        }
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(null);
        }
    }

    /**
     * Now this method will be called automatically
     */
    @Override
    protected boolean onDataLoad() {
        startRefresh(REQUEST_LOAD_MORE);
        return false;
    }

    /**
     * start refresh animation and run as load more type.
     */
    protected void startRefresh() {
        startRefresh(REQUEST_LOAD_MORE);
    }

    protected void startRefresh(int loadType) {
        if (mRefreshLayout == null) {
            mPostRefresh = true;
        } else {
            mRefreshLayout.setRefreshing(true);
        }
        onDataRefresh(loadType);
    }

    protected void stopRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void notifyDataLoaded() {
        super.notifyDataLoaded();
        try {
            mAdapter.notifyDataSetChanged();
        } catch (IllegalStateException ignore) {
        }

        if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
            stopRefresh();
        }
    }

    protected void setIsDataEnd(boolean isEnd) {
        mIsDataEnd = isEnd;
    }

    protected boolean isDataEnd() {
        return mIsDataEnd;
    }

    protected void onScrolledToEnd() {
        mLastId = getNextPagerId();
        startRefresh(REQUEST_LOAD_MORE);
    }

    protected int getNextPagerId() {
        return REQUEST_NO_ID;
    }

    protected int getTopPagerId() {
        return REQUEST_NO_ID;
    }

    protected int getExtraItemSize() {
        return 0;
    }

    /**
     * @return lastId, decided by load type, could be result of getNextPagerId() or getTopPagerId()
     */
    protected int getLastId() {
        return mLastId;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected SwipeRefreshLayout getRefreshLayout() {
        return mRefreshLayout;
    }

    protected RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    protected void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    /**
     * called when a good time to load data.
     * SwipeRefreshLayout will call this method too.
     *
     * @param loadType choose to append result to current list or replace list.
     */
    protected abstract void onDataRefresh(int loadType);

    /**
     * Delegate method to generate view holder.
     */
    protected abstract RecyclerView.ViewHolder createViewHolder(ViewGroup parent, int viewType);

    protected abstract void bindViewHolder(RecyclerView.ViewHolder holder, int position);

    protected abstract int getViewType(int position);

    protected abstract int getCount();

    private class BaseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return BaseRecyclerFragment.this.createViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            BaseRecyclerFragment.this.bindViewHolder(holder, position);
        }

        @Override
        public int getItemCount() {
            return BaseRecyclerFragment.this.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return BaseRecyclerFragment.this.getViewType(position);
        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0 && !recyclerView.canScrollVertically(1) && !isDataEnd()) {
                onScrolledToEnd();
            }
        }
    }
}
