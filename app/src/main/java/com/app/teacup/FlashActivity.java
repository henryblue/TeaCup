package com.app.teacup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.app.util.OkHttpUtils;
import com.app.util.ToolUtils;
import com.app.util.urlUtils;
import com.squareup.okhttp.Request;

import java.io.FileOutputStream;

public class FlashActivity extends Activity {

    private static final int PRE_LOAD_DATA_FINISH = 0;
    private static final int PRE_LOAD_DATA_ERROR = 1;
    private static final String TAG = "FlashActivity";

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PRE_LOAD_DATA_FINISH:
                case PRE_LOAD_DATA_ERROR:
                    if (msg.arg1 == 1) {
                        enterMainPage();
                    } else if (msg.arg1 == 0){
                        preloadMoviesData();
                    }
                    break;
                default:
                    break;
            }
        }
    };

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
        preloadNewsData();
    }

    private void preloadNewsData() {
        OkHttpUtils.getAsyn(urlUtils.NEWS_JIANDAN_URL, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(PRE_LOAD_DATA_ERROR, 0);
            }

            @Override
            public void onResponse(String response) {
                try {
                    FileOutputStream outputStream = openFileOutput(getString(R.string.news_cache_name),
                            MODE_PRIVATE);
                    outputStream.write(response.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    Log.i(TAG, "preloadNewsData: error==" + e.getMessage());
                }
                sendParseDataMessage(PRE_LOAD_DATA_FINISH, 0);
            }
        });
    }

    private void preloadMoviesData() {
        OkHttpUtils.getAsyn(urlUtils.MOVIE_URL, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(PRE_LOAD_DATA_ERROR, 1);
            }

            @Override
            public void onResponse(String response) {
                try {
                    FileOutputStream outputStream = openFileOutput(getString(R.string.movies_cache_name),
                            MODE_PRIVATE);
                    outputStream.write(response.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    Log.i(TAG, "preloadMoviesData: error==" + e.getMessage());
                }
                sendParseDataMessage(PRE_LOAD_DATA_FINISH, 1);
            }
        });
    }

    private void sendParseDataMessage(int message, int arg1) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = message;
            msg.arg1 = arg1;
            mHandler.sendMessage(msg);
        }
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

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeMessages(PRE_LOAD_DATA_FINISH);
            mHandler.removeMessages(PRE_LOAD_DATA_ERROR);
            mHandler = null;
        }
        super.onDestroy();
    }

}
