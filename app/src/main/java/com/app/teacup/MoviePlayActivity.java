package com.app.teacup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.teacup.adapter.TvPlayRecyclerAdapter;
import com.app.teacup.bean.movie.MoviePlayInfo;
import com.app.teacup.bean.movie.TvItemInfo;
import com.app.teacup.util.LogcatUtils;
import com.app.teacup.util.OkHttpUtils;
import com.app.teacup.util.ToolUtils;
import com.app.teacup.util.urlUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;


public class MoviePlayActivity extends BaseActivity {

    private static final String TAG = "MoviePlayActivity";
    private List<MoviePlayInfo> mMoreDatas;
    private List<TvItemInfo> mTvListDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private WebView mWebView;
    private MxVideoPlayerWidget mxVideoPlayerWidget;
    private TextView mDependText;
    private TextView mTvText;
    private RecyclerView mRecyclerView;
    private LinearLayout mMoreContainer;
    private boolean mIsInitData = false;
    public int mPlayIndex = 0;
    private boolean mIsChangeVideo = false;
    private String mVideoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tv_play_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        initView();
        setupRefreshLayout();
    }

    private void initView() {
        mMoreDatas = new ArrayList<>();
        mTvListDatas = new ArrayList<>();
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.tv_srl_refresh);
        mWebView = new WebView(getApplicationContext());
        mxVideoPlayerWidget = (MxVideoPlayerWidget) findViewById(R.id.tv_video_player);
        mTvText = (TextView) findViewById(R.id.tv_series_textView);
        mDependText = (TextView) findViewById(R.id.tv_depend_textview);
        mMoreContainer = (LinearLayout) findViewById(R.id.tv_base_container);
        mRecyclerView = (RecyclerView) findViewById(R.id.tv_numbers_recyclerView);

        mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        if (getIntent() != null) {
            String style = getIntent().getStringExtra("movieStyle");
            if (!TextUtils.isEmpty(style)) {
                mTvText.setText(style);
            }
        }
    }

    private void initData() {
        if (mIsInitData) { //WebView onPageFinished maybe call two
            return;
        }
        if (mMoreDatas.isEmpty() && TextUtils.isEmpty(mVideoUrl)) {
            Toast.makeText(MoviePlayActivity.this, getString(R.string.refresh_net_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            mRefreshLayout.setEnabled(false);
        }

        if (!TextUtils.isEmpty(mVideoUrl)) {
            String videoName = getIntent().getStringExtra("moviePlayName");
            mxVideoPlayerWidget.startPlay(mVideoUrl, MxVideoPlayer.SCREEN_LAYOUT_NORMAL, videoName);
        } else {
            Toast.makeText(MoviePlayActivity.this, getString(R.string.parse_url_error),
                    Toast.LENGTH_SHORT).show();
        }

        // show video's source or episode
        if (!mTvListDatas.isEmpty()) {
            mTvText.setVisibility(View.VISIBLE);
            setupRecyclerView();
        }
        // load more videos
        if (!mMoreDatas.isEmpty()) {
            mDependText.setVisibility(View.VISIBLE);
            for (int i = 0; i < mMoreDatas.size(); i++) {
                loadViewToContainer(i);
            }
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);
        final TvPlayRecyclerAdapter adapter = new TvPlayRecyclerAdapter(MoviePlayActivity.this, mTvListDatas);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new TvPlayRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mPlayIndex != position) {
                    mIsChangeVideo = true;
                    mPlayIndex = position;
                    adapter.notifyDataSetChanged();
                    final String nextUrl = mTvListDatas.get(position).getNextUrl();
                    MxVideoPlayer.releaseAllVideos();
                    mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE,
                            View.INVISIBLE, View.VISIBLE, View.INVISIBLE, View.INVISIBLE);
                    parseNextPlayUrl(nextUrl);
                }
            }
        });
    }

    private void loadViewToContainer(final int position) {
        View itemView = View.inflate(MoviePlayActivity.this, R.layout.item_movie_play_view, null);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.moive_play_img);
        TextView nameView = (TextView) itemView.findViewById(R.id.movie_play_name);
        TextView timeView = (TextView) itemView.findViewById(R.id.movie_play_addTime);
        MoviePlayInfo info = mMoreDatas.get(position);
        timeView.setText(info.getAddTime());
        nameView.setText(info.getMovieName());
        loadImageResource(info.getImgUrl(), imageView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoviePlayActivity.this, MoviePlayActivity.class);
                intent.putExtra("moviePlayUrl", mMoreDatas.get(position).getNextUrl());
                intent.putExtra("moviePlayName", mMoreDatas.get(position).getMovieName());
                startActivity(intent);
            }
        });
        mMoreContainer.addView(itemView);
    }

    private void loadImageResource(String url, ImageView imageView) {
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(MoviePlayActivity.this).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(imageView);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(MoviePlayActivity.this).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.main_load_bg);
            }
        }
    }

    @Override
    protected void onLoadDataError() {
        mWebView.loadUrl("about:blank");
        LogcatUtils.getInstance().stop();
        Toast.makeText(MoviePlayActivity.this, getString(R.string.not_have_more_data),
                Toast.LENGTH_SHORT).show();
        mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        mRefreshLayout.setRefreshing(false);
        mIsChangeVideo = false;
    }

    @Override
    protected void onLoadDataFinish() {
        mWebView.loadUrl("about:blank");
        LogcatUtils.getInstance().stop();
        mRefreshLayout.setRefreshing(false);
        initData();
        mIsInitData = true;
        if (mIsChangeVideo) {
            if (!TextUtils.isEmpty(mVideoUrl)) {
                String videoName = getIntent().getStringExtra("moviePlayName") +
                        "-" + mTvListDatas.get(mPlayIndex).getName();
                mxVideoPlayerWidget.startPlay(mVideoUrl, MxVideoPlayer.SCREEN_LAYOUT_NORMAL, videoName);
            } else {
                mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                        View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                Toast.makeText(MoviePlayActivity.this, getString(R.string.parse_url_error),
                        Toast.LENGTH_SHORT).show();
            }
            mIsChangeVideo = false;
        }
        mVideoUrl = "";
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
        mRefreshLayout.setColorSchemeColors(ToolUtils.getThemeColorPrimary(this));
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
        mMoreDatas.clear();
        if (getIntent() == null) {
            return;
        }
        String videoUrl = getIntent().getStringExtra("moviePlayUrl");
        if (!TextUtils.isEmpty(videoUrl)) {
            OkHttpUtils.getAsyn(videoUrl, new OkHttpUtils.ResultCallback<String>() {

                @Override
                public void onError(Request request, Exception e) {
                    sendParseDataMessage(LOAD_DATA_ERROR);
                }

                @Override
                public void onResponse(String response) {
                    parseMoreVideoData(response);
                }
            });
        }
    }

    private void parseMoreVideoData(String response) {
        Document document = Jsoup.parse(response);
        try {
            if (document != null) {
                Element container = document.getElementsByClass("container").get(3);
                //parse tv data
                Element colMd = container.getElementsByClass("container-fluid").get(0)
                        .getElementsByClass("col-md-12").get(0);
                Element group = colMd.getElementsByClass("dslist-group").get(0);
                Elements groupItems = group.getElementsByClass("dslist-group-item");
                for (Element groupItem : groupItems) {
                    TvItemInfo tvItemInfo = new TvItemInfo();
                    Element a = groupItem.getElementsByTag("a").get(0);
                    String nextUrl = urlUtils.MOVIE_URL + a.attr("href");
                    tvItemInfo.setNextUrl(nextUrl);
                    String name = a.text();
                    tvItemInfo.setName(name);
                    mTvListDatas.add(tvItemInfo);
                }
                //parse more video info
                Element row = container.getElementsByClass("row").get(1);
                Elements moreMovies = row.getElementsByClass("movie-item-out");
                for (Element movie : moreMovies) {
                    Elements movieItem = movie.getElementsByClass("movie-item");
                    if (movieItem.size() <= 0) {
                        continue;
                    }

                    Element e = movieItem.get(0);
                    Element a = e.getElementsByTag("a").get(0);
                    String url = a.attr("href");
                    String replace = url.replace("show", "play");
                    String nextUrl = urlUtils.MOVIE_URL + replace;
                    String currentLoadUrl = getIntent().getStringExtra("moviePlayUrl");
                    if (currentLoadUrl.equals(nextUrl)) {
                        continue;
                    }
                    MoviePlayInfo info = new MoviePlayInfo();
                    info.setNextUrl(nextUrl);
                    Element img = a.getElementsByTag("img").get(0);
                    String imgUrl = img.attr("src");
                    String movieName = img.attr("alt");
                    info.setImgUrl(imgUrl);
                    info.setMovieName(movieName);
                    String addTime = e.getElementsByClass("meta").get(0)
                            .getElementsByTag("em").get(0).text();
                    info.setAddTime(addTime);
                    mMoreDatas.add(info);
                }
            }
            requestParseVideoUrl();
        } catch (Exception e) {
            Log.i(TAG, "parseBaseData: ====error===" + e.getMessage());
            sendParseDataMessage(LOAD_DATA_ERROR);
        }
    }

    private void requestParseVideoUrl() {
        String videoUrl = getIntent().getStringExtra("moviePlayUrl");
        if (!TextUtils.isEmpty(videoUrl)) {
            OkHttpUtils.getAsynWithHeader(MoviePlayActivity.this, videoUrl,
                    new OkHttpUtils.ResultCallback<String>() {

                        @Override
                        public void onError(Request request, Exception e) {
                            sendParseDataMessage(LOAD_DATA_FINISH);
                        }

                        @Override
                        public void onResponse(String response) {
                            parseVideoUrl(response);
                        }
                    });
        }
    }

    private void parseVideoUrl(String response) {
        Document document = Jsoup.parse(response);
        try {
            Element video = document.getElementsByTag("iframe").first();
            String videoUrl = video.attr("src");
            Log.i(TAG, "parseVideoUrl: ======videoUrl===" + videoUrl);
            parseVideoPlayUrl(videoUrl);
        } catch (Exception e) {
            sendParseDataMessage(LOAD_DATA_FINISH);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void parseVideoPlayUrl(String videoUrl) {
        if (!mIsInitData && !TextUtils.isEmpty(videoUrl) && mWebView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.getSettings().setAllowFileAccess(false);
            mWebView.setWebViewClient(new MyTvWebViewClient(MoviePlayActivity.this));
            LogcatUtils.getInstance().start();
            mWebView.loadUrl(videoUrl);
        } else if (!TextUtils.isEmpty(videoUrl)) {
            LogcatUtils.getInstance().start();
            mWebView.loadUrl(videoUrl);
        } else {
            sendParseDataMessage(LOAD_DATA_FINISH);
        }
    }

    private void parseNextPlayUrl(String nextUrl) {
        if (!TextUtils.isEmpty(nextUrl)) {
            OkHttpUtils.getAsynWithHeader(MoviePlayActivity.this, nextUrl,
                    new OkHttpUtils.ResultCallback<String>() {

                        @Override
                        public void onError(Request request, Exception e) {
                            sendParseDataMessage(LOAD_DATA_FINISH);
                        }

                        @Override
                        public void onResponse(String response) {
                            parseVideoUrl(response);
                        }
                    });
        }
    }

    private void destroyWebView() {
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.stopLoading();
            mWebView.removeAllViews();
            mWebView.clearFormData();
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            mWebView.destroy();
            mWebView = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MxVideoPlayer.releaseAllVideos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogcatUtils.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        destroyWebView();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (MxVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    private void parseVideoUrlFinish(String htmlData) {
        Document document = Jsoup.parse(htmlData);
        try {
            if (document != null) {
                Elements videos = document.getElementsByTag("video");
                LogcatUtils.getInstance().stop();
                if (videos != null) {
                    Element video = videos.first();
                    mVideoUrl = video.attr("src");
                }
            }
        } catch (Exception e) {
            mVideoUrl = "";
        }

        if (TextUtils.isEmpty(mVideoUrl)) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    String result = LogcatUtils.getInstance().getResult();
                    if (!TextUtils.isEmpty(result)) {
                        String[] splitInfo = result.split("url:");
                        mVideoUrl = splitInfo[splitInfo.length - 1];
                    }
                }
            }, 3000);
        } else if (mVideoUrl.endsWith("format=mp4")) {
            String result = LogcatUtils.getInstance().getResult();
            if (!TextUtils.isEmpty(result)) {
                String[] splitInfo = result.split("url:");
                mVideoUrl = splitInfo[splitInfo.length - 1];
            }
        }

        // videoUrl maybe parse equal 'undefined' or 'unknown'
        if (!TextUtils.isEmpty(mVideoUrl) && !mVideoUrl.startsWith("http")) {
            mVideoUrl = "";
        }

        LogcatUtils.getInstance().stop();
        sendParseDataMessage(LOAD_DATA_FINISH);
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) {
            parseVideoUrlFinish(html);
        }
    }

    private static class MyTvWebViewClient extends WebViewClient {
        private WeakReference<MoviePlayActivity> mTvPlayActivity;

        MyTvWebViewClient(MoviePlayActivity activity) {
            mTvPlayActivity = new WeakReference<>(activity);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!url.contains("about:blank")) {
                view.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'" +
                        "+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request,
                                    WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (mTvPlayActivity.get() != null) {
                mTvPlayActivity.get().sendParseDataMessage(LOAD_DATA_ERROR);
            }
        }
    }
}
