package com.app.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.app.teacup.R;


public class MediaService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener {

    public static final int OPTION_PLAY = 0;
    public static final int OPTION_PAUSE = 1;
    public static final int OPTION_CONTINUE = 2;
    public static final String SEEK_BAR_CHANGE = "com.media.update_progress";
    public static final String MUSIC_SERVICE_ACTION = "com.media.service.action";
    public static final String OPTION_PLAY_NEXT = "com.media.service.update.action";

    private static MediaPlayer mMediaPlayer;
    private static ProgressTask mProgressTask;
    private int mCurrPlayPosition;
    private String mPlayUrl;
    private MusicServiceReceiver mReceiver;

    private class MusicServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int option = intent.getIntExtra("option", -1);
            switch (option) {
                case OPTION_PLAY:
                    mPlayUrl = intent.getStringExtra("playUrl");
                    play(mPlayUrl);
                    break;
                case OPTION_PAUSE:
                    mCurrPlayPosition = mMediaPlayer.getCurrentPosition();
                    pause();
                    break;
                case OPTION_CONTINUE:
                    playerToPosition(mCurrPlayPosition);
                    if (TextUtils.isEmpty(mPlayUrl)) {
                        mPlayUrl = intent.getStringExtra("playUrl");
                        play(mPlayUrl);
                    } else {
                        mMediaPlayer.start();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
        }
        mReceiver = new MusicServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_SERVICE_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void play(String path) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
            if (mProgressTask == null) {
                mProgressTask = new ProgressTask();
                mProgressTask.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    private void stopMediaPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void playerToPosition(int position) {

        if (position > 0 && position < mMediaPlayer.getDuration()) {
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void onDestroy() {
        stopMediaPlay();
        unregisterReceiver(mReceiver);
        if (mProgressTask != null) {
            mProgressTask.stopProgressUpdate();
            mProgressTask = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent mediaIntent = new Intent();
        mediaIntent.setAction(MUSIC_SERVICE_ACTION);
        mediaIntent.putExtra("option", intent.getIntExtra("option", -1));
        mediaIntent.putExtra("playUrl", intent.getStringExtra("playUrl"));
        sendBroadcast(mediaIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp != null) {
            mp.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intent = new Intent();
        intent.setAction(OPTION_PLAY_NEXT);
        sendBroadcast(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), getString(R.string.load_music_error),
                Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private class ProgressTask extends AsyncTask<Void, Void, Void> {

        private boolean mIsUpdate = true;
        @Override
        protected Void doInBackground(Void... params) {
            while (mIsUpdate) {
                SystemClock.sleep(1000);
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                Intent intent = new Intent();
                intent.setAction(SEEK_BAR_CHANGE);
                float pos  = mMediaPlayer.getCurrentPosition() * 100 / mMediaPlayer.getDuration();
                intent.putExtra("currentPos", pos);
                sendBroadcast(intent);
            }
            super.onProgressUpdate(values);
        }

        public void stopProgressUpdate() {
            mIsUpdate = false;
        }
    }
}
