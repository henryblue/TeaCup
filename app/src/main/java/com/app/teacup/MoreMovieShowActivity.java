package com.app.teacup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.teacup.adapter.MoreMovieRecyclerAdapter;
import com.app.teacup.bean.movie.MovieItemInfo;
import com.app.teacup.util.OkHttpUtils;
import com.app.teacup.util.ToolUtils;
import com.app.teacup.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class MoreMovieShowActivity extends BaseActivity {

    private static final String TAG = "MoreMovieShowActivity";
    private List<MovieItemInfo> mDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private MoreMovieRecyclerAdapter mMoreRecyclerAdapter;
    private String mMoreBaseUrl;
    private int mLoadIndex = 1;
    private int movieStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_movie_more_view);
        String moreUrl = getIntent().getStringExtra("moreMovieUrl");
        mMoreBaseUrl = moreUrl.replace(".html", "/");
        movieStyle = getIntent().getIntExtra("moreMovieStyle", -1);
        initView();
        setupRecyclerView();
        initToolBar();
        setupRefreshLayout();
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                mLoadIndex = 1;
                startRefreshData();
            }

            @Override
            public void onLoadMore() {
                if (mDatas.size() <= 0) {
                    mRecyclerView.loadMoreComplete();
                } else {
                    startLoadData();
                }
            }
        });
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) findViewById(R.id.base_recycler_view);
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            int movieStyle = getIntent().getIntExtra("moreMovieStyle", 0);
            String[] arrayStyle = getResources().getStringArray(R.array.video_style);
            mToolbar.setTitle(arrayStyle[movieStyle]);
        }
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeColors(ToolUtils.getThemeColorPrimary(this));
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        StartRefreshPage();
    }

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(MoreMovieShowActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mMoreRecyclerAdapter == null) {
                mMoreRecyclerAdapter = new MoreMovieRecyclerAdapter(MoreMovieShowActivity.this, mDatas);
                mRecyclerView.setAdapter(mMoreRecyclerAdapter);
                mMoreRecyclerAdapter.setOnItemClickListener(new MoreMovieRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        enterPlayPage(position, MoviePlayActivity.class);
                    }
                });
            }
        }
    }

    private void enterPlayPage(int position, Class<?> className) {
        MovieItemInfo itemInfo = mDatas.get(position);
        Intent intent = new Intent(MoreMovieShowActivity.this, className);
        intent.putExtra("moviePlayUrl", itemInfo.getNextUrl());
        intent.putExtra("moviePlayName", itemInfo.getMovieName());
        String style = getString(R.string.tv_series);
        if (movieStyle != -1 && (movieStyle == 0 || movieStyle == 4)) {
            style = getString(R.string.video_from);
        }
        intent.putExtra("movieStyle", style);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(MoreMovieShowActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(MoreMovieShowActivity.this, getString(R.string.not_have_more_data),
                Toast.LENGTH_SHORT).show();
        mRecyclerView.loadMoreComplete();
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.loadMoreComplete();
        mMoreRecyclerAdapter.reSetData(mDatas);
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(MoreMovieShowActivity.this, getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setEnabled(false);
        mRecyclerView.refreshComplete();
        initData();
    }

    @Override
    protected void onRefreshStart() {

    }

    private void startLoadData() {
        mLoadIndex++;
        if (mLoadIndex > 50) {
            mRecyclerView.loadMoreComplete();
            Toast.makeText(MoreMovieShowActivity.this, R.string.not_have_more_data,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String url = mMoreBaseUrl + mLoadIndex + ".html";
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

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
        String moreUrl = getIntent().getStringExtra("moreMovieUrl");
        if (!TextUtils.isEmpty(moreUrl)) {
            OkHttpUtils.getAsyn(moreUrl, new OkHttpUtils.ResultCallback<String>() {

                @Override
                public void onError(Request request, Exception e) {
                    sendParseDataMessage(REFRESH_ERROR);
                }

                @Override
                public void onResponse(String response) {
                    parseData(response);
                }
            });
        }

    }

    private void parseData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            try {
                Element container = document.getElementsByClass("container").get(3);
                Element row = container.getElementsByClass("row").get(1);
                Elements rows = row.getElementsByClass("col-xs-6");
                for (Element item : rows) {
                    Element movieItem = item.getElementsByClass("movie-item").get(0);
                    MovieItemInfo itemInfo = parseMovieItemInfo(movieItem);
                    if (itemInfo != null) {
                        mDatas.add(itemInfo);
                    }
                }
                sendParseDataMessage(REFRESH_FINISH);
            } catch (Exception e) {
                Log.i(TAG, "parseMovieData: ====error===" + e.getMessage());
                sendParseDataMessage(REFRESH_ERROR);
            }
        }
    }

    private MovieItemInfo parseMovieItemInfo(Element item) {
        if (item != null) {
            MovieItemInfo itemInfo = new MovieItemInfo();
            Element a = item.getElementsByTag("a").get(0);
            String url = urlUtils.MOVIE_URL + a.attr("href");
            String videoUrl = url.replace("show", "play");
            itemInfo.setNextUrl(videoUrl);
            Element img = a.getElementsByTag("img").get(0);
            String imgUrl = img.attr("src");
            String name = img.attr("alt");
            itemInfo.setImageUrl(imgUrl);
            itemInfo.setMovieName(name);
            String imgIndex = a.getElementsByTag("button").get(0).text();
            itemInfo.setImageIndex(imgIndex);
            return itemInfo;
        } else {
            return null;
        }
    }
}
