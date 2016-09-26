package com.app.teacup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;


public class FlashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        }, 1000);
    }

    private void enterMainPage() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        boolean isFirst = sp.getBoolean("com.app.teacup.GuideActivity", true);
        Intent intent = new Intent();
        if (isFirst) {
            intent.setClass(FlashActivity.this, GuideActivity.class);
        } else {
            intent.setClass(FlashActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
