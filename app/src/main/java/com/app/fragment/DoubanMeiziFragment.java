package com.app.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.adapter.PhotoDoubanRecyclerAdapter;
import com.app.bean.PhotoInfo;
import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.app.util.HttpUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class DoubanMeiziFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REFRESH_START = 0;
    private static final int REFRESH_FINISH = 1;
    private static final int REFRESH_ERROR = 2;
    private static final int LOAD_DATA_FINISH = 3;
    private static final int LOAD_DATA_ERROR = 4;

    private int mPageNum = 1;
    private List<PhotoInfo> mImgUrl;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private PhotoDoubanRecyclerAdapter mPhotoRecyclerAdapter;

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
                    mRecyclerView.refreshComplete();
                    mPhotoRecyclerAdapter.reSetData(mImgUrl);
                    break;
                case REFRESH_ERROR:
                    mRecyclerView.refreshComplete();
                    mRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "刷新失败, 请检查网络", Toast.LENGTH_SHORT).show();
                    break;
                case LOAD_DATA_FINISH:
                    mRecyclerView.loadMoreComplete();
                    mPhotoRecyclerAdapter.reSetData(mImgUrl);
                    break;
                case LOAD_DATA_ERROR:
                    mRecyclerView.loadMoreComplete();
                    Toast.makeText(getContext(), "刷新失败, 请检查网络", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

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
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mPhotoRecyclerAdapter = new PhotoDoubanRecyclerAdapter(getContext(),
                mImgUrl);
        mRecyclerView.setAdapter(mPhotoRecyclerAdapter);
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
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);

        mPhotoRecyclerAdapter.setOnItemClickListener(new PhotoDoubanRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(getContext(), ShowPhotoActivity.class);
                    intent.putExtra("ImageUrl", url);
                    startActivity(intent);
                }
            }
        });
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.base_recycler_view);
    }

    private void startRefreshData() {
        mImgUrl.clear();
        String url = "http://www.dbmeinv.com/dbgroup/show.htm?pager_offset=1";
        HttpUtils.sendHttpRequest(url, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parsePhotoData(response);
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
        String url = "http://www.dbmeinv.com/dbgroup/show.htm?pager_offset=" + mPageNum;
        HttpUtils.sendHttpRequest(url, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parsePhotoData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
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
        Element main = document.getElementById("main");
        Elements liElements = main.getElementsByTag("li");
        for (Element element : liElements) {

            Elements aElements = element.getElementsByTag("a");
            for (Element a : aElements) {
                Elements height_min = a.getElementsByClass("height_min");
                for (Element height : height_min) {
                    PhotoInfo info = new PhotoInfo();
                    String url = height.attr("src");
                    if (url.contains(".jpg")) {
                        info.setImgUrl(url);
                    }
                    String title = height.attr("title");
                    info.setTitle(title);
                    mImgUrl.add(info);
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        Message msg = Message.obtain();
        msg.what = REFRESH_START;
        mHandler.sendMessage(msg);
    }
}
