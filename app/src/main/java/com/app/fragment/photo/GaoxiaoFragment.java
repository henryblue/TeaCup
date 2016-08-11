package com.app.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.adapter.PhotoGaoxiaoRecyclerAdapter;
import com.app.bean.PhotoInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.app.util.OkHttpUtils;
import com.app.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GaoxiaoFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private int mPageNum = 1;
    private List<PhotoInfo> mImgUrl;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private PhotoGaoxiaoRecyclerAdapter mPhotoRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_fragment, container, false);
        initView(view);
        setupRecycleView();
        setupRefreshLayout();
        return view;
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

    private void setupRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                mPageNum = 1;
                startRefreshData();
            }

            @Override
            public void onLoadMore() {
                if (mImgUrl.size() <= 0) {
                 mRecyclerView.loadMoreComplete();
                } else {
                    startLoadData();
                }
            }
        });

        mPhotoRecyclerAdapter = new PhotoGaoxiaoRecyclerAdapter(getContext(),
                mImgUrl);
        mRecyclerView.setAdapter(mPhotoRecyclerAdapter);
        mPhotoRecyclerAdapter.setOnItemClickListener(new PhotoGaoxiaoRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(getContext(), ShowPhotoActivity.class);
                    intent.putExtra("ImageUrl", url);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.base_recycler_view);
    }

    /**
     * 下拉刷新
     */
    private void startRefreshData() {
        mImgUrl.clear();
        OkHttpUtils.getAsyn(urlUtils.GAOXIAO_URL, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parsePhotoData(response);
                sendParseDataMessage(REFRESH_FINISH);
            }
        });
    }

    /**
     * 下拉加载
     */
    private void startLoadData() {
        if (mPageNum > 35) {
            Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                    Toast.LENGTH_SHORT).show();
            mRecyclerView.loadMoreComplete();
            return;
        }
        mPageNum++;

        String url = urlUtils.GAOXIAO_URL_NEXT + mPageNum + urlUtils.GAOXIAO_URL_NEXT_ID;
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parsePhotoData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }
        });
    }

    public void sendParseDataMessage(int message) {
        Message msg = Message.obtain();
        msg.what = message;
        mHandler.sendMessage(msg);
    }

    private void parsePhotoData(String response) {
        Document document = Jsoup.parse(response);
        Element content = document.getElementById("content-left");
        Elements articles = content.getElementsByClass("article");
        for (Element art : articles) {
            PhotoInfo info = new PhotoInfo();

            Elements authors = art.getElementsByClass("author");
            for (Element author : authors) {
                Elements as = author.getElementsByTag("a");
                for (Element tit : as) {
                    String title = tit.attr("title");
                    if (!TextUtils.isEmpty(title)) {
                        info.setTitle(title);
                    }
                }
            }

            Elements title = art.getElementsByClass("content");
            info.setContent(title.text());
            Elements thumbs = art.getElementsByClass("thumb");
            for (Element thumb : thumbs) {
                Elements img = thumb.getElementsByTag("img");
                for (Element e : img) {
                    String url = e.attr("src");
                    if (url.contains(".jpg") || url.contains(".gif")) {
                        info.setImgUrl(url);
                    }
                }
            }
            mImgUrl.add(info);
        }
    }

    @Override
    public void onRefresh() {
        Message msg = Message.obtain();
        msg.what = REFRESH_START;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(getContext(), getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
        mRecyclerView.loadMoreComplete();
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.loadMoreComplete();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshStart() {
        startRefreshData();
    }
}
