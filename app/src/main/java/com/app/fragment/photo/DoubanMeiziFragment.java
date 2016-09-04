package com.app.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
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
import com.app.fragment.BaseFragment;
import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.app.teacup.ShowPhotoListActivity;
import com.app.util.HttpUtils;
import com.app.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class DoubanMeiziFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private int mPageNum = 1;
    private List<PhotoInfo> mImgUrl;
    private ArrayList<String> mImageUrls;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private PhotoDoubanRecyclerAdapter mPhotoRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
        mImageUrls = new ArrayList<>();
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
        mImageUrls.clear();
        HttpUtils.sendHttpRequest(urlUtils.DOUBAN_MEINV_URL, new HttpUtils.HttpCallBackListener() {
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
        String url = urlUtils.DOUBAN_MEINV_NEXT_URL + mPageNum;
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
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = message;
            mHandler.sendMessage(msg);
        }
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
                        mImageUrls.add(url);
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

    @Override
    protected void onLoadDataError() {
        mRecyclerView.loadMoreComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.loadMoreComplete();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshError() {
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
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
