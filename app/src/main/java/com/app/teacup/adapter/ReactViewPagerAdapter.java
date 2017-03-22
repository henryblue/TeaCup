package com.app.teacup.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ReactViewPagerAdapter extends PagerAdapter {

    private final List<View> mViewList;
    private final ViewPager mViewPager;
    private boolean mIsAutoScroll = false;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            if (mIsAutoScroll) {
                mHandler.sendEmptyMessageDelayed(0, 6000);
            }
            super.handleMessage(msg);
        }
    };

    public ReactViewPagerAdapter(ViewPager viewPager, List<View> imageViews) {
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
        container.removeView((View) object);
        object = null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mViewList.get(position % mViewList.size()).getParent() != null) {
            ((ViewPager)mViewList.get(position % mViewList.size()).getParent()).
                    removeView(mViewList.get(position % mViewList.size()));
        }
        try {
            container.addView(mViewList.get(position % mViewList.size()));
        } catch (Exception e) {
            //
        }
        return mViewList.get(position % mViewList.size());
    }


    public void startAutoScrolled() {
        if (mIsAutoScroll) {
            return;
        }
        mIsAutoScroll = true;
        mHandler.sendEmptyMessageDelayed(0, 4000);
    }

    public void stopAutoScrolled() {
        if (!mIsAutoScroll) {
            return;
        }
        mIsAutoScroll = false;
        mHandler.removeMessages(0);
    }
}