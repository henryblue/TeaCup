package com.app.teacup;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;


public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
    }
}
