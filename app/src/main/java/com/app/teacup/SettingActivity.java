package com.app.teacup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.ui.SettingItemView;
import com.app.util.ToolUtils;


public class SettingActivity extends AppCompatActivity {

    private SettingItemView mSaveData;
    private SettingItemView mWarmSetting;
    private SettingItemView mPlayMusic;
    private SettingItemView mCleanCache;
    private SharedPreferences mSps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToolUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_setting);
        mSps = getSharedPreferences("config", MODE_PRIVATE);
        initToolBar();
        initView();
    }

    private void initView() {
        mSaveData = (SettingItemView) findViewById(R.id.siv_save_data_stream);
        mWarmSetting = (SettingItemView) findViewById(R.id.siv_warm_data_stream);
        mPlayMusic = (SettingItemView) findViewById(R.id.siv_play_music);
        mCleanCache = (SettingItemView) findViewById(R.id.siv_clean_cache);

        try {
            String cacheSize = ToolUtils.getTotalCacheSize(SettingActivity.this);
            mCleanCache.setContent(getString(R.string.cache_size) + cacheSize);
        } catch (Exception e) {
            mCleanCache.setContent(getString(R.string.cache_size) + "0.00 B");
        }

        mSaveData.setChecked(mSps.getBoolean("loadPhoto", false));
        mWarmSetting.setChecked(mSps.getBoolean("warmData", false));
        mPlayMusic.setChecked(mSps.getBoolean("playMusic", false));
        final SharedPreferences.Editor edit = mSps.edit();
        //save data stream
        mSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mSaveData.isChecked()) {
                    edit.putBoolean("loadPhoto", false);
                } else {
                    edit.putBoolean("loadPhoto", true);
                }
                edit.apply();
                mSaveData.setChecked(!mSaveData.isChecked());
            }
        });

        //data stream warming
        mWarmSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWarmSetting.isChecked()) {
                    edit.putBoolean("warmData", false);
                } else {
                    edit.putBoolean("warmData", true);
                }
                edit.apply();
                mWarmSetting.setChecked(!mWarmSetting.isChecked());
            }
        });

        //play music at WIFI mode
        mPlayMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayMusic.isChecked()) {
                    edit.putBoolean("playMusic", false);
                } else {
                    edit.putBoolean("playMusic", true);
                }
                edit.apply();
                mPlayMusic.setChecked(!mPlayMusic.isChecked());
            }
        });

        //clean cache
        mCleanCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleanCache();
            }
        });
    }

    private void cleanCache() {
        ToolUtils.clearAllCache(SettingActivity.this);
        try {
            String cacheSize = ToolUtils.getTotalCacheSize(SettingActivity.this);
            mCleanCache.setContent(getString(R.string.cache_size) + cacheSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(SettingActivity.this, getString(R.string.clean_cache_finish),
                Toast.LENGTH_SHORT).show();
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.settings));
        }
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
