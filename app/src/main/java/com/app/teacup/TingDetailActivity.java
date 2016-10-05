package com.app.teacup;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
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

import me.drakeet.materialdialog.MaterialDialog;


public class TingDetailActivity extends BaseActivity {

    private MusicInfo mMusicInfo;
    private TextView mDetail;
    private LinearLayout mContent;
    private MiniMusicView mMusicView;
    private String mAudioUrl;
    private List<String> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ting_detail);
        mDatas = new ArrayList<>();
        initToolBar();
        initView();
        startLoadData();
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(TingDetailActivity.this, getString(R.string.refresh_net_error),
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

        mDetail = (TextView) findViewById(R.id.tv_ting_detail_type);
        mContent = (LinearLayout) findViewById(R.id.tv_ting_detail_content);
        mMusicView = (MiniMusicView) findViewById(R.id.chl_music_view);
    }

    private void initData() {
        mDetail.setText(mMusicInfo.getInfoNum());
        mMusicView.setTitleText(mMusicInfo.getTitle());
        mMusicView.setVisibility(View.VISIBLE);
        initLayout();
        if (!MainActivity.mIsPlayMusic) {
            if (!MainActivity.mIsWIFIState) {
                showAlertDialog();
                return;
            }
        }
        mMusicView.startPlayMusic(mAudioUrl);
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
                sendParseDataMessage(LOAD_DATA_FINISH);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                sendParseDataMessage(LOAD_DATA_ERROR);
            }
        });
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
        if (mMusicView != null) {
            mMusicView.stopPlayMusic();
        }
    }
}
