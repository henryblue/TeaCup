package com.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.teacup.MainActivity;
import com.app.util.HttpUtils;


public class ConnectionChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.mIsWIFIState = HttpUtils.isWifi(context);
    }
}
