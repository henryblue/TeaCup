package com.app.teacup;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.bean.Music.MusicInfo;
import com.app.util.HttpUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hrb.library.MiniMusicView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class TingDetailActivity extends AppCompatActivity {

    private static final int LOAD_DATA_FINISH = 0;
    private static final int LOAD_DATA_ERROR = 1;

    private MusicInfo mMusicInfo;
    private TextView mDetail;
    private LinearLayout mContent;
    private MiniMusicView mMusicView;
    private String mAudioUrl;
    private List<String> mDatas;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    initData();
                    break;
                case LOAD_DATA_ERROR:
                    Toast.makeText(TingDetailActivity.this, getString(R.string.refresh_net_error),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ting_detail);
        mDatas = new ArrayList<>();
        initToolBar();
        initView();
        startLoadData();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.ting_toolbar);
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
        mMusicInfo = (MusicInfo) getIntent().getSerializableExtra("ting");
        CollapsingToolbarLayout mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        if (mCollapsingToolbar != null) {
            mCollapsingToolbar.setTitle(mMusicInfo.getTitle());
        }

        ImageView ivImage = (ImageView) findViewById(R.id.iv_ting_image);
        if (ivImage != null) {
            if (MainActivity.mIsLoadPhoto) {
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

        mDetail = (TextView) findViewById(R.id.tv_ting_detail_type);
        mContent = (LinearLayout) findViewById(R.id.tv_ting_detail_content);
        mMusicView = (MiniMusicView) findViewById(R.id.chl_music_view);
    }

    private void initData() {
        mDetail.setText(mMusicInfo.getInfoNum());
        mMusicView.setTitleText(mMusicInfo.getTitle());
        mMusicView.setVisibility(View.VISIBLE);
        initLayout();
        mMusicView.startPlayMusic(mAudioUrl);
    }

    private void initLayout() {
        int left = getResources().getDimensionPixelOffset(R.dimen.news_detail_item_marginLeft);
        int right = getResources().getDimensionPixelOffset(R.dimen.news_detail_item_marginRight);
        int bottom = getResources().getDimensionPixelOffset(R.dimen.news_detail_item_marginBottom);
        int textSize = getResources().getDimensionPixelSize(R.dimen.news_detail_item_txt_textSize);

        LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtParams.setMargins(left, 0, right, bottom);

        for (int i = 0; i < mDatas.size(); i++) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(txtParams);
            textView.setText(mDatas.get(i));
            textView.setTextColor(Color.parseColor("#aa000000"));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            textView.setLineSpacing(0, 1.45f);
            mContent.addView(textView);
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
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = what;
            mHandler.sendMessage(msg);
        }
    }

    private void parseMusicData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element content = document.getElementById("content");
            Element post = content.getElementsByClass("post").get(0);
            Element singlePos = post.getElementsByClass("single-post-content").get(0);
            Elements ps = singlePos.getElementsByTag("p");
            for (Element p : ps) {
                if (!TextUtils.isEmpty(p.text())) {
                    mDatas.add(p.text());
                }
            }

            Element yueTing = singlePos.getElementsByClass("yueting-skin").get(0);
            Element player = yueTing.getElementsByClass("pro-small-player").get(0);
            Element mediaElement = player.getElementsByClass("wp-audio-shortcode").get(0);
            mAudioUrl = mediaElement.getElementsByTag("a").get(0).text();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeMessages(LOAD_DATA_FINISH);
            mHandler.removeMessages(LOAD_DATA_ERROR);
            mHandler = null;
        }

        if (mMusicView != null) {
            mMusicView.stopPlayMusic();
        }
    }
}
