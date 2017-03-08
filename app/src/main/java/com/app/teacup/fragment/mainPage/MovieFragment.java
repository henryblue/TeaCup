package com.app.teacup.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.teacup.MoreMovieShowActivity;
import com.app.teacup.MoviePlayActivity;
import com.app.teacup.R;
import com.app.teacup.adapter.MovieDetailRecyclerAdapter;
import com.app.teacup.bean.movie.MovieDetailInfo;
import com.app.teacup.bean.movie.MovieItemInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.ThreadPoolUtils;
import com.app.teacup.util.urlUtils;

import org.apache.http.util.EncodingUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源15影城
 * @author henryblue
 */
public class MovieFragment extends BaseFragment {

    private List<MovieDetailInfo> mDatas;
    private MovieDetailRecyclerAdapter mMovieDetailAdapter;
    private boolean mIsFirstEnter = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDatas = new ArrayList<>();
        mRequestUrl = urlUtils.MOVIE_URL;
    }

    @Override
    protected void startRefreshData() {
        mDatas.clear();
        if (mIsFirstEnter) {
            ThreadPoolUtils.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileInputStream fin = getContext().openFileInput(getContext()
                                .getString(R.string.movies_cache_name));
                        int length = fin.available();
                        byte[] buffer = new byte[length];
                        fin.read(buffer);
                        String result = EncodingUtils.getString(buffer, "UTF-8");
                        if (!TextUtils.isEmpty(result)) {
                            parseData(result);
                            sendParseDataMessage(REFRESH_FINISH);
                        } else {
                            sendParseDataMessage(LOAD_DATA_NONE);
                        }
                    } catch (Exception e) {
                        sendParseDataMessage(LOAD_DATA_NONE);
                    }
                }
            });
        } else {
            super.startRefreshData();
        }
        mIsFirstEnter = false;
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mRecyclerView.setLoadingMoreEnabled(false);
        mMovieDetailAdapter = new MovieDetailRecyclerAdapter(getContext(), mDatas);
        mRecyclerView.setAdapter(mMovieDetailAdapter);

        mMovieDetailAdapter.setOnItemClickListener(new MovieDetailRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int itemPosition) {
                    enterPlayPage(position, itemPosition, MoviePlayActivity.class);
            }

            @Override
            public void onMoreItemClick(View view, int position) {
                String moreUrl = mDatas.get(position).getMoreUrl();
                Intent intent = new Intent(getContext(), MoreMovieShowActivity.class);
                intent.putExtra("moreMovieUrl", moreUrl);
                intent.putExtra("moreMovieStyle", position);
                startActivity(intent);
            }

        });
    }

    private void enterPlayPage(int position, int itemPosition, Class<?> className) {
        MovieItemInfo itemInfo = mDatas.get(position).getMovieInfoList().get(itemPosition);
        Intent intent = new Intent(getContext(), className);
        intent.putExtra("moviePlayUrl", itemInfo.getNextUrl());
        intent.putExtra("moviePlayName", itemInfo.getMovieName());
        String style = getContext().getString(R.string.tv_series);
        if (position == 0 || position == 4) {
            style = getContext().getString(R.string.video_from);
        }
        intent.putExtra("movieStyle", style);
        startActivity(intent);
    }

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        try {
            if (document != null) {
                Element container = document.getElementsByClass("container").get(3);
                Elements rows = container.getElementsByClass("row");
                int i = 0;
                int j = 0;
                for (Element row : rows) {
                    if (i == 4 || i == 3) { // remove dislike label
                        i++;
                        continue;
                    }
                    i++;
                    MovieDetailInfo info = new MovieDetailInfo();
                    List<MovieItemInfo> movieInfoList = new ArrayList<>();
                    Elements movieItem = row.getElementsByClass("movie-item-out");
                    for (Element item : movieItem) {
                        if (j == 0) { // get label name
                            String labelName = item.getElementsByTag("h3").get(0).text();
                            Elements span = item.getElementsByTag("span");
                            if (span.size() > 0) {
                                String moreUrl = urlUtils.MOVIE_URL + span.get(0)
                                        .getElementsByTag("a").get(0).attr("href");
                                info.setMoreUrl(moreUrl);
                            }
                            info.setMovieBlockName(labelName);
                        } else if (j < 7 && j > 0) { // only need 6 item
                            MovieItemInfo itemInfo = parseMovieItemInfo(item);
                            if (itemInfo != null) {
                                movieInfoList.add(itemInfo);
                            }
                        } else {
                            break;
                        }
                        j++;
                    }
                    j = 0;
                    info.setMovieInfoList(movieInfoList);
                    mDatas.add(info);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "MovieFragment::parseMovieData: error===" + e.getMessage());
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

    private void loadData() {
        if (!mDatas.isEmpty()) {
            mMovieDetailAdapter.resetData(mDatas);
        } else {
            Toast.makeText(getContext(), getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoadDataNone() {
        startRefreshData();
    }

    @Override
    protected void onRefreshFinish() {
        super.onRefreshFinish();
        loadData();
    }
}

