package com.app.teacup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.app.adapter.FanjuVideoRecyclerAdapter;
import com.app.bean.fanju.FanjuVideoInfo;
import com.app.util.OkHttpUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import hb.xvideoplayer.MxTvPlayerWidget;
import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;


public class FanjuVideoActivity extends BaseActivity {

    private List<FanjuVideoInfo> mDatas;
    private XRecyclerView mRecyclerView;
    private FanjuVideoRecyclerAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private MxVideoPlayerWidget mxVideoPlayerWidget;
    private String mVideoPlayUrl;
    private TextView mXiangGuanText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fanjuvideo_view);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        initView();
        setupRefreshLayout();
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(FanjuVideoActivity.this, getString(R.string.not_have_more_data),
                Toast.LENGTH_SHORT).show();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onLoadDataFinish() {
        mRefreshLayout.setRefreshing(false);
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

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        StartRefreshPage();
    }

    private void StartRefreshPage() {
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                startRefreshData();
            }
        });
    }

    private void startRefreshData() {
        mDatas.clear();
        String videoUrl = getIntent().getStringExtra("fanjuVideoUrl");
        if (!TextUtils.isEmpty(videoUrl)) {
            OkHttpUtils.getAsyn(videoUrl, new OkHttpUtils.ResultCallback<String>() {

                @Override
                public void onError(Request request, Exception e) {
                    sendParseDataMessage(LOAD_DATA_ERROR);
                }

                @Override
                public void onResponse(String response) {
                    parseData(response);
                    sendParseDataMessage(LOAD_DATA_FINISH);
                }
            });
        }

    }

    private void parseData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element userVideo = document.getElementsByClass("user_video").get(0);
            Element videoFrame = userVideo.getElementsByClass("video_frame").get(0);
            Element danMu = videoFrame.getElementsByClass("danmu-div").get(0);
            mVideoPlayUrl = danMu.getElementsByTag("video").get(0).attr("src");

            Element videoSection = document.getElementsByClass("video_section").get(0);
            Element contentVideo = videoSection.getElementsByClass("content_video").get(0);
            Element recVideo = contentVideo.getElementById("rec_video");
            Element replyVideo = recVideo.getElementsByClass("reply_video").get(0);
            Elements lis = replyVideo.getElementsByTag("ul").get(0).getElementsByTag("li");
            for (Element li : lis) {
                FanjuVideoInfo info = new FanjuVideoInfo();
                Element a = li.getElementsByTag("a").get(0);
                String nextUrl = "http://www.diyidan.com" + a.attr("href");
                Element img = a.getElementsByTag("img").get(0);
                String title = img.attr("alt");
                String imgUrl = img.attr("src");
                info.setNextUrl(nextUrl);
                info.setVideoName(title);
                info.setImgeUrl(imgUrl);
                mDatas.add(info);
            }
        }
    }

    private void initData() {
        if (mDatas.isEmpty() && TextUtils.isEmpty(mVideoPlayUrl)) {
            Toast.makeText(FanjuVideoActivity.this, getString(R.string.refresh_net_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            mRefreshLayout.setEnabled(false);
        }
        if (!TextUtils.isEmpty(mVideoPlayUrl)) {
            //loadVideoBackgroundImg();
            mxVideoPlayerWidget.startPlay(mVideoPlayUrl, MxVideoPlayer.SCREEN_LAYOUT_NORMAL,
                    getIntent().getStringExtra("fanjuVideoName"));
            mxVideoPlayerWidget.mStartButton.performClick();
            mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
                    View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
        }

        if (!mDatas.isEmpty()) {
            mXiangGuanText.setVisibility(View.VISIBLE);
            if (mAdapter == null) {
                mAdapter = new FanjuVideoRecyclerAdapter(FanjuVideoActivity.this, mDatas);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new FanjuVideoRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        FanjuVideoInfo fanjuInfo = mDatas.get(position);
                        Intent intent = new Intent(FanjuVideoActivity.this, FanjuVideoActivity.class);
                        intent.putExtra("fanjuVideoUrl", fanjuInfo.getNextUrl());
                        intent.putExtra("fanjuVideoName", fanjuInfo.getVideoName());
                        intent.putExtra("fanjuVideoImgUrl", fanjuInfo.getImgeUrl());
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mRecyclerView = (XRecyclerView) findViewById(R.id.base_recycler_view);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        mxVideoPlayerWidget = (MxVideoPlayerWidget) findViewById(R.id.fanju_video_player);
        mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.VISIBLE, View.INVISIBLE);

        mXiangGuanText = (TextView) findViewById(R.id.xiangguan_textview);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreEnabled(false);
        mRecyclerView.setPullRefreshEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MxVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        if (MxVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }
}
