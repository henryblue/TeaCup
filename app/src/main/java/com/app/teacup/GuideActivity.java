package com.app.teacup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class GuideActivity extends Activity {

    private static final int[] mImageUris = {R.drawable.guide_1,
            R.drawable.guide_2, R.drawable.guide_3, R.drawable.guide_4};
    private List<ImageView> mImgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_guide);
        for (int i = 0; i < mImageUris.length; i++) {
            ImageView view = new ImageView(GuideActivity.this);
            mImgList.add(view);
        }

        ViewPager mViewPager = (ViewPager) findViewById(R.id.vp_guide);

        mViewPager.setAdapter(new GuidePagerAdapter());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                colorChange(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void colorChange(int position) {
        // 用来提取颜色的Bitmap
//http://blog.csdn.net/jdsjlzx/article/details/41441083/
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
            super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }
    }
}
