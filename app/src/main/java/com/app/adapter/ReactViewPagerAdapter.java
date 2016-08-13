package com.app.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class ReactViewPagerAdapter extends PagerAdapter {

    private List<ImageView> mImageViewList;
    private ViewPager mViewPager;
    private boolean mIsAutoScroll = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            if (mIsAutoScroll) {
                mHandler.sendEmptyMessageDelayed(0, 4000);
            }
            super.handleMessage(msg);
        }
    };

    public ReactViewPagerAdapter(ViewPager viewPager, List<ImageView> imageViews) {
        mViewPager = viewPager;
        mImageViewList = imageViews;
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
        container.addView(mImageViewList.get(position % mImageViewList.size()));
        return mImageViewList.get(position % mImageViewList.size());
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
