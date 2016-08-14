package com.app.teacup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.bean.Music.MusicDetail;
import com.app.service.MediaService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import info.abdolahi.CircularMusicProgressBar;


public class MusicPlayActivity extends Activity implements View.OnClickListener {

    private CircularMusicProgressBar mProgressBar;
    private TextView mPlayTitle;
    private TextView mPlayAuthor;
    private ImageButton mPreButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ProgressUpdateReceiver mProgressUpdateReceiver;

    private int mCurrPlayPos;
    private List<MusicDetail> mMusicList;
    private Intent mServiceIntent;
    private boolean mIsPlay = true;
    private PLayUpdateReceiver mPLayUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_music_play);
        initView();
        initData();
        initAction();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initAction() {
        mServiceIntent = new Intent(getApplicationContext(), MediaService.class);
        mServiceIntent.putExtra("option", MediaService.OPTION_PLAY);
        mServiceIntent.putExtra("playUrl", mMusicList.get(mCurrPlayPos).getMusicUrl());
        startService(mServiceIntent);

        mProgressUpdateReceiver = new ProgressUpdateReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(MediaService.SEEK_BAR_CHANGE);
        registerReceiver(mProgressUpdateReceiver, filter);

        mPLayUpdateReceiver = new PLayUpdateReceiver();
        IntentFilter playFilter = new IntentFilter();
        playFilter.addAction(MediaService.OPTION_PLAY_NEXT);
        registerReceiver(mPLayUpdateReceiver, playFilter);
    }

    private void initData() {
        Intent intent = getIntent();
        mCurrPlayPos = intent.getIntExtra("position", -1);
        mMusicList = (List<MusicDetail>) intent.getSerializableExtra("musicList");
        mPlayTitle.setText(mMusicList.get(mCurrPlayPos).getMusicName());
        mPlayAuthor.setText(mMusicList.get(mCurrPlayPos).getMusicPlayer());
        Glide.with(this).load(mMusicList.get(mCurrPlayPos).getImgUrl())
                .error(R.drawable.photo_loaderror)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .into(mProgressBar);
    }

    private void initView() {
        mMusicList = new ArrayList<>();

        mProgressBar = (CircularMusicProgressBar) findViewById(R.id.album_art);
        mPlayTitle = (TextView) findViewById(R.id.tv_music_play_title);
        mPlayAuthor = (TextView) findViewById(R.id.tv_music_play_user);
        mPreButton = (ImageButton) findViewById(R.id.ib_pre);
        mPlayButton = (ImageButton) findViewById(R.id.ib_play);
        mNextButton = (ImageButton) findViewById(R.id.ib_next);

        mPreButton.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_pre:
                mCurrPlayPos--;
                if (mCurrPlayPos >= 0) {
                    startPlay(mCurrPlayPos);
                } else {
                    mCurrPlayPos = 0;
                    Toast.makeText(getApplicationContext(), getString(R.string.music_play_pre),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ib_play:
                Intent intent = new Intent();
                intent.setAction(MediaService.MUSIC_SERVICE_ACTION);
                if (mIsPlay) {
                    intent.putExtra("option", MediaService.OPTION_PAUSE);
                    mPlayButton.setImageResource(R.drawable.play);
                    mIsPlay = false;
                } else {
                    intent.putExtra("option", MediaService.OPTION_CONTINUE);
                    mPlayButton.setImageResource(R.drawable.pause);
                    mIsPlay = true;
                }
                sendBroadcast(intent);
                break;
            case R.id.ib_next:
                startPlayNext();
                break;
            default:
                break;
        }
    }

    private void startPlayNext() {
        mCurrPlayPos++;
        if (mCurrPlayPos >= mMusicList.size()) {
            mCurrPlayPos = 0;
        }
        mProgressBar.setValue(0);
        startPlay(mCurrPlayPos);
    }

    private void startPlay(int pos) {
        Intent intent = new Intent();
        intent.setAction("com.media.service.action");
        intent.putExtra("option", MediaService.OPTION_PLAY);
        intent.putExtra("playUrl", mMusicList.get(pos).getMusicUrl());
        sendBroadcast(intent);
        mPlayTitle.setText(mMusicList.get(pos).getMusicName());
        mPlayAuthor.setText(mMusicList.get(pos).getMusicPlayer());
        Glide.with(this).load(mMusicList.get(pos).getImgUrl())
                .error(R.drawable.photo_loaderror)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .into(mProgressBar);
        mPlayButton.setImageResource(R.drawable.pause);
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(mProgressUpdateReceiver);
        unregisterReceiver(mPLayUpdateReceiver);
        stopService(mServiceIntent);
        super.onDestroy();
    }

    public class ProgressUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            float currentPos = intent.getFloatExtra("currentPos", 0.0f);
            mProgressBar.setValue(currentPos);
        }
    }

    public class PLayUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            startPlayNext();
        }
    }

}
