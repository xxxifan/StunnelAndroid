package com.xxxifan.devbox.library.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.adapter.BasePagerAdapter;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by xifan on 15-5-17.
 */
public abstract class BasePagerFragment extends BaseFragment {

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
        ButterKnife.findById(rootView, R.id.base_viewpager_strip);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(initFragments(savedInstanceState));
    }

    /**
     * Init fragments, return null if no saved fragments, which means no need to create new instances.
     */
    protected List<Fragment> initFragments(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            List<Fragment> fragmentList = getChildFragmentManager().getFragments();
            if (fragmentList != null && !fragmentList.isEmpty()) {
                return fragmentList;
            }
        }
        return null;
    }

    protected void setupViewPager(List<Fragment> fragments) {
        if (fragments != null && !fragments.isEmpty()) {
            mViewPager.setAdapter(mPagerAdapter = new BasePagerAdapter(getChildFragmentManager(),
                    fragments));
            mPagerStrip.setViewPager(mViewPager);
        }
    }

    protected void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mPagerStrip.setOnPageChangeListener(listener);
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
}
