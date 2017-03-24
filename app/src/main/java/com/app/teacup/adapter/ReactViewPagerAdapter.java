package com.app.teacup.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.app.teacup.ui.ReactViewPager;

import java.util.List;

public class ReactViewPagerAdapter extends PagerAdapter {

    private final List<View> mViewList;
    private final ReactViewPager mViewPager;
    private boolean mIsAutoScroll = false;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            if (mIsAutoScroll) {
                mHandler.sendEmptyMessageDelayed(0, 6000);
            }
            super.handleMessage(msg);
        }
    };

    public ReactViewPagerAdapter(ReactViewPager viewPager, List<View> imageViews) {
        mViewPager = viewPager;
        mViewList = imageViews;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //container.removeView((View) object);
    }

    @Override
    public float getPageWidth(int position) {
        return 0.9f;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position %= mViewList.size();
        if (position < 0) {
            position = mViewList.size() + position;
        }
        View view = mViewList.get(position);
        ViewParent vp = view.getParent();
        if (vp != null) {
            ViewGroup parent = (ViewGroup) vp;
            parent.removeView(view);
        }
        container.addView(view);
        return view;
    }


    public void startAutoScrolled() {
        if (mIsAutoScroll) {
            return;
        }
        mIsAutoScroll = true;
        mHandler.sendEmptyMessageDelayed(0, 6000);
    }

    public void stopAutoScrolled() {
        if (!mIsAutoScroll) {
            return;
        }
        mIsAutoScroll = false;
        mHandler.removeMessages(0);
    }
}