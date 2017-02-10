package com.app.teacup;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
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
import android.view.View;

import com.app.fragment.mainPage.FanjuFragment;
import com.app.receiver.ConnectionChangeReceiver;
import com.app.adapter.PagerAdapter;
import com.app.fragment.mainPage.MusicFragment;
import com.app.fragment.mainPage.NewsFragment;
import com.app.fragment.mainPage.ReadFragment;
import com.app.util.HttpUtils;
import com.app.util.ToolUtils;

import java.util.ArrayList;

import me.drakeet.materialdialog.MaterialDialog;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    public static boolean mIsLoadPhoto;
    public static boolean mIsPlayMusic;
    public static boolean mIsWIFIState;

    private ConnectionChangeReceiver mNetSateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToolUtils.onActivityCreateSetTheme(MainActivity.this);
        setContentView(R.layout.activity_main);

        mIsWIFIState = HttpUtils.isWifi(MainActivity.this);
        //注册网络监听
        registerReceiver();

        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        mIsLoadPhoto = sp.getBoolean("loadPhoto", false);
        mIsPlayMusic = sp.getBoolean("playMusic", false);
        boolean isNeedWarm = sp.getBoolean("warmData", false);

        initToolbar();
        if (isNeedWarm) {  //流量状态下提醒
            if (HttpUtils.isMobile(MainActivity.this)) {
                showAlertDialog();
            } else {
                initView();
            }
        } else {
            initView();
        }
    }

    private void showAlertDialog() {
        final MaterialDialog mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle(getString(R.string.alert_dialog))
                .setMessage(getString(R.string.main_tips_not_wifi))
                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initView();
                        mMaterialDialog.dismiss();
                    }
                })
        .setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMaterialDialog.dismiss();
                finish();
            }
        });

        mMaterialDialog.show();
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

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupDrawerContent(mNavigationView);

        ArrayList<Fragment> mFragmentLists = new ArrayList<>();
        FanjuFragment mFanjuFragment = new FanjuFragment();
        ReadFragment mReadFragment = new ReadFragment();
        MusicFragment mMusicFragment = new MusicFragment();
        NewsFragment newsFragment = new NewsFragment();
        mFragmentLists.add(newsFragment);
        mFragmentLists.add(mReadFragment);
        mFragmentLists.add(mMusicFragment);
        mFragmentLists.add(mFanjuFragment);

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

    private void setupDrawerContent(final NavigationView mNavigationView) {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navigation_item_main:
                                break;
                            case R.id.navigation_item_book:
                                enterOtherActivity(FindBookActivity.class);
                                break;
                            case R.id.navigation_item_photo:
                                enterOtherActivity(PhotoActivity.class);
                                break;
                            case R.id.navigation_item_weather:
                                enterOtherActivity(WeatherActivity.class);
                                break;
                            case R.id.navigation_item_theme:
                                Intent intent = new Intent(MainActivity.this, ThemeChangeActivity.class);
                                startActivityForResult(intent, 0);
                                break;
                            case R.id.navigation_item_about:
                                enterOtherActivity(AboutActivity.class);
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

    private  void registerReceiver(){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetSateReceiver = new ConnectionChangeReceiver();
        this.registerReceiver(mNetSateReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNavigationView != null) {
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }
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
            case R.id.action_share:
                shareApplication();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareApplication() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            boolean isChangeTheme = data.getBooleanExtra("isChangeTheme", false);
            if (isChangeTheme) {
                ToolUtils.changeToTheme(MainActivity.this, false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mNetSateReceiver);
        super.onDestroy();
    }
}
