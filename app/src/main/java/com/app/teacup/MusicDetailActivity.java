package com.app.teacup;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.teacup.bean.Music.MusicDetail;
import com.app.teacup.bean.Music.MusicDetailInfo;
import com.app.teacup.bean.Music.MusicInfo;
import com.app.teacup.ui.MoreTextView;
import com.app.teacup.util.OkHttpUtils;
import com.app.teacup.util.urlUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MusicDetailActivity extends BaseActivity {

    private MusicInfo mMusicInfo;
    private MusicDetailInfo mDetailInfo;
    private TextView mMusicTitle;
    private TextView mMusicType;
    private MoreTextView mMusicContent;
    private LinearLayout mPlayList;
    private TextView mMusicTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_music_detail);
        initToolBar();
        initView();
        startLoadData();
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(MusicDetailActivity.this, getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onLoadDataFinish() {
        initData();
    }

    @Override
    protected void onRefreshError() {

    }

    @Override
    protected void onRefreshFinish() {

    }

    @Override
    protected void onRefreshStart() {

    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.music_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        CollapsingToolbarLayout mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if (mCollapsingToolbar != null) {
            String[] split = mMusicInfo.getTitle().split(" ");
            mCollapsingToolbar.setTitle(split[1]);
        }

        ImageView ivImage = (ImageView) findViewById(R.id.iv_music_image);
        if (ivImage != null) {
            if (!MainActivity.mIsLoadPhoto) {
                Glide.with(this).load(mMusicInfo.getImgUrl())
                        .error(R.drawable.photo_loaderror)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .into(ivImage);
            } else {
                if (MainActivity.mIsWIFIState) {
                    Glide.with(this).load(mMusicInfo.getImgUrl())
                            .error(R.drawable.photo_loaderror)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade()
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.main_load_bg);
                }
            }
        }

        mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        mMusicType = (TextView) findViewById(R.id.tv_music_type);
        mMusicContent = (MoreTextView) findViewById(R.id.tv_music_content);
        mMusicTotal = (TextView) findViewById(R.id.tv_music_total);
        mPlayList = (LinearLayout) findViewById(R.id.ll_play_list);
    }

    private void initData() {
        mMusicTitle.setText(mMusicInfo.getTitle());
        mMusicType.setText(mDetailInfo.getType());
        mMusicContent.setContent(mDetailInfo.getContent());
        int total = mDetailInfo.getMusicList().size();
        String musicTotal = String.format(Locale.getDefault(), "   %d%s",
                total, getString(R.string.music_total_end));
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

            index.setText(String.format(Locale.getDefault(), "%02d", i + 1));
            if (!MainActivity.mIsLoadPhoto) {
                Glide.with(this).load(list.get(i).getImgUrl())
                        .error(R.drawable.photo_loaderror)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .into(img);
            } else {
                if (MainActivity.mIsWIFIState) {
                    Glide.with(this).load(list.get(i).getImgUrl())
                            .error(R.drawable.photo_loaderror)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade()
                            .into(img);
                } else {
                    img.setImageResource(R.drawable.main_load_bg);
                }
            }
            name.setText(list.get(i).getMusicName());
            user.setText(list.get(i).getMusicPlayer());

            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MusicDetailActivity.this, MusicPlayActivity.class);
                    intent.putExtra("position", finalI);
                    intent.putExtra("musicList", (Serializable) mDetailInfo.getMusicList());
                    startActivity(intent);
                }
            });
            mPlayList.addView(view);
        }
    }

    private void startLoadData() {
        OkHttpUtils.getAsyn(mMusicInfo.getNextUrl(), new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parseMusicData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }
        });
    }

    private void parseMusicData(String response) {
        Document document = Jsoup.parse(response);
        Element name = document.getElementsByClass("vol-name").get(0);
        Element title = name.getElementsByClass("vol-title").get(0);
        Element dec = document.getElementsByClass("vol-desc").get(0);
        mDetailInfo.setType(title.text());
        mDetailInfo.setContent(dec.text());

        Element playlist = document.getElementById("luooPlayerPlaylist");
        List<MusicDetail> musicList = new ArrayList<>();

        Elements lis = playlist.getElementsByTag("li");
        try {
            for (Element wrapper : lis) {
                Element li = wrapper.getElementsByClass("track-wrapper").get(0);
                MusicDetail music = new MusicDetail();
                Element trackCover = li.getElementsByClass("btn-action-share").get(0);
                music.setImgUrl(trackCover.attr("data-img"));
                Element trackName = li.getElementsByClass("trackname").get(0);
                music.setMusicName(trackName.text());
                Element trackMeta = li.getElementsByClass("artist").get(0);
                music.setMusicPlayer(trackMeta.text());
                musicList.add(music);
            }
        } catch (Exception e) {
            // do not process
        }


        String id = mMusicInfo.getTitle().substring(4, 7);
        for (int i = 1; i <= musicList.size(); i++) {
            String num = String.format(Locale.getDefault(), "%02d", i);
            String musicUrl = urlUtils.MUSIC_PLAYER_URL + id + "/" + num + ".mp3";
            musicList.get(i - 1).setMusicUrl(musicUrl);
        }

        mDetailInfo.setMusicList(musicList);
    }

}
