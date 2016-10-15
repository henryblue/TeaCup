package com.app.teacup;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.app.adapter.PagerAdapter;
import com.app.fragment.photo.DoubanMeiziFragment;
import com.app.fragment.photo.GaoxiaoFragment;
import com.app.fragment.photo.JiandanMeiziFragment;
import com.app.fragment.photo.QiubaiFragment;
import com.app.util.ToolUtils;

import java.util.ArrayList;
import java.util.List;


public class PhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToolUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_photo);
        initView();
        initToolBar();
    }

    private void initView() {
        List<Fragment> mFragmentLists = new ArrayList<>();
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.photo_tab_layout);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.photo_view_pager);

        JiandanMeiziFragment jiandanMeiziFragment = new JiandanMeiziFragment();
        mFragmentLists.add(jiandanMeiziFragment);
        DoubanMeiziFragment doubanMeiziFragment = new DoubanMeiziFragment();
        mFragmentLists.add(doubanMeiziFragment);
        GaoxiaoFragment gaoxiaoFragment = new GaoxiaoFragment();
        mFragmentLists.add(gaoxiaoFragment);
        QiubaiFragment qiubaiFragment = new QiubaiFragment();
        mFragmentLists.add(qiubaiFragment);

        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager(),
                mFragmentLists, getResources().getStringArray(R.array.tab_photo));

        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setAdapter(mPagerAdapter);
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(mViewPager);
                mTabLayout.setTabMode(TabLayout.MODE_FIXED);
            }
        }
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.item_package));
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
