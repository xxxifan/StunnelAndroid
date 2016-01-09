package com.xxxifan.devbox.library.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.umeng.analytics.MobclickAgent;
import com.xxxifan.devbox.library.Devbox;
import com.xxxifan.devbox.library.entity.CustomEvent;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.tools.Log;
import com.xxxifan.devbox.library.tools.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Bob Peng on 2015/5/7.
 */
public abstract class BaseFragment extends Fragment {

    private MaterialDialog mLoadingDialog;
    private LayoutInflater mInflater;
    private List<ChildUiController> mUiControllers;

    private boolean mIsDataLoaded = false;
    private boolean mLazyLoad = false;
    private int mLayoutId;
    private String mTabTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle data = getArguments();
        if (data != null) {
            onBundleReceived(data);
        }

        mLayoutId = getLayoutId();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(mLayoutId, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getSimpleName());

        // handle ui controller resume
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onResume();
            }
        }

        if (!mIsDataLoaded && !mLazyLoad) {
            setDataLoaded(onDataLoad());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getSimpleName());

        // handle ui controller pause
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onPause();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.e("dd", "setUserVisibleHint " + getSimpleName());
        if (isVisibleToUser && !mIsDataLoaded && mLazyLoad) {
            setDataLoaded(onDataLoad());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.e("dd", "onCreateOptionsMenu " + getSimpleName());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("dd", "onHiddenChanged " + hidden + " " + getSimpleName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissDialog();

        // unregister ui controllers
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onDestroy();
            }
            mUiControllers.clear();
            mUiControllers = null;
        }
    }

    /**
     * Called when fragment initialized with a Bundle in onCreate().
     */
    public void onBundleReceived(Bundle data) {
        String title = data.getString(Devbox.EXTRA_TITLE);
        setTabTitle(TextUtils.isEmpty(title) ? "" : title);
    }

    /**
     * for pager fragments, better to load data when user visible, that's time to setLazyDataLoad to
     * true.
     * called before onResume().
     *
     * @param lazyLoad set to false to call onDataLoad() in onResume(), or later in setMenuVisibility().
     */
    protected void setLazyDataLoad(boolean lazyLoad) {
        mLazyLoad = lazyLoad;
    }

    /**
     * a good point to load data, called on setUserVisibleHint() at first time and later on onResume().
     *
     * @return whether data load successful.
     */
    protected boolean onDataLoad() {
        return false;
    }

    /**
     * notify data loaded and set status to loaded
     */
    protected void notifyDataLoaded() {
        setDataLoaded(true);
    }

    protected boolean isDataLoaded() {
        return mIsDataLoaded;
    }

    protected void setDataLoaded(boolean value) {
        mIsDataLoaded = value;
    }

    public String getTabTitle() {
        return mTabTitle == null ? "" : mTabTitle;
    }

    public void setTabTitle(String title) {
        mTabTitle = title;
    }

    @ColorInt
    protected int getCompatColor(@ColorRes int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }

    protected Drawable getCompatDrawable(@DrawableRes int resId) {
        return ContextCompat.getDrawable(getContext(), resId);
    }

    /**
     * Hacky way to use fragment lifecycle to control dialog
     * You shouldn't use {@link #getLoadingDialog()} anymore
     */
    protected void setCurrentDialog(MaterialDialog newDialog) {
        mLoadingDialog = newDialog;
    }

    public MaterialDialog getLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = ViewUtils.getLoadingDialog(getContext(), null);
        }
        return mLoadingDialog;
    }

    public MaterialDialog getLoadingDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = ViewUtils.getLoadingDialog(getContext(), msg);
        } else {
            mLoadingDialog.setContent(msg);
        }
        return mLoadingDialog;
    }

    public void dismissDialog() {
        ViewUtils.dismissDialog(mLoadingDialog);
    }

    /**
     * @param addToBackStack add current fragment to back stack
     */
    public void checkoutFragment(Fragment fragment, boolean addToBackStack) {
        checkoutFragment(fragment, false, addToBackStack);
    }

    /**
     * @param detach         detach other fragments, if is true, addToBackStack must be false.
     * @param addToBackStack add current fragment to back stack
     */
    public void checkoutFragment(Fragment fragment, boolean detach, boolean addToBackStack) {
        if (fragment == null) {
            return;
        }

        // get tag name
        String tag = fragment.getTag();
        if (TextUtils.isEmpty(tag)) {
            if (fragment instanceof BaseFragment) {
                tag = ((BaseFragment) fragment).getSimpleName();
            } else {
                tag = fragment.getClass().getName();
            }
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        List<Fragment> fragmentList = getFragmentManager().getFragments();
        if (fragmentList != null && fragmentList.size() > 0) {
            // hide other fragment and check if fragment is exist
            for (Fragment oldFragment : fragmentList) {
                if (oldFragment != null && oldFragment.isVisible()) {
                    transaction.hide(oldFragment);
                    if (detach) {
                        transaction.detach(oldFragment);
                    }
                }
            }

            if (addToBackStack) {
                if (detach) {
                    Log.e(this, "You cannot addToBackStack while detach bro");
                } else {
                    transaction.addToBackStack(getTag());
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                }
            }
        }

        if (!fragment.isAdded()) {
            transaction.add(ActivityConfig.DEFAULT_FRAGMENT_CONTAINER_ID, fragment, tag);
        }
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
    }

    protected void postEvent(CustomEvent event, Class target) {
        EventBus.getDefault().post(event);
    }

    protected void postStickyEvent(CustomEvent event, Class target) {
        EventBus.getDefault().postSticky(event);
    }

    protected void registerEventBus(BaseFragment fragment) {
        EventBus.getDefault().registerSticky(fragment);
    }

    protected void unregisterEventBus(BaseFragment fragment) {
        EventBus.getDefault().unregister(fragment);
    }

    /**
     * register controllers, so that BaseFragment can do some lifecycle work automatically
     */
    protected void registerUiController(ChildUiController controller) {
        if (mUiControllers == null) {
            mUiControllers = new ArrayList<>();
        }
        mUiControllers.add(controller);
    }

    protected LayoutInflater getLayoutInflater() {
        return mInflater;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    protected abstract void initView(View rootView);

    /**
     * @return human readable class name for tracking.
     */
    protected abstract String getSimpleName();

}
