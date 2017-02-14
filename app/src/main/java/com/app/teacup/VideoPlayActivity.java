package com.app.teacup;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.bean.fanju.FanjuVideoInfo;
import com.app.util.OkHttpUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;


public class VideoPlayActivity extends BaseActivity {

    private static final String TAG = "VideoPlayActivity";
    private List<FanjuVideoInfo> mDatas;
    private LinearLayout mVideoContainer;
    private SwipeRefreshLayout mRefreshLayout;
    private MxVideoPlayerWidget mxVideoPlayerWidget;
    private String mVideoPlayUrl;
    private TextView mXiangGuanText;
    private TextView mContentView;
    private String mVideoContent = "";
    private TextView mVideoIntroduce;
    private FloatingActionButton mSearchBtn;

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
        Toast.makeText(VideoPlayActivity.this, getString(R.string.not_have_more_data),
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
        String videoUrl = "http://www.1zdm.com/play/3953f18033.html";
        if (!TextUtils.isEmpty(videoUrl)) {
            OkHttpUtils.getAsyn(videoUrl, new OkHttpUtils.ResultCallback<String>() {

                @Override
                public void onError(Request request, Exception e) {
                    sendParseDataMessage(LOAD_DATA_ERROR);
                }

                @Override
                public void onResponse(String response) {
                    parseBaseData(response);
                }
            });
        }
    }

    private void parseBaseData(String response) {
        Document document = Jsoup.parse(response);
        try {
            if (document != null) {
                Element container = document.getElementsByClass("container").get(3);
                Element fluid = container.getElementsByClass("container-fluid").get(0);
                Element player = fluid.getElementById("player");
                Element playerSwf = player.getElementById("player_swf");
                String htmlUrl = playerSwf.attr("src");
                if (!TextUtils.isEmpty("https://vs.6no.cc/xml.class.php?guhuo=en6vxGy1hr6YgKRudQO0O0OO0O0O&referer=https://vs.6no.cc/player/player.swf")) {
                    OkHttpUtils.getAsyn(htmlUrl, new OkHttpUtils.ResultCallback<String>() {

                        @Override
                        public void onError(Request request, Exception e) {
                            sendParseDataMessage(LOAD_DATA_ERROR);
                        }

                        @Override
                        public void onResponse(String response) {
                            parseVideoData(response);
                            sendParseDataMessage(LOAD_DATA_FINISH);
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "parseBaseData: ====error===" + e.getMessage());
        }
    }

    private void parseVideoData(String response) {
        Document document = Jsoup.parse(response);
        Log.i(TAG, "parseVideoData: ===response=" +response);
        try {
//            if (document != null) {
//                Element a1 = document.getElementById("a1");
//                Element object = a1.getElementsByTag("object").get(0);
//                Element ckplayer = object.getElementsByTag("embed").get(0);
//                String videoXmlUrl = ckplayer.attr("flashvars");
//                Log.i(TAG, "parseVideoData: ====videoXmlUrl===" + videoXmlUrl);
//            }
        } catch (Exception e) {
            Log.i(TAG, "parseVideoData: ====error===" + e.getMessage());
        }
    }

    private void initData() {
//        if (mDatas.isEmpty() && TextUtils.isEmpty(mVideoPlayUrl)) {
//            Toast.makeText(VideoPlayActivity.this, getString(R.string.refresh_net_error),
//                    Toast.LENGTH_SHORT).show();
//        } else {
//            mRefreshLayout.setEnabled(false);
//        }
//        if (!TextUtils.isEmpty(mVideoPlayUrl)) {
//            mxVideoPlayerWidget.startPlay(mVideoPlayUrl, MxVideoPlayer.SCREEN_LAYOUT_NORMAL,
//                    getIntent().getStringExtra("fanjuVideoName"));
//            mxVideoPlayerWidget.mStartButton.performClick();
//            mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.VISIBLE,
//                    View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
//        }
//
//        if (!TextUtils.isEmpty(mVideoContent)) {
//            mVideoIntroduce.setVisibility(View.VISIBLE);
//            mContentView.setVisibility(View.VISIBLE);
//            mContentView.setText(mVideoContent);
//            if (mVideoContent.contains("http://www.diyidan.com") ||
//                    mVideoContent.contains("http://website.diyidan.net")) {
//                mSearchBtn.setVisibility(View.VISIBLE);
//            }
//        }
//
//        if (!mDatas.isEmpty()) {
//            mXiangGuanText.setVisibility(View.VISIBLE);
//            addVideoInfoToContainer();
//        }
    }

    private void addVideoInfoToContainer() {
        for (final FanjuVideoInfo info : mDatas) {
            View view = View.inflate(VideoPlayActivity.this, R.layout.item_fanju_video_view, null);
            TextView videoName = (TextView) view.findViewById(R.id.fanju_video_name);
            videoName.setText(info.getVideoName());
            ImageView videoImg = (ImageView) view.findViewById(R.id.fanju_video_img);
            loadImageResource(videoImg, info.getImgeUrl());
            mVideoContainer.addView(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(VideoPlayActivity.this, VideoPlayActivity.class);
                        intent.putExtra("fanjuVideoUrl", info.getNextUrl());
                        intent.putExtra("fanjuVideoName", info.getVideoName());
                        startActivity(intent);
                }
            });
        }
    }

    private void loadImageResource(ImageView videoImg, String imgUrl) {
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(this).load(imgUrl).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(videoImg);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(this).load(imgUrl).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(videoImg);
            } else {
                videoImg.setImageResource(R.drawable.main_load_bg);
            }
        }
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mVideoIntroduce = (TextView) findViewById(R.id.tv_video_intr);
        mContentView = (TextView) findViewById(R.id.fanjuvideo_content);
        mVideoContainer = (LinearLayout) findViewById(R.id.fanju_video_container);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        mxVideoPlayerWidget = (MxVideoPlayerWidget) findViewById(R.id.fanju_video_player);
        mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.VISIBLE, View.INVISIBLE);

        mXiangGuanText = (TextView) findViewById(R.id.xiangguan_textview);
        mSearchBtn = (FloatingActionButton) findViewById(R.id.fanju_btn_search);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });
    }

    private void showSearchDialog() {
        final MaterialEditText editText = new MaterialEditText(VideoPlayActivity.this);
        editText.setHint(R.string.input_http);
        editText.setMetTextColor(Color.parseColor("#009688"));
        editText.setPrimaryColor(Color.parseColor("#009688"));
        editText.setMaxCharacters(100);
        editText.setErrorColor(Color.parseColor("#ff0000"));

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this)
                .setTitle(R.string.search_video)
                .setView(editText, 30, 20, 20, 20)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doSearch(editText.getText().toString());
                    }
                });
        builder.create().show();
    }

    private void doSearch(String url) {
        if (url.startsWith("http") && (url.endsWith("detail/1") || url.endsWith("channel=share"))) {
            Intent intent = new Intent(VideoPlayActivity.this, VideoPlayActivity.class);
            intent.putExtra("fanjuVideoUrl", url);
            intent.putExtra("fanjuVideoName", "");
            startActivity(intent);
        } else if (url.startsWith("http") && url.endsWith("detail/1#anchor_1")) {
            Intent intent = new Intent(VideoPlayActivity.this, FanjuNewsActivity.class);
            intent.putExtra("fanjuNewsUrl", url);
            intent.putExtra("fanjuNewsTitle", "");
            intent.putExtra("fanjuNewsUserImgUrl", "");
            intent.putExtra("fanjuNewsUserName", "");
            intent.putExtra("fanjuNewsUserTime", "");
            startActivity(intent);
        } else {
            Toast.makeText(VideoPlayActivity.this,
                    R.string.parse_url_error, Toast.LENGTH_SHORT).show();
        }
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
