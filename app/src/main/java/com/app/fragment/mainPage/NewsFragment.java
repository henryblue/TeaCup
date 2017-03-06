package com.app.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.adapter.NewsRecyclerAdapter;
import com.app.bean.News.NewsInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.MainActivity;
import com.app.teacup.NewsDetailActivity;
import com.app.teacup.R;
import com.app.util.OkHttpUtils;
import com.app.util.urlUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.okhttp.Request;

import org.apache.http.util.EncodingUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源于煎蛋网
 *
 * @author henry-blue
 */
public class NewsFragment extends BaseFragment {

    private static final int IMAGE_VIEW_LEN = 4;
    private List<NewsInfo> mNewsDatas;
    private List<ImageView> mHeaderList;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private boolean mIsFirstEnter = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mNewsDatas = new ArrayList<>();
        mHeaderList = new ArrayList<>();
        for (int i = 0; i < IMAGE_VIEW_LEN; i++) {
            ImageView view = new ImageView(getContext());
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mHeaderList.add(view);
        }
    }

    private void initHeaderData() {
        if (mNewsDatas.size() <= 0) {
            return;
        }
        for (int i = 0; i < IMAGE_VIEW_LEN; i++) {
            String url = mNewsDatas.get(i).getImgUrl();
            url = url.replace("square", "medium");
            if (!MainActivity.mIsLoadPhoto) {
                Glide.with(getContext()).load(url)
                        .asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(mHeaderList.get(i));
            } else {
                if (MainActivity.mIsWIFIState) {
                    Glide.with(getContext()).load(url)
                            .asBitmap()
                            .error(R.drawable.photo_loaderror)
                            .placeholder(R.drawable.main_load_bg)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(mHeaderList.get(i));
                } else {
                    mHeaderList.get(i).setImageResource(R.drawable.main_load_bg);
                }
            }

            final int pos = i;
            mHeaderList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NewsDetailActivity.class);
                    intent.putExtra("newsDetailUrl", mNewsDatas.get(pos).getNextUrl());
                    intent.putExtra("newsTitle", mNewsDatas.get(pos).getTitle());
                    startActivity(intent);
                }
            });
        }
        mNewsRecyclerAdapter.setHeaderVisible(View.VISIBLE);
    }

    @Override
    protected void startRefreshData() {
        mNewsDatas.clear();
        mNewsRecyclerAdapter.setHeaderVisible(View.INVISIBLE);
        if (mIsFirstEnter) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileInputStream fin = getContext().openFileInput(getContext()
                                .getString(R.string.news_cache_name));
                        int length = fin.available();
                        byte[] buffer = new byte[length];
                        fin.read(buffer);
                        String result = EncodingUtils.getString(buffer, "UTF-8");
                        if (!TextUtils.isEmpty(result)) {
                            parseNewsData(result);
                        } else {
                            sendParseDataMessage(LOAD_DATA_NONE);
                        }
                    } catch (Exception e) {
                        sendParseDataMessage(LOAD_DATA_NONE);
                    }
                }
            }, 850);
        } else {
            loadDataFromNet();
        }
        mIsFirstEnter = false;
    }

    @Override
    protected void onResponseLoadMore() {
        if (mNewsDatas.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData();
        }
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mNewsRecyclerAdapter = new NewsRecyclerAdapter(getContext(), mNewsDatas, mHeaderList);
        mRecyclerView.setAdapter(mNewsRecyclerAdapter);
        mNewsRecyclerAdapter.setOnItemClickListener(new NewsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), NewsDetailActivity.class);
                intent.putExtra("newsDetailUrl", mNewsDatas.get(position).getNextUrl());
                intent.putExtra("newsTitle", mNewsDatas.get(position).getTitle());
                startActivity(intent);
            }
        });
    }

    private void loadDataFromNet() {
        OkHttpUtils.getAsyn(urlUtils.NEWS_JIANDAN_URL, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parseNewsData(response);
            }
        });
    }

    private void startLoadData() {
        mPageNum++;
        if (mPageNum > 50) {
            Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String url = urlUtils.NEWS_NEXT_URL + mPageNum;
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parseNextData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }
        });
    }

    private void parseNewsData(String response) {
        Document document = Jsoup.parse(response);
        try {
            if (document != null) {
                Element body = document.getElementById("body");
                Element content = body.getElementById("content");
                Elements columns = content.getElementsByClass("list-post");
                for (Element column : columns) {
                    Element indexs = column.getElementsByClass("indexs").get(0);
                    String text = indexs.getElementsByTag("h2").get(0).text();
                    if (text.contains(getString(R.string.news_delete1))
                            || text.contains(getString(R.string.news_delete2))) {
                        continue;
                    }
                    NewsInfo info = new NewsInfo();
                    info.setTitle(text);
                    Element thumb_s = column.getElementsByClass("thumbs_b").get(0);
                    Element a = thumb_s.getElementsByTag("a").get(0);
                    String href = a.attr("href");
                    info.setNextUrl(href);
                    Element img = a.getElementsByTag("img").get(0);
                    String imgUrl = img.attr("data-original");
                    if (TextUtils.isEmpty(imgUrl)) {
                        imgUrl = img.attr("src");
                    }
                    info.setImgUrl("http:" + imgUrl);

                    String tip = indexs.getElementsByClass("time_s").get(0).text();
                    info.setLabel(tip);
                    mNewsDatas.add(info);
                }
                sendParseDataMessage(REFRESH_FINISH);
            }

        } catch (Exception e) {
            sendParseDataMessage(LOAD_DATA_ERROR);
        }
    }

    private void parseNextData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element body = document.getElementById("body");
            if (body != null) {
                Element content = body.getElementById("content");
                if (content != null) {
                    Elements divs = content.getElementsByClass("column");
                    if (divs != null) {
                        for (Element div : divs) {
                            Element post = div.getElementsByClass("post").get(0);
                            String text = post.getElementsByClass("title2").get(0).text();
                            if (text.contains(getString(R.string.news_delete1))
                                    || text.contains(getString(R.string.news_delete2))) {
                                continue;
                            }
                            NewsInfo info = new NewsInfo();
                            info.setTitle(text);
                            Element thumbs_b = post.getElementsByClass("thumbs_b").get(0);
                            Element a = thumbs_b.getElementsByTag("a").get(0);
                            String href = a.attr("href");
                            info.setNextUrl(href);
                            Element img = a.getElementsByTag("img").get(0);
                            String imgUrl = img.attr("data-original");
                            if (TextUtils.isEmpty(imgUrl)) {
                                imgUrl = img.attr("src");
                            }
                            info.setImgUrl("http:" + imgUrl);

                            String tip = post.getElementsByClass("time_s").get(0).text();
                            info.setLabel(tip);
                            mNewsDatas.add(info);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onLoadDataNone() {
        super.onLoadDataNone();
        loadDataFromNet();
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
        mRecyclerView.loadMoreComplete();
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.loadMoreComplete();
        mNewsRecyclerAdapter.reSetData(mNewsDatas);
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        if (mNewsDatas.size() <= 0) {
            Toast.makeText(getContext(), getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            initHeaderData();
            mNewsRecyclerAdapter.reSetData(mNewsDatas);
        }
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
        if (mNewsRecyclerAdapter != null) {
            mNewsRecyclerAdapter.stopHeaderAutoScrolled();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirstEnter) {
            mIsInitData = true;
            super.onFragmentVisible();
        }
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        if (mNewsRecyclerAdapter != null) {
            mNewsRecyclerAdapter.startHeaderAutoScrolled();
        }
    }
}
