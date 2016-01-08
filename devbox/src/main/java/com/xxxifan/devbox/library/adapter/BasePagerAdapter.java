package com.xxxifan.devbox.library.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.xxxifan.devbox.library.Devbox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob Peng on 2015/5/12.
 */
public class BasePagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;
    private String[] mTitles;

    public BasePagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        mFragmentList = fragmentList;
        if (mFragmentList == null) {
            mFragmentList = new ArrayList<>();
        }
    }

    public void setTitles(String[] titles) {
        mTitles = titles;
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // retrieve title from fragments arguments
        if (mTitles == null || mTitles.length < 1) {
            if (mFragmentList != null && mFragmentList.size() > 0) {
                mTitles = new String[mFragmentList.size()];
                try {
                    for (int i = 0; i < mFragmentList.size(); i++) {
                        mTitles[i] = mFragmentList.get(i).getArguments().getString(Devbox.EXTRA_TITLE);
                    }
                } catch (Exception e) {
                    return "";
                }
                return mTitles[position];
            } else {
                return "";
            }
        }

        return mTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList == null ? null : mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }
}
