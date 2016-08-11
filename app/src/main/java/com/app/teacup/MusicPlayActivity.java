package com.app.teacup;

import android.app.Activity;
import android.os.Bundle;

import info.abdolahi.CircularMusicProgressBar;


public class MusicPlayActivity extends Activity {

    CircularMusicProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_music_play);
        initView();
    }

    private void initView() {
        mProgressBar = (CircularMusicProgressBar) findViewById(R.id.album_art);
        mProgressBar.setValue(40);
    }
}
