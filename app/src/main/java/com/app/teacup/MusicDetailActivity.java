package com.app.teacup;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final int LOAD_DATA_FINISH = 0;
    private static final int LOAD_DATA_ERROR = 1;

    private MusicInfo mMusicInfo;
    private MusicDetailInfo mDetailInfo;
    private TextView mMusicTitle;
    private TextView mMusicType;
    private TextView mMusicContent;
    private LinearLayout mPlayList;
    private TextView mMusicTotal;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    initData();
                    break;
                case LOAD_DATA_ERROR:
                    Toast.makeText(MusicDetailActivity.this, "加载数据失败, 请检查网络",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private CollapsingToolbarLayout mCollapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_music_detail);
        initToolBar();
        initView();
        startLoadData();
    }

    private void startAnimator() {
        int cx = (mCollapsingToolbar.getLeft() + mCollapsingToolbar.getRight()) / 2;
        int cy = (mCollapsingToolbar.getTop() + mCollapsingToolbar.getBottom()) / 2;

        int finalRadius = mCollapsingToolbar.getWidth();

        Animator anim =
                ViewAnimationUtils.createCircularReveal(mCollapsingToolbar, cx, cy, 0, finalRadius);
        anim.start();
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
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if (mCollapsingToolbar != null) {
            String[] split = mMusicInfo.getTitle().split(" ");
            mCollapsingToolbar.setTitle(split[1]);
        }

        ImageView ivImage = (ImageView) findViewById(R.id.iv_music_image);
        if (ivImage != null) {
            Glide.with(this).load(mMusicInfo.getImgUrl())
                    .error(R.drawable.photo_loaderror)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(ivImage);
        }

        mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        mMusicType = (TextView) findViewById(R.id.tv_music_type);
        mMusicContent = (TextView) findViewById(R.id.tv_music_content);
        mMusicTotal = (TextView) findViewById(R.id.tv_music_total);
        mPlayList = (LinearLayout) findViewById(R.id.ll_play_list);
    }

    private void initData() {
        mMusicTitle.setText(mMusicInfo.getTitle());
        mMusicType.setText(mDetailInfo.getType());
        mMusicContent.setText(mDetailInfo.getContent());
        int total = mDetailInfo.getMusicList().size();
        String musicTotal = String.format("   %d%s", total, getString(R.string.music_total_end));
        mMusicTotal.setText(musicTotal);
        initLayout();
    }

    private void initLayout() {
        List<MusicDetail> list = mDetailInfo.getMusicList();
        for (int  i = 0; i < list.size(); i++) {
            View view = View.inflate(MusicDetailActivity.this, R.layout.item_music_detail, null);
            TextView index = (TextView) view.findViewById(R.id.tv_play_index);
            ImageView img = (ImageView) view.findViewById(R.id.iv_play_img);
            TextView name = (TextView) view.findViewById(R.id.tv_play_name);
            TextView user = (TextView) view.findViewById(R.id.tv_play_user);

            index.setText(String.format("%02d", i + 1));
            Glide.with(this).load(list.get(i).getImgUrl())
                    .error(R.drawable.photo_loaderror)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(img);
            name.setText(list.get(i).getMusicName());
            user.setText(list.get(i).getMusicPlayer());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MusicDetailActivity.this, MusicPlayActivity.class);
                    startActivity(intent);
                }
            });
            mPlayList.addView(view);
        }
    }

    private void startLoadData() {
        HttpUtils.sendHttpRequest(mMusicInfo.getNextUrl(),
                new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseMusicData(response);
                sendParseMessage(LOAD_DATA_FINISH);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                sendParseMessage(LOAD_DATA_ERROR);
            }
        });
    }

    private void sendParseMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        mHandler.sendMessage(msg);
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

        String id = mMusicInfo.getTitle().substring(4, 7);
        for (int i = 1; i <= musicList.size(); i++) {
            String num = String.format("%02d", i);
            String musicUrl = "http://luoo-mp3.kssws.ks-cdn.com/low/luoo/radio" +
                    id + "/" + num + ".mp3";
            musicList.get(i - 1).setMusicUrl(musicUrl);
        }

        mDetailInfo.setMusicList(musicList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeMessages(LOAD_DATA_FINISH);
            mHandler.removeMessages(LOAD_DATA_ERROR);
            mHandler = null;
        }
    }
}
