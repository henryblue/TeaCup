package com.app.teacup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.bean.Music.MusicDetail;
import com.app.util.ToolUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hrb.library.MiniMusicView;

import java.util.ArrayList;
import java.util.List;

import info.abdolahi.CircularMusicProgressBar;
import me.drakeet.materialdialog.MaterialDialog;


public class MusicPlayActivity extends Activity implements View.OnClickListener {

    private CircularMusicProgressBar mProgressBar;
    private TextView mPlayTitle;
    private TextView mPlayAuthor;
    private ImageButton mPlayButton;

    private int mCurrPlayPos;
    private List<MusicDetail> mMusicList;
    private boolean mIsPlay = true;
    private MiniMusicView mMusicView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToolUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_music_play);
        initView();
        initData();
        startPlay(mCurrPlayPos);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initData() {
        Intent intent = getIntent();
        mCurrPlayPos = intent.getIntExtra("position", -1);
        mMusicList = (List<MusicDetail>) intent.getSerializableExtra("musicList");
    }

    private void initView() {
        mMusicList = new ArrayList<>();
        mMusicView = (MiniMusicView) findViewById(R.id.mmv_play_music);
        View view = View.inflate(MusicPlayActivity.this, R.layout.layout_music_play, null);
        mProgressBar = (CircularMusicProgressBar) view.findViewById(R.id.album_art);
        mPlayTitle = (TextView) view.findViewById(R.id.tv_music_play_title);
        mPlayAuthor = (TextView) view.findViewById(R.id.tv_music_play_user);
        ImageButton mPreButton = (ImageButton) view.findViewById(R.id.ib_pre);
        mPlayButton = (ImageButton) view.findViewById(R.id.ib_play);
        ImageButton mNextButton = (ImageButton) view.findViewById(R.id.ib_next);

        mPreButton.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);

        mMusicView.addView(view);
        mMusicView.setOnMusicStateListener(new OnPlayerMusicStateListener());
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
                if (mIsPlay) {
                    mMusicView.pausePlayMusic();
                    mPlayButton.setImageResource(R.drawable.playmusic);
                    mIsPlay = false;
                } else {
                    mMusicView.resumePlayMusic();
                    mPlayButton.setImageResource(R.drawable.pausemusic);
                    mIsPlay = true;
                }
                break;
            case R.id.ib_next:
                startPlayNext();
                break;
            default:
                break;
        }
    }

    private void startPlayNext() {
        mProgressBar.setValue(0);
        mCurrPlayPos++;
        if (mCurrPlayPos >= mMusicList.size()) {
            mCurrPlayPos = 0;
        }
        startPlay(mCurrPlayPos);
    }

    private void startPlay(int pos) {
        mPlayTitle.setText(mMusicList.get(pos).getMusicName());
        mPlayAuthor.setText(mMusicList.get(pos).getMusicPlayer());
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(this).load(mMusicList.get(pos).getImgUrl())
                    .error(R.drawable.photo_loaderror)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(mProgressBar);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(this).load(mMusicList.get(pos).getImgUrl())
                        .error(R.drawable.photo_loaderror)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .into(mProgressBar);
            } else {
                mProgressBar.setImageResource(R.drawable.photo_default);
            }
        }
        mPlayButton.setImageResource(R.drawable.pausemusic);

        if (!MainActivity.mIsPlayMusic) {
            if (!MainActivity.mIsWIFIState) {
                showAlertDialog();
                return;
            }
        }

        mMusicView.startPlayMusic(mMusicList.get(pos).getMusicUrl());
    }

    private void showAlertDialog() {
        final MaterialDialog mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle(getString(R.string.alert_dialog))
                .setMessage(getString(R.string.music_tips_not_wifi))
                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
        mMaterialDialog.setCanceledOnTouchOutside(true);
        mMaterialDialog.show();
    }

    @Override
    protected void onDestroy() {
        mMusicView.stopPlayMusic();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class OnPlayerMusicStateListener implements MiniMusicView.OnMusicStateListener {

        @Override
        public void onPrepared(int i) {
        }

        @Override
        public void onError(int what, int extra) {
            Toast.makeText(MusicPlayActivity.this,
                    getString(R.string.load_music_error), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onInfo(int what, int extra) {

        }

        @Override
        public void onMusicPlayComplete() {
            startPlayNext();
        }

        @Override
        public void onSeekComplete() {
        }

        @Override
        public void onProgressUpdate(int duration, int currPos) {
            mProgressBar.setValue(currPos * 100 / duration);
        }

        @Override
        public void onHeadsetPullOut() {
            if (mIsPlay) {
                mPlayButton.setImageResource(R.drawable.playmusic);
                mIsPlay = false;
                mMusicView.pausePlayMusic();
            }
        }
    }
}
