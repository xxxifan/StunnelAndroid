package com.xxxifan.devbox.library.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.helpers.ActivityConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by xifan on 15-8-12.
 */
public class DrawerAdapter extends BaseAdapter {
    private Context mContext;
    private ListView mListView;
    private List<ListItem> mList;

    public DrawerAdapter(ListView listView, ActivityConfig config) {
        mListView = listView;
        mContext = listView.getContext();
        initMenuList(config);
    }

    private void initMenuList(ActivityConfig config) {
        mList = new ArrayList<>();
        String[] items = mContext.getResources().getStringArray(config.getDrawerMenuItemId());
        TypedArray typedArray = mContext.getResources().obtainTypedArray(config.getDrawerMenuIconId());
        if (typedArray.length() != items.length) {
            throw new IllegalStateException("drawer icons is not equals items!");
        }

        ListItem listItem;
        for (int i = 0, s = typedArray.length(); i < s; i++) {
            listItem = new ListItem(typedArray.getResourceId(i, 0), items[i]);
            mList.add(listItem);
        }

        typedArray.recycle();
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItemCounter(int position, int count) {
        mList.get(position).count = count;
        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return !TextUtils.isEmpty(mList.get(position).menu);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.view_drawer_list, null);
            holder = new ViewHolder();
            holder.drawerContent = ButterKnife.findById(convertView, R.id.drawer_content);
            holder.drawerCount = ButterKnife.findById(convertView, R.id.drawer_count);
            holder.drawerDivider = ButterKnife.findById(convertView, R.id.drawer_divider);
            holder.drawerIcon = ButterKnife.findById(convertView, R.id.drawer_icon);
            holder.drawerTitle = ButterKnife.findById(convertView, R.id.drawer_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListItem listItem = mList.get(position);
        if (TextUtils.isEmpty(listItem.menu) && listItem.iconId == 0) {
            holder.drawerContent.setVisibility(View.GONE);
            holder.drawerDivider.setVisibility(View.VISIBLE);
        } else {
            holder.drawerContent.setVisibility(View.VISIBLE);
            holder.drawerDivider.setVisibility(View.GONE);
            if (listItem.count > 0) {
                holder.drawerCount.setVisibility(View.VISIBLE);
                holder.drawerCount.setText(listItem.count + "");
            } else {
                holder.drawerCount.setVisibility(View.GONE);
            }

            if (mListView.getCheckedItemPosition() == position) {
                convertView.setBackgroundResource(R.color.divider);
                int color = mContext.getResources().getColor(R.color.colorAccent);
                holder.drawerTitle.setTextColor(color);
                holder.drawerIcon.setColorFilter(color);
            } else {
                convertView.setBackgroundResource(0);
                holder.drawerTitle.setTextColor(mContext.getResources().getColor(android.R.color.primary_text_light));
                holder.drawerIcon.clearColorFilter();
            }

            holder.drawerIcon.setImageResource(listItem.iconId);
            holder.drawerTitle.setText(listItem.menu);
        }

        return convertView;
    }

    private static class ViewHolder {
        View drawerDivider;
        ImageView drawerIcon;
        TextView drawerTitle;
        TextView drawerCount;
        RelativeLayout drawerContent;
    }

    private class ListItem {
        @DrawableRes
        int iconId;
        String menu;
        int count;

        public ListItem(@DrawableRes int icon, String menu) {
            this.iconId = icon;
            this.menu = menu;
        }
    }
}
