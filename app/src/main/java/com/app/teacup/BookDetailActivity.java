package com.app.teacup;


import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.app.bean.book.FindBookInfo;
import com.app.fragment.DetailFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private FindBookInfo mBookInfo;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_book_detail);
        initToolBar();
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    private void initView() {
        mBookInfo = (FindBookInfo) getIntent().getSerializableExtra("book");
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(mBookInfo.getmBookTitle());
        }

        ImageView ivImage = (ImageView) findViewById(R.id.iv_book_image);
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(this).load(mBookInfo.getmImgUrl())
                    .error(R.drawable.photo_loaderror)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(ivImage);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(this).load(mBookInfo.getmImgUrl())
                        .error(R.drawable.photo_loaderror)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.main_load_bg);
            }
        }

        mViewPager = (ViewPager) findViewById(R.id.book_viewpager);
        setupViewPager(mViewPager);

        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText(
                    getString(R.string.book_content_detail)));
            tabLayout.addTab(tabLayout.newTab().setText(
                    getString(R.string.book_author_detail)));
            tabLayout.addTab(tabLayout.newTab().setText(
                    getString(R.string.book_table)));

            tabLayout.setupWithViewPager(mViewPager);
        }
    }

    private void setupViewPager(ViewPager mViewPager) {
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(DetailFragment.newInstance(mBookInfo.getmSummary()),
                getString(R.string.book_content_detail));
        adapter.addFragment(DetailFragment.newInstance(mBookInfo.getmAuthor()),
                getString(R.string.book_author_detail));
        adapter.addFragment(DetailFragment.newInstance(mBookInfo.getmTable()),
                getString(R.string.book_table));
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        tabLayout.setVisibility(View.GONE);
        super.onBackPressed();
    }

    static class MyPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
