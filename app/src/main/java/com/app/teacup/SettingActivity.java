package com.app.teacup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.app.ui.SettingItemView;


public class SettingActivity extends AppCompatActivity {

    private SettingItemView mSaveData;
    private SettingItemView mWarmSetting;
    private SettingItemView mPlayMusic;
    private SettingItemView mCleanCache;
    private SharedPreferences mSps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mSaveData.setChecked(mSps.getBoolean("loadPhoto", false));
        mWarmSetting.setChecked(mSps.getBoolean("warmData", false));
        mPlayMusic.setChecked(mSps.getBoolean("playMusic", false));

        //save data stream
        mSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = mSps.edit();
                if (mSaveData.isChecked()) {
                    edit.putBoolean("loadPhoto", false);
                } else {
                    edit.putBoolean("loadPhoto", true);
                }
                edit.commit();
                mSaveData.setChecked(!mSaveData.isChecked());
            }
        });

        //data stream warming
        mWarmSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = mSps.edit();
                if (mWarmSetting.isChecked()) {
                    edit.putBoolean("warmData", false);
                } else {
                    edit.putBoolean("warmData", true);
                }
                edit.commit();
                mWarmSetting.setChecked(!mWarmSetting.isChecked());
            }
        });

        //play music at WIFI mode
        mPlayMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = mSps.edit();
                if (mPlayMusic.isChecked()) {
                    edit.putBoolean("playMusic", false);
                } else {
                    edit.putBoolean("playMusic", true);
                }
                edit.commit();
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
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.settings));
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
