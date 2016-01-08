package com.xxxifan.devbox.library.ui;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.adapter.BasePagerAdapter;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Bob Peng on 2015/5/12.
 */
public abstract class BasePagerActivity extends BaseActivity {

    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerStrip;
    private BasePagerAdapter mPagerAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.view_base_pager;
    }

    @Override
    protected void initView(View rootView) {
        mViewPager = ButterKnife.findById(rootView, R.id.base_viewpager);
        mPagerStrip = ButterKnife.findById(rootView, R.id.base_viewpager_strip);
    }

    @Override
    protected void onCreateFragment(List<Fragment> savedFragments) {
        onSetupViewPager(initFragments(savedFragments));
    }

    protected void onSetupViewPager(List<Fragment> fragments) {
        if (fragments != null && !fragments.isEmpty()) {
            mViewPager.setAdapter(mPagerAdapter = new BasePagerAdapter(getSupportFragmentManager(),
                    fragments));
            mPagerStrip.setViewPager(mViewPager);
        } else {
            mPagerStrip.setVisibility(View.GONE);
        }
    }

    protected void setPagerStripEnabled(boolean enabled) {
        if (enabled) {
            mPagerStrip.setVisibility(View.VISIBLE);
        } else {
            mPagerStrip.setVisibility(View.GONE);
        }
    }

    protected FrameLayout getPagerRootLayout() {
        return ButterKnife.findById(this, R.id.base_viewpager_layout);
    }

    protected void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPager.setOnPageChangeListener(listener);
    }

    protected ViewPager getViewPager() {
        return mViewPager;
    }

    protected void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    protected PagerSlidingTabStrip getPagerStrip() {
        return mPagerStrip;
    }

    protected void setPagerStrip(PagerSlidingTabStrip pagerStrip) {
        mPagerStrip = pagerStrip;
    }

    protected BasePagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    /**
     * proper time to init fragments inside viewpager
     *
     * @param savedInstanceState null if no saved fragments, which means needs to create new instances.
     **/
    protected abstract List<Fragment> initFragments(List<Fragment> savedInstanceState);
}
