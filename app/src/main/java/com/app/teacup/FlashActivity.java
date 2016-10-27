package com.app.teacup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.app.util.ToolUtils;

public class FlashActivity extends Activity {

    private static final int LAUNCHER_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        ToolUtils.onActivityCreateSetTheme(FlashActivity.this);
        setContentView(R.layout.layout_flash_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enterMainPage();
            }
        }, LAUNCHER_TIME);
    }

    private void enterMainPage() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        boolean isFirst = sp.getBoolean("com.app.teacup.GuideActivity", true);
        Intent intent = new Intent();
        if (isFirst) {
            intent.setClass(FlashActivity.this, GuideActivity.class);
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("com.app.teacup.GuideActivity", false);
            edit.apply();
        } else {
            intent.setClass(FlashActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
