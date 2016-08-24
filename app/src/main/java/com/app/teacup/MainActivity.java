package com.app.teacup;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.app.adapter.PagerAdapter;
import com.app.fragment.mainPage.FindBookFragment;
import com.app.fragment.mainPage.MusicFragment;
import com.app.fragment.mainPage.NewsFragment;
import com.app.fragment.mainPage.ReadFragment;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initView();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        mToolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(mToolbar);

    }

    private void initView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupDrawerContent(mNavigationView);

        ArrayList<Fragment> mFragmentLists = new ArrayList<>();
        FindBookFragment mFindBookFragment = new FindBookFragment();
        ReadFragment mReadFragment = new ReadFragment();
        MusicFragment mMusicFragment = new MusicFragment();
        NewsFragment newsFragment = new NewsFragment();
        mFragmentLists.add(newsFragment);
        mFragmentLists.add(mReadFragment);
        mFragmentLists.add(mMusicFragment);
        mFragmentLists.add(mFindBookFragment);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager(),
                mFragmentLists, getResources().getStringArray(R.array.tab_name));

        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setAdapter(mPagerAdapter);
        }
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(mViewPager);
            mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
    }

    private void setupDrawerContent(NavigationView mNavigationView) {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navigation_item_main:
                                break;
                            case R.id.navigation_item_photo:
                                enterOtherActivity(PhotoActivity.class);
                                break;
                            case R.id.navigation_item_weather:
                                enterOtherActivity(WeatherActivity.class);
                                break;
                            case R.id.navigation_item_about:
                                break;
                        }
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void enterOtherActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_share:
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
