package com.app.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.app.teacup.R;

import java.util.ArrayList;


public class PagerAdapter extends FragmentPagerAdapter {

    private String[] tabTitles;
    private Context mContext;
    private ArrayList<Fragment> mFragmentArrayList;

    public PagerAdapter(FragmentManager fm, Context context, ArrayList<Fragment> lists) {
        super(fm);
        mContext = context;
        mFragmentArrayList = lists;
        tabTitles = mContext.getResources().getStringArray(R.array.tab_name);
    }


    @Override
    public Fragment getItem(int position) {
        return mFragmentArrayList.get(position);
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
