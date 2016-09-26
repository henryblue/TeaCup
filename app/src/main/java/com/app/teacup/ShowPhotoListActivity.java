package com.app.teacup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;


public class ShowPhotoListActivity extends Activity {

    private ArrayList<String> mPhotoList;
    private int mPhotoPos;
    private LinearLayout.LayoutParams mLps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_show_photo_list);
        Intent intent = getIntent();
        mPhotoList = intent.getStringArrayListExtra("photoList");
        mPhotoPos = intent.getIntExtra("photoPos", 0);
        mLps = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mLps.gravity = Gravity.CENTER;

        ViewPager mViewPager = (ViewPager) findViewById(R.id.photo_viewPager);
        mViewPager.setAdapter(new MyViewPagerAdapter());
        mViewPager.setCurrentItem(mPhotoPos);
    }

    private class MyViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPhotoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(ShowPhotoListActivity.this);
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(ShowPhotoListActivity.this).load(mPhotoList.get(position))
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.loading_photo)
                    .dontAnimate()
                    .into(photoView);
            container.addView(photoView, mLps);
            return photoView;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }


    }
}
