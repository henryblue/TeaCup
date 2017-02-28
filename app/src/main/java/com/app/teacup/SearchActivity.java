package com.app.teacup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.adapter.MoreMovieRecyclerAdapter;
import com.app.bean.movie.MovieItemInfo;
import com.app.util.OkHttpUtils;
import com.app.util.ToolUtils;
import com.app.util.urlUtils;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends BaseActivity {

    private static final String TAG = "SearchActivity";
    private List<MovieItemInfo> mDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private MoreMovieRecyclerAdapter mMoreRecyclerAdapter;
    private String mSearchName;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search_view);
        initView();
        setupSearchView();
        setupRecyclerView();
        setupRefreshLayout();
    }

    private void setupSearchView() {
        mSearchView.onActionViewExpanded();
        mSearchView.setQueryHint(getString(R.string.search_video_hint));
        // change font color
        SearchView.SearchAutoComplete textView = (SearchView.SearchAutoComplete)
                mSearchView.findViewById(R.id.search_src_text);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(ContextCompat.getColor(this, R.color.alpha_white));

        //光标颜色
        try {
            Field mCursorDrawableRes=TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(textView, R.drawable.cursor_color);
        } catch (Exception e){
            Log.i(TAG, "setupSearchView: set color error==" + e.getMessage());
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String queryText) {
                if (mSearchView != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(
                                mSearchView.getWindowToken(), 0);
                    }
                    mSearchView.clearFocus();
                    mSearchName = queryText;
                    StartRefreshPage();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,
                StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreEnabled(false);
        mRecyclerView.setPullRefreshEnabled(false);
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.search_srl_refresh);
        mRecyclerView = (XRecyclerView) findViewById(R.id.search_base_recycler_view);
        ImageView mBackView = (ImageView) findViewById(R.id.search_back_img);
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSearchView = (SearchView) findViewById(R.id.search_search_view);
    }


    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeColors(ToolUtils.getThemeColorPrimary(this));
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
    }

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(SearchActivity.this, getString(R.string.nothing_find),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mMoreRecyclerAdapter == null) {
                mMoreRecyclerAdapter = new MoreMovieRecyclerAdapter(SearchActivity.this, mDatas);
                mRecyclerView.setAdapter(mMoreRecyclerAdapter);
                mMoreRecyclerAdapter.setOnItemClickListener(new MoreMovieRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String index = mDatas.get(position).getImageIndex();
                        boolean isTv = index.contains(getString(R.string.whether_tv_tip));
                        if (isTv) {
                            enterPlayPage(position, TVPlayActivity.class);
                        } else {
                            enterPlayPage(position, MoviePlayActivity.class);
                        }
                        finish();
                    }
                });
            } else {
                mMoreRecyclerAdapter.reSetData(mDatas);
            }
        }
    }

    private void enterPlayPage(int position, Class<?> className) {
        MovieItemInfo itemInfo = mDatas.get(position);
        Intent intent = new Intent(SearchActivity.this, className);
        intent.putExtra("moviePlayUrl", itemInfo.getNextUrl());
        intent.putExtra("moviePlayName", itemInfo.getMovieName());
        startActivity(intent);
    }

    @Override
    protected void onLoadDataError() {
    }

    @Override
    protected void onLoadDataFinish() {
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        Toast.makeText(SearchActivity.this, getString(R.string.nothing_find),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setEnabled(false);
        initData();
    }

    @Override
    protected void onRefreshStart() {
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
        if (!TextUtils.isEmpty(mSearchName)) {
            OkHttpUtils.getAsyn(urlUtils.MOVIE_SEARCH_URL + mSearchName, new OkHttpUtils.ResultCallback<String>() {

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
                Log.i(TAG, "parseData: ====error===" + e.getMessage());
                sendParseDataMessage(REFRESH_ERROR);
            }
        }
    }

    private MovieItemInfo parseMovieItemInfo(Element item) {
        if (item != null) {
            MovieItemInfo itemInfo = new MovieItemInfo();
            Element a = item.getElementsByTag("a").get(0);
            String imgIndex = a.getElementsByTag("button").get(0).text();
            if (imgIndex.contains("YY")) {
                return null;
            }
            itemInfo.setImageIndex(imgIndex);
            String url = urlUtils.MOVIE_URL + a.attr("href");
            String videoUrl = url.replace("show", "play");
            itemInfo.setNextUrl(videoUrl);
            Element img = a.getElementsByTag("img").get(0);
            String imgUrl = img.attr("src");
            String name = img.attr("alt");
            itemInfo.setImageUrl(imgUrl);
            itemInfo.setMovieName(name);
            return itemInfo;
        } else {
            return null;
        }
    }
}
