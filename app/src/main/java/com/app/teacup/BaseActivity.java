package com.app.teacup;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.app.teacup.util.ToolUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.lang.ref.WeakReference;

public abstract class BaseActivity extends AppCompatActivity {

    public static final int REFRESH_START = 0;
    public static final int REFRESH_FINISH = 1;
    public static final int REFRESH_ERROR = 2;
    public static final int LOAD_DATA_FINISH = 3;
    public static final int LOAD_DATA_ERROR = 4;

    private UpdateHandler mHandler;

    private static class UpdateHandler extends Handler {
        private WeakReference<BaseActivity> mActivity;

        UpdateHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            if (activity.isFinishing()){
                return;
            }

            switch (msg.what) {
                case REFRESH_START:
                    activity.onRefreshStart();
                    break;
                case REFRESH_FINISH:
                    activity.onRefreshFinish();
                    break;
                case REFRESH_ERROR:
                    activity.onRefreshError();
                    break;
                case LOAD_DATA_FINISH:
                    activity.onLoadDataFinish();
                    break;
                case LOAD_DATA_ERROR:
                    activity.onLoadDataError();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToolUtils.onActivityCreateSetTheme(this);
        mHandler = new UpdateHandler(BaseActivity.this);
    }

    public void sendParseDataMessage(int message) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = message;
            mHandler.sendMessage(msg);
        }
    }

    public void postDelayed(Runnable runnable, long millis) {
        mHandler.postDelayed(runnable, millis);
    }

    public void sendParseDataMessageDelayed(int message, long delay) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = message;
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    protected void loadImageResource(ImageView videoImg, String imgUrl) {
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(this).load(imgUrl).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(videoImg);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(this).load(imgUrl).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(videoImg);
            } else {
                videoImg.setImageResource(R.drawable.main_load_bg);
            }
        }
    }

    protected abstract void onLoadDataError();

    protected abstract void onLoadDataFinish();

    protected abstract void onRefreshError();

    protected abstract void onRefreshFinish();

    protected abstract void onRefreshStart();

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

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeMessages(REFRESH_START);
            mHandler.removeMessages(REFRESH_FINISH);
            mHandler.removeMessages(REFRESH_ERROR);
            mHandler.removeMessages(LOAD_DATA_FINISH);
            mHandler.removeMessages(LOAD_DATA_ERROR);
            mHandler = null;
        }
        super.onDestroy();
    }
}
