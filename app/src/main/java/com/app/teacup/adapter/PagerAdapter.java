package com.app.teacup.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;


public class PagerAdapter extends FragmentPagerAdapter {

    private final String[] tabTitles;
    private final List<Fragment> mFragmentArrayList;

    public PagerAdapter(FragmentManager fm, List<Fragment> lists, String[] titles) {
        super(fm);
        mFragmentArrayList = lists;
        tabTitles = titles;
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
