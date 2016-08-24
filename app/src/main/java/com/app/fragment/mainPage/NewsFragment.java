package com.app.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.adapter.NewsRecyclerAdapter;
import com.app.bean.News.NewsInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.NewsDetailActivity;
import com.app.teacup.R;
import com.app.util.HttpUtils;
import com.app.util.urlUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源于煎蛋网
 * @author henry-blue
 */
public class NewsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int IMAGE_VIEW_LEN = 4;
    private List<NewsInfo> mNewsDatas;
    private List<ImageView> mImageViewList;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private NewsRecyclerAdapter mNewsRecyclerAdapter;
    private int mPageNum = 1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mNewsDatas = new ArrayList<>();
        mImageViewList = new ArrayList<>();
        for (int i = 0; i < IMAGE_VIEW_LEN; i++) {
            ImageView view = new ImageView(getContext());
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImageViewList.add(view);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        initView(view);
        setupRefreshLayout();
        setupRecycleView();
        return view;
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.base_recycler_view);

    }

    private void setupRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                startRefreshData();
            }

            @Override
            public void onLoadMore() {
                if (mNewsDatas.size() <= 0) {
                    mRecyclerView.loadMoreComplete();
                } else {
                    startLoadData();
                }
            }
        });

        mNewsRecyclerAdapter = new NewsRecyclerAdapter(getContext(), mNewsDatas, mImageViewList);
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

    private void initImageViewList() {
        if (mNewsDatas.size() <= 0) {
            return;
        }
        for (int i = 0; i < IMAGE_VIEW_LEN; i++) {
            String url = mNewsDatas.get(i).getImgUrl();
            url = url.replace("square", "medium");
            Glide.with(getContext()).load(url)
                    .asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(mImageViewList.get(i));
        }
        mNewsRecyclerAdapter.startHeaderAutoScrolled();
        mNewsRecyclerAdapter.setHeaderVisible(View.VISIBLE);
    }

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        mRefreshLayout.setOnRefreshListener(this);
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
        mNewsDatas.clear();
        mNewsRecyclerAdapter.setHeaderVisible(View.INVISIBLE);
        HttpUtils.sendHttpRequest(urlUtils.NEWS_JIANDAN_URL, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseNewsData(response);
                sendParseDataMessage(REFRESH_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }
        });
    }

    private void startLoadData() {
        mPageNum++;
        if (mPageNum > 80) {
            Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String url = urlUtils.NEWS_NEXT_URL + mPageNum;
        HttpUtils.sendHttpRequest(url, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseNewsData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }
        });
    }

    private void parseNewsData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element wrapper = document.getElementById("mainwrapper");
            if (wrapper != null) {
                Element content = wrapper.getElementById("maincontent");
                if (content != null) {
                    Elements columns = content.getElementsByClass("posthit");
                    if (columns != null) {
                        for (Element column : columns) {
                            NewsInfo info = new NewsInfo();
                            Element thumb_s = column.getElementsByClass("thumb_s").get(0);
                            Element a = thumb_s.getElementsByTag("a").get(0);
                            String href = a.attr("href");
                            info.setNextUrl(href);
                            Element img = a.getElementsByTag("img").get(0);
                            String imgUrl = img.attr("data-original");
                            info.setImgUrl("http:" + imgUrl);
                            Element thetitle = column.getElementsByClass("thetitle").get(0);
                            String text = thetitle.getElementsByTag("a").get(0).text();
                            info.setTitle(text);
                            Element indexs = column.getElementsByClass("indexs").get(0);
                            String tip = indexs.text();
                            info.setLabel(tip);
                            mNewsDatas.add(info);
                        }
                    }
                }
            }
        }
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
            initImageViewList();
            mRecyclerView.refreshComplete();
            mRefreshLayout.setRefreshing(false);
            mNewsRecyclerAdapter.reSetData(mNewsDatas);
        }
    }

    @Override
    protected void onRefreshStart() {
        startRefreshData();
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }

    public void sendParseDataMessage(int message) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = message;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mNewsRecyclerAdapter.startHeaderAutoScrolled();
    }

    @Override
    public void onPause() {
        super.onPause();
        mNewsRecyclerAdapter.stopHeaderAutoScrolled();
    }

}
