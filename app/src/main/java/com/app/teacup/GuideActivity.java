package com.app.teacup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


public class GuideActivity extends Activity {

    private static final int[] mImageUris = {R.drawable.guide_1,
            R.drawable.guide_2, R.drawable.guide_3, R.drawable.guide_4};

    private static final int[] mBgColors = {R.color.colorPrimaryDark,
    R.color.orange, R.color.blue, R.color.green};

    private List<View> mViewList = new ArrayList<>();
    private ViewPager mViewPager;
    private LinearLayout mLayoutDot;
    private ImageView mImgView;
    private int mLastDotPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_guide);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height= getResources().getDisplayMetrics().heightPixels;
        FrameLayout.LayoutParams lps = new FrameLayout.LayoutParams(
                (int)(width * 0.7), (int)(height * 0.6));

        for (int i = 0; i < mImageUris.length; i++) {
            View view = View.inflate(GuideActivity.this, R.layout.item_activity_guide, null);
            ImageView imgView = (ImageView) view.findViewById(R.id.iv_guide_view);
            imgView.setLayoutParams(lps);
            imgView.setImageResource(mImageUris[i]);
            mViewList.add(view);
        }

        mLayoutDot = (LinearLayout) findViewById(R.id.ll_dot);
        mImgView = (ImageView) findViewById(R.id.iv_enter_teacup);
        mImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        for (int i = 1; i < mLayoutDot.getChildCount(); i++) {
            View view = mLayoutDot.getChildAt(i);
            view.setEnabled(false);
        }

        mViewPager = (ViewPager) findViewById(R.id.vp_guide);
        mViewPager.setAdapter(new GuidePagerAdapter());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                colorChange(position);
                if (position == mImageUris.length - 1) {
                    mImgView.setVisibility(View.VISIBLE);
                    mLayoutDot.setVisibility(View.GONE);
                } else {
                    mImgView.setVisibility(View.GONE);
                    mLayoutDot.setVisibility(View.VISIBLE);
                }

                View view = mLayoutDot.getChildAt(position);
                view.setEnabled(true);
                View viewLast = mLayoutDot.getChildAt(mLastDotPos);
                viewLast.setEnabled(false);
                mLastDotPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void colorChange(int position) {
        mViewPager.setBackgroundResource(mBgColors[position]);
    }

    private class GuidePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageUris.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViewList.get(position);
            container.addView(view);
            return view;
        }
    }
}
