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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.adapter.TvPlayRecyclerAdapter;
import com.app.bean.movie.MoviePlayInfo;
import com.app.bean.movie.TvItemInfo;
import com.app.util.OkHttpUtils;
import com.app.util.urlUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import hb.xvideoplayer.MxVideoPlayer;
import hb.xvideoplayer.MxVideoPlayerWidget;


public class TVPlayActivity extends BaseActivity {

    private static final String TAG = "TVPlayActivity";
    private List<MoviePlayInfo> mDatas;
    private List<TvItemInfo> mTvDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private WebView mWebView;
    private String mVideoUrl;
    private MxVideoPlayerWidget mxVideoPlayerWidget;
    private TextView mDependText;
    private TextView mTvText;
    private RecyclerView mRecyclerView;
    private LinearLayout mMoreContainer;
    private boolean mIsInitData = false;
    public static int mPlayIndex = 0;

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
        mDatas = new ArrayList<>();
        mTvDatas = new ArrayList<>();
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.tv_srl_refresh);
        mWebView = (WebView) findViewById(R.id.tv_base_webView);
        mxVideoPlayerWidget = (MxVideoPlayerWidget) findViewById(R.id.tv_video_player);
        mTvText = (TextView) findViewById(R.id.tv_series_textView);
        mDependText = (TextView) findViewById(R.id.tv_depend_textview);
        mMoreContainer = (LinearLayout) findViewById(R.id.tv_base_container);
        mRecyclerView = (RecyclerView) findViewById(R.id.tv_numbers_recyclerView);

        mxVideoPlayerWidget.setAllControlsVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
    }

    private void initData() {
        if (mIsInitData) {
            return;
        }
        if (mDatas.isEmpty() && TextUtils.isEmpty(mVideoUrl)) {
            Toast.makeText(TVPlayActivity.this, getString(R.string.refresh_net_error),
                    Toast.LENGTH_SHORT).show();
        } else {
            mRefreshLayout.setEnabled(false);
        }

        if (!TextUtils.isEmpty(mVideoUrl)) {
            String videoName = getIntent().getStringExtra("moviePlayName");
            mxVideoPlayerWidget.startPlay(mVideoUrl, MxVideoPlayer.SCREEN_LAYOUT_NORMAL, videoName);
        } else {
            Toast.makeText(TVPlayActivity.this, getString(R.string.parse_url_error),
                    Toast.LENGTH_SHORT).show();
        }

        if (!mTvDatas.isEmpty()) {
            mTvText.setVisibility(View.VISIBLE);
            setupRecyclerView();
        }
        if (!mDatas.isEmpty()) {
            mDependText.setVisibility(View.VISIBLE);
            for (int i = 0; i < mDatas.size(); i++) {
                loadViewToContainer(i);
            }
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);
        final TvPlayRecyclerAdapter adapter = new TvPlayRecyclerAdapter(TVPlayActivity.this, mTvDatas);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new TvPlayRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPlayIndex = position;
                adapter.notifyDataSetChanged();
            }
        });
    }


    private void loadViewToContainer(final int position) {
        View itemView = View.inflate(TVPlayActivity.this, R.layout.item_movie_play_view, null);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.moive_play_img);
        TextView nameView = (TextView) itemView.findViewById(R.id.movie_play_name);
        TextView timeView = (TextView) itemView.findViewById(R.id.movie_play_addTime);
        MoviePlayInfo info = mDatas.get(position);
        timeView.setText(info.getAddTime());
        nameView.setText(info.getMovieName());
        loadImageResource(info.getImgUrl(), imageView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayIndex = 0;
                Intent intent = new Intent(TVPlayActivity.this, TVPlayActivity.class);
                intent.putExtra("moviePlayUrl", mDatas.get(position).getNextUrl());
                intent.putExtra("moviePlayName", mDatas.get(position).getMovieName());
                startActivity(intent);
            }
        });
        mMoreContainer.addView(itemView);
    }

    private void loadImageResource(String url, ImageView imageView) {
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(TVPlayActivity.this).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(imageView);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(TVPlayActivity.this).load(url).asBitmap()
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
        destroyWebView();
        Toast.makeText(TVPlayActivity.this, getString(R.string.not_have_more_data),
                Toast.LENGTH_SHORT).show();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onLoadDataFinish() {
        destroyWebView();
        mRefreshLayout.setRefreshing(false);
        initData();
        mIsInitData = true;
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
        String videoUrl = getIntent().getStringExtra("moviePlayUrl");
        if (!TextUtils.isEmpty(videoUrl)) {
            OkHttpUtils.getAsyn(videoUrl, new OkHttpUtils.ResultCallback<String>() {

                @Override
                public void onError(Request request, Exception e) {
                    sendParseDataMessage(LOAD_DATA_ERROR);
                }

                @Override
                public void onResponse(String response) {
                    parseVideoData(response);
                }
            });
        }
    }

    private void parseVideoData(String response) {
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
                    mTvDatas.add(tvItemInfo);
                }
                //parse more video info
                Element row = container.getElementsByClass("row").get(0);
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
                    String nextUrl = "http://www.1zdm.com" + replace;
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
                    mDatas.add(info);
                }

                // parse video url
                Element fluid = container.getElementsByClass("container-fluid").get(0);
                Element player = fluid.getElementById("player");
                Element playerSwf = player.getElementById("player_swf");
                String htmlVideoUrl = playerSwf.attr("src");
                parseVideoPlayUrl(htmlVideoUrl);
            }
        } catch (Exception e) {
            Log.i(TAG, "parseBaseData: ====error===" + e.getMessage());
            sendParseDataMessage(LOAD_DATA_ERROR);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void parseVideoPlayUrl(String htmlUrl) {
        if (!TextUtils.isEmpty(htmlUrl) && mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    sendParseDataMessage(LOAD_DATA_FINISH);
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String url = request.getUrl().toString();
                        if (url.startsWith("http") && (url.contains("sid") || url.contains("mp4"))) {
                            mVideoUrl = url;
                        }
                    } else {
                        String url = view.getUrl();
                        if (url.startsWith("http") && url.contains("sid")) {
                            mVideoUrl = url;
                        }
                    }
                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request,
                                            WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    sendParseDataMessage(LOAD_DATA_ERROR);
                }
            });
            mWebView.loadUrl(htmlUrl);
        }
    }

    public void destroyWebView() {
        if (mWebView != null) {
            mWebView.clearHistory();
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
        destroyWebView();
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
