package com.app.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.app.util.OkHttpUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GaoxiaoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REFRESH_START = 0;
    private static final int REFRESH_FINISH = 1;
    private static final int REFRESH_ERROR = 2;
    private static final int LOAD_DATA_FINISH = 3;
    private static final int LOAD_DATA_ERROR = 4;

    private int mPageNum = 1;
    private List<PhotoInfo> mImgUrl;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_START:
                    startRefreshData();
                    break;
                case REFRESH_FINISH:
                    mRefreshLayout.setRefreshing(false);
                    mPhotoRecyclerAdapter.reSetData(mImgUrl);
                    break;
                case REFRESH_ERROR:
                    mRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "刷新失败, 请检查网络", Toast.LENGTH_SHORT).show();
                    break;
                case LOAD_DATA_FINISH:
                    mRecyclerView.loadMoreComplete();
                    mPhotoRecyclerAdapter.reSetData(mImgUrl);
                    break;
                case LOAD_DATA_ERROR:
                    Toast.makeText(getContext(), "刷新失败, 请检查网络", Toast.LENGTH_SHORT).show();
                    mRecyclerView.loadMoreComplete();
                    break;
                default:
                    break;
            }
        }
    };
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
        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                startLoadData();
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
        String url = "http://www.qiushibaike.com/pic/";
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

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
        if (mPageNum > 9) {
            Toast.makeText(getContext(), "没有更多数据了...", Toast.LENGTH_SHORT).show();
            mRecyclerView.loadMoreComplete();
            return;
        }
        mPageNum++;

        String url = "http://www.qiushibaike.com/pic/page/" + mPageNum + "/?s=4902398";
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
}
