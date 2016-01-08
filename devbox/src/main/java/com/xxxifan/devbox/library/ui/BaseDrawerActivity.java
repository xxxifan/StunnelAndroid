package com.xxxifan.devbox.library.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.adapter.DrawerAdapter;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.tools.Log;

import butterknife.ButterKnife;

/**
 * Created by xifan on 15-12-12.
 */
public abstract class BaseDrawerActivity extends BaseActivity {

    public static final int DEFAULT_DRAWER_LAYOUT_ID = R.id.drawer_layout;

    private static final int DEFAULT_LIST_ID = R.id.drawer_item_list;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void setContentView(int layoutResID, ActivityConfig config) {
        super.setContentView(layoutResID, config);
        if (config.isDrawerLayout()) {
            setupDrawerLayout();
        }
    }

    protected void setupDrawerLayout() {
        mDrawerLayout = ButterKnife.findById(this, DEFAULT_DRAWER_LAYOUT_ID);
        if (mDrawerLayout == null) {
            Log.e(this, "Cannot find DrawerLayout!");
            return;
        }

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        View headerView = getLayoutInflater().inflate(getConfig().getDrawerHeaderResId(), null);
        ListView drawerListView = ButterKnife.findById(this, DEFAULT_LIST_ID);
        setDrawerAdapter(drawerListView, headerView);
    }

    /**
     * setup drawer item list. You can override it to use a custom adapter.
     */
    protected void setDrawerAdapter(ListView drawerListView, View headerView) {
        final DrawerAdapter drawerAdapter = new DrawerAdapter(drawerListView, getConfig());
        drawerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        drawerListView.setDivider(new ColorDrawable(getResources().getColor(R.color.transparent)));
        drawerListView.setDividerHeight(0);
        drawerListView.setBackgroundColor(getResources().getColor(R.color.white));
        drawerListView.setCacheColorHint(Color.TRANSPARENT);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private int lastCheckPosition;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                if (listView.getHeaderViewsCount() > 0) {
                    position--; // fix wrong pos.
                }
                listView.getCheckedItemPosition();
                if (view.getId() != R.id.drawer_divider && lastCheckPosition != position) {
                    listView.setItemChecked(position, true);
                    lastCheckPosition = position;

                    if (getConfig().getDrawerMenuClickListener() != null) {
                        getConfig().getDrawerMenuClickListener().onMenuClick(view, position);
                    }
                }
            }
        });
        drawerListView.addHeaderView(headerView, null, false);
        drawerListView.setAdapter(drawerAdapter);
        drawerListView.setItemChecked(0, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getConfig().isDrawerLayout() && mDrawerLayout != null) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            } else {
                super.onOptionsItemSelected(item);
            }
            return true;
        }
        return false;
    }

}
