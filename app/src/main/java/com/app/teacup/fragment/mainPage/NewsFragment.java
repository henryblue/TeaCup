package com.app.teacup.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.teacup.NewsDetailActivity;
import com.app.teacup.R;
import com.app.teacup.adapter.NewsRecyclerAdapter;
import com.app.teacup.adapter.ReactViewPagerAdapter;
import com.app.teacup.bean.News.NewsInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.ui.ReactViewPager;
import com.app.teacup.ui.ZoomOutPageTransformer;
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
 * 数据来源于煎蛋网
 *
 * @author henryblue
 */
public class NewsFragment extends BaseFragment {

    private static final int IMAGE_VIEW_LEN = 5;
    private List<NewsInfo> mNewsDatas;
    private List<View> mHeaderList;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private boolean mIsFirstEnter = true;
    private ReactViewPagerAdapter mHeaderAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mNewsDatas = new ArrayList<>();
        mHeaderList = new ArrayList<>();
        mRequestUrl = urlUtils.NEWS_JIANDAN_URL;
    }

    @Override
    protected void startRefreshData() {
        mNewsDatas.clear();
        mNewsRecyclerAdapter.getHeaderView().setVisibility(View.INVISIBLE);
        if (mIsFirstEnter) {
            ThreadPoolUtils.getInstance().execute(new Runnable() {
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
        if (mNewsDatas.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData(urlUtils.NEWS_NEXT_URL, 50);
        }
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mNewsRecyclerAdapter = new NewsRecyclerAdapter(getContext(), mNewsDatas);
        mNewsRecyclerAdapter.setHeaderView(setupRecycleViewHeader());
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

    private View setupRecycleViewHeader() {
        View headView = View.inflate(getContext(), R.layout.item_base_header, null);
        headView.setVisibility(View.VISIBLE);
        ReactViewPager viewPager = (ReactViewPager) headView.findViewById(R.id.base_view_pager);
        for (int i = 0; i < IMAGE_VIEW_LEN; i++) {
            View itemView = View.inflate(getContext(), R.layout.item_base_header_view, null);
            mHeaderList.add(itemView);
        }
        mHeaderAdapter = new ReactViewPagerAdapter(viewPager, mHeaderList);
        viewPager.setAdapter(mHeaderAdapter);
        viewPager.enableCenterLockOfChilds();
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2, true);
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

    @Override
    protected void parseData(String response) {
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
            }

        } catch (Exception e) {
            Log.i(TAG, "NewsFragment::parseData: ==error==" + e.getMessage());
        }
    }

    @Override
    protected void parseLoadData(String response) {
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
        startRefreshData();
    }

    @Override
    protected void onLoadDataFinish() {
        super.onLoadDataFinish();
        mNewsRecyclerAdapter.reSetData(mNewsDatas);
    }

    private void initHeaderData() {
        mNewsRecyclerAdapter.getHeaderView().setVisibility(View.VISIBLE);
        for (int i = 0; i < IMAGE_VIEW_LEN; i++) {
            View view = mHeaderList.get(i);
            String url = mNewsDatas.get(i).getImgUrl();
            url = url.replace("square", "medium");
            ImageView imgView = (ImageView) view.findViewById(R.id.movie_header_img);
            loadImageResource(url, imgView);
            TextView textView = (TextView) view.findViewById(R.id.movie_header_text);
            textView.setText(mNewsDatas.get(i).getTitle());
            final int pos = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), NewsDetailActivity.class);
                    intent.putExtra("newsDetailUrl", mNewsDatas.get(pos).getNextUrl());
                    intent.putExtra("newsTitle", mNewsDatas.get(pos).getTitle());
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onRefreshFinish() {
        super.onRefreshFinish();
        if (mNewsDatas.size() <= 0) {
            Toast.makeText(getContext(), getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            initHeaderData();
            mNewsRecyclerAdapter.reSetData(mNewsDatas);
        }
    }

    @Override
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
        if (mHeaderAdapter != null) {
            mHeaderAdapter.stopAutoScrolled();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirstEnter) {
            mIsInitData = true;
            super.onFragmentVisible();
        }
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
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        if (mHeaderAdapter != null) {
            mHeaderAdapter.startAutoScrolled();
        }
    }
}
