package com.app.teacup.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class LogcatUtils {
    private static LogcatUtils INSTANCE = null;
    private LogDumper mLogDumper = null;
    private String mResult;


    public static LogcatUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LogcatUtils();
        }
        return INSTANCE;
    }

    private LogcatUtils() {
    }

    public void start() {
        mResult = null;
        if (mLogDumper == null)
            mLogDumper = new LogDumper("MediaResourceGetter");
        mLogDumper.start();
    }

    public void stop() {
        mResult = null;
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    public String getResult() {
        return mResult;
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String mCmds = null;
        private String mGrep;
        private final ArrayList<String> mClearLog;

        LogDumper(String grep) {
            mGrep = grep;
            mCmds = "logcat *:e *:i | grep \"(" + mGrep + ")\"";
            //设置命令  logcat -c 清除日志
            mClearLog = new ArrayList<>();
            mClearLog.add("logcat");
            mClearLog.add("-c");

        }

        void stopLogs() {
            mRunning = false;
            try {
                Runtime.getRuntime().exec(mClearLog.toArray(new String[mClearLog.size()]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(mCmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.contains(mGrep)) {
                        mResult = line;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
