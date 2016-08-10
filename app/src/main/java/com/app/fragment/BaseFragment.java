package com.app.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {

    public static final int REFRESH_START = 0;
    public static final int REFRESH_FINISH = 1;
    public static final int REFRESH_ERROR = 2;
    public static final int LOAD_DATA_FINISH = 3;
    public static final int LOAD_DATA_ERROR = 4;

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (((Activity)getContext()).isFinishing()){
                return;
            }

            switch (msg.what) {
                case REFRESH_START:
                    onRefreshStart();
                    break;
                case REFRESH_FINISH:
                    onRefreshFinish();
                    break;
                case REFRESH_ERROR:
                    onRefreshError();
                    break;
                case LOAD_DATA_FINISH:
                    onLoadDataFinish();
                    break;
                case LOAD_DATA_ERROR:
                    onLoadDataError();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected abstract void onLoadDataError();

    protected abstract void onLoadDataFinish();

    protected abstract void onRefreshError();

    protected abstract void onRefreshFinish();

    protected abstract void onRefreshStart();

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeMessages(REFRESH_START);
            mHandler.removeMessages(REFRESH_FINISH);
            mHandler.removeMessages(REFRESH_ERROR);
            mHandler.removeMessages(LOAD_DATA_FINISH);
            mHandler.removeMessages(LOAD_DATA_ERROR);
            mHandler = null;
        }
    }
}
