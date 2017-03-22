package com.app.teacup.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.teacup.MoreMovieShowActivity;
import com.app.teacup.MovieTestPlayActivity;
import com.app.teacup.R;
import com.app.teacup.adapter.MovieDetailRecyclerAdapter;
import com.app.teacup.adapter.ReactViewPagerAdapter;
import com.app.teacup.bean.movie.MovieDetailInfo;
import com.app.teacup.bean.movie.MovieItemInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.OkHttpUtils;
import com.app.teacup.util.ThreadPoolUtils;
import com.app.teacup.util.urlUtils;
import com.squareup.okhttp.Request;

import org.apache.http.util.EncodingUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.app.teacup.util.urlUtils.MOVIE_URL;

/**
 * 数据来源15影城
 * @author henryblue
 */
public class MovieFragment extends BaseFragment {

    private static final int HEADER_LOAD_NUM = 5;

    private List<MovieDetailInfo> mDatas;
    private List<MovieItemInfo> mHeadersData;
    private MovieDetailRecyclerAdapter mMovieDetailAdapter;
    private boolean mIsFirstEnter = true;
    private List<View> mHeaderList;
    private ReactViewPagerAdapter mHeaderAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDatas = new ArrayList<>();
        mHeadersData = new ArrayList<>();
//        mRequestUrl = urlUtils.MOVIE_URL;
    }

    @Override
    protected void startRefreshData() {
        mDatas.clear();
        mHeadersData.clear();
        mMovieDetailAdapter.getHeaderView().setVisibility(View.INVISIBLE);
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
            startRefreshDataWithHeader();
        }
        mIsFirstEnter = false;
    }

    private void startRefreshDataWithHeader() {
        OkHttpUtils.getAsynWithHeader(getContext(), MOVIE_URL, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parseData(response);
                sendParseDataMessage(REFRESH_FINISH);
            }
        });
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mRecyclerView.setLoadingMoreEnabled(false);
        mMovieDetailAdapter = new MovieDetailRecyclerAdapter(getContext(), mDatas);
        mMovieDetailAdapter.setHeaderView(setupRecycleViewHeader());
        mRecyclerView.setAdapter(mMovieDetailAdapter);

        mMovieDetailAdapter.setOnItemClickListener(new MovieDetailRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, int itemPosition) {
                    enterPlayPage(position, itemPosition, MovieTestPlayActivity.class);
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

    private View setupRecycleViewHeader() {
        View headView = View.inflate(getContext(), R.layout.item_movie_header, null);
        ViewPager viewPager = (ViewPager) headView.findViewById(R.id.vp_movie);
        mHeaderList = new ArrayList<>();
        for (int i = 0; i < HEADER_LOAD_NUM; i++) {
            View itemView = View.inflate(getContext(), R.layout.item_movie_header_view, null);
            mHeaderList.add(itemView);
        }
        mHeaderAdapter = new ReactViewPagerAdapter(viewPager, mHeaderList);
        viewPager.setAdapter(mHeaderAdapter);
        headView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mHeaderAdapter.startAutoScrolled();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                mHeaderAdapter.stopAutoScrolled();
            }
        });
        return headView;
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
                // parse header content
                Element focusBanner = document.getElementsByClass("focusBanner").first();
                Element focusList = focusBanner.getElementsByClass("focusList").first();
                Elements headers = focusList.getElementsByTag("li");
                int i = -1;
                for (Element header : headers) {
                    i++;
                    if (i >= HEADER_LOAD_NUM) {
                        break;
                    }
                    MovieItemInfo itemInfo = new MovieItemInfo();
                    Element a = header.getElementsByTag("a").first();
                    Element img = a.getElementsByTag("img").first();
                    itemInfo.setMovieName(img.attr("alt"));
                    String imgUrl = img.attr("data-src");
                    itemInfo.setImageUrl(imgUrl);
                    String href = a.attr("href");
                    String tmpUrl = urlUtils.MOVIE_URL + href;
                    String nextUrl = tmpUrl.replace("show", "play");
                    itemInfo.setNextUrl(nextUrl);
                    mHeadersData.add(itemInfo);
                }

                // parse video content
                Elements videos = document.getElementsByClass("main").first()
                        .getElementsByClass("clearfix");
                i = -1;
                for (Element index : videos) {
                    i++;
                    if (i == 3 || i == 5 || i == 6) {
                        continue;
                    }
                    MovieDetailInfo info = new MovieDetailInfo();
                    Element section = index.getElementsByTag("section").first();
                    String videoMark = section.getElementsByClass("sMark").first().text();
                    String subMark = videoMark.substring(0, 4);
                    info.setMovieBlockName(subMark);
                    String moreUrl = urlUtils.MOVIE_URL +
                            section.getElementsByClass("aMore").first().attr("href");
                    info.setMoreUrl(moreUrl);

                    //parse all movie info
                    List<MovieItemInfo> movieInfoList = new ArrayList<>();
                    Elements lis = index.getElementsByTag("li");
                    for (Element li : lis) {
                        MovieItemInfo itemInfo = parseMovieItemInfo(li);
                        if (itemInfo != null) {
                            movieInfoList.add(itemInfo);
                        }
                    }
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
            Element a = item.getElementsByTag("a").first();
            String url = MOVIE_URL + a.attr("href");
            String videoUrl = url.replace("show", "play");
            itemInfo.setNextUrl(videoUrl);

            Element img = a.getElementsByTag("img").first();
            String imgUrl = img.attr("src");
            String name = img.attr("alt");
            itemInfo.setImageUrl(imgUrl);
            itemInfo.setMovieName(name);
            String imgIndex = a.getElementsByClass("other").first().text();
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
            return;
        }

        if (!mHeadersData.isEmpty()) {
            initHeaderData();
        }
    }

    private void initHeaderData() {
        mMovieDetailAdapter.getHeaderView().setVisibility(View.VISIBLE);
        for (int i = 0; i < HEADER_LOAD_NUM; i++) {
            View view = mHeaderList.get(i);
            final MovieItemInfo itemInfo = mHeadersData.get(i);
            ImageView imgView = (ImageView) view.findViewById(R.id.movie_header_img);
            loadImageResource(itemInfo.getImageUrl(), imgView);
            TextView textView = (TextView) view.findViewById(R.id.movie_header_text);
            textView.setText(itemInfo.getMovieName());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MovieTestPlayActivity.class);
                    intent.putExtra("moviePlayUrl", itemInfo.getNextUrl());
                    intent.putExtra("moviePlayName", itemInfo.getMovieName());
                    intent.putExtra("movieStyle", getString(R.string.video_from));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsInitData) {
            if (mHeaderAdapter != null) {
                mHeaderAdapter.startAutoScrolled();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mIsInitData) {
            if (mHeaderAdapter != null) {
                mHeaderAdapter.stopAutoScrolled();
            }
        }
    }

    @Override
    public void onLoadDataNone() {
        startRefreshDataWithHeader();
    }

    @Override
    protected void onRefreshFinish() {
        super.onRefreshFinish();
        loadData();
    }

    @Override
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
        if (mHeaderAdapter != null) {
            mHeaderAdapter.stopAutoScrolled();
        }
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        if (mHeaderAdapter != null) {
            mHeaderAdapter.startAutoScrolled();
        }
    }
}

