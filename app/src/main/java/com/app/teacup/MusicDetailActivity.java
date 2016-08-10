package com.app.teacup;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.app.bean.Music.MusicDetail;
import com.app.bean.Music.MusicDetailInfo;
import com.app.bean.Music.MusicInfo;
import com.app.util.HttpUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MusicDetailActivity extends AppCompatActivity {

    private static final String TAG = "MusicDetailActivity";
    private MusicInfo mMusicInfo;
    private MusicDetailInfo mDetailInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_music_detail);
        initToolBar();
        initView();
        startLoadData();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.music_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    private void initView() {
        mDetailInfo = new MusicDetailInfo();
        mMusicInfo = (MusicInfo) getIntent().getSerializableExtra("music");
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if (collapsingToolbar != null) {
            String[] split = mMusicInfo.getTitle().split(" ");
            collapsingToolbar.setTitle(split[1]);
        }

        ImageView ivImage = (ImageView) findViewById(R.id.iv_music_image);
        Glide.with(this).load(mMusicInfo.getImgUrl())
                .error(R.drawable.photo_loaderror)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .into(ivImage);
    }

    private void initData() {
    }

    private void startLoadData() {
        HttpUtils.sendHttpRequest(mMusicInfo.getNextUrl(), new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseMusicData(response);
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void parseMusicData(String response) {

        Document document = Jsoup.parse(response);

        Element playlist = document.getElementById("luooPlayerPlaylist");
        Element element = playlist.getElementsByClass("w").get(0);
        Element head = element.getElementsByClass("vol-head").get(0);
        Element meta = head.getElementsByClass("vol-meta").get(1);
        mDetailInfo.setType(meta.text());
        Element content = playlist.getElementsByClass("w").get(1);
        mDetailInfo.setContent(content.text());

        List<MusicDetail> musicList = new ArrayList<>();

        Element playList = playlist.getElementsByClass("w").get(2);
        Elements lis = playList.getElementsByTag("li");
        for (Element li : lis) {
            MusicDetail music = new MusicDetail();
            Element trackCover = li.getElementsByClass("track-cover").get(0);
            String musicImg = trackCover.getElementsByTag("img").get(0).attr("src");
            music.setImgUrl(musicImg);
            Element trackName = li.getElementsByClass("track-name").get(0);
            music.setMusicName(trackName.text());
            Element trackMeta = li.getElementsByClass("track-meta").get(0);
            music.setMusicPlayer(trackMeta.text());
            musicList.add(music);
        }
        mDetailInfo.setMusicList(musicList);
    }
}
