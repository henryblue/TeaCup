package com.app.teacup.util;


import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtils {

    private ThreadPoolExecutor mExecutor;
    private static ThreadPoolUtils mInstance = new ThreadPoolUtils();

    public static ThreadPoolUtils getInstance() {
        return mInstance;
    }

    private ThreadPoolUtils() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        mExecutor = new ThreadPoolExecutor(
                corePoolSize, corePoolSize, 1,
                TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public void execute(Runnable runnable){
        if(runnable==null)return;

        mExecutor.execute(runnable);
    }

    public void remove(Runnable runnable){
        if(runnable ==null)
            return;

        mExecutor.remove(runnable);
    }
}
