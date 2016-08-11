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

import com.app.adapter.PhotoQiubaiRecyclerAdapter;
import com.app.bean.PhotoInfo;
import com.app.fragment.BaseFragment;
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

public class QiubaiFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private List<PhotoInfo> mImgUrl;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private PhotoQiubaiRecyclerAdapter mPhotoRecyclerAdapter;
    private int mPageNum = 1;

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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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

        mPhotoRecyclerAdapter = new PhotoQiubaiRecyclerAdapter(getContext(),
                mImgUrl);
        mRecyclerView.setAdapter(mPhotoRecyclerAdapter);

        mPhotoRecyclerAdapter.setOnItemClickListener(new PhotoQiubaiRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url) && url.endsWith(".gif")) {
                    mPhotoRecyclerAdapter.startLoadImage(view, url);
                } else if (url.endsWith(".jpg")) {
                    Intent intent = new Intent(getContext(), ShowPhotoActivity.class);
                    intent.putExtra("ImageUrl", url);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url) && url.endsWith(".gif")) {
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

    /**
     * 上拉刷新数据
     */
    private void startRefreshData() {
        mImgUrl.clear();
        String url = "http://www.qiushibaike18.com/";
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

    /**
     * 下拉加载数据
     */
    private void startLoadData() {
        mPageNum++;
        if (mPageNum > 20) {
            mRecyclerView.loadMoreComplete();
            Toast.makeText(getContext(), "没有更多的数据了...", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "http://www.qiushibaike18.com/page/" + mPageNum + "/";
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
        Elements mainWrap = document.getElementsByClass("home_main_wrap");
        for (Element m : mainWrap) {
            Elements panelClearfix = m.getElementsByClass("panel");
            for (Element e : panelClearfix) {
                PhotoInfo info = new PhotoInfo();
                Elements tops = e.getElementsByClass("top");
                for (Element top : tops) {
                    Elements h2 = top.getElementsByTag("h2");
                    for (Element h : h2) {
                        Elements title = h.getElementsByTag("a");
                        info.setTitle(title.text());
                    }
                }

                Elements clearfix = e.getElementsByClass("main");
                for (Element main : clearfix) {
                    Elements imagebox = main.getElementsByClass("imagebox");
                    for (Element photo : imagebox) {
                        Elements a = photo.getElementsByClass("gif");
                        for (Element gif : a) {
                            String src = gif.attr("href");
                            if (src.contains(".gif")) {
                                info.setImgUrl(src);
                            }
                        }
                        Elements img = photo.getElementsByTag("img");
                        for (Element pic : img) {
                            String src = pic.attr("src");
                            if (src.contains(".jpg")) {
                                info.setImgUrl(src);
                            }
                        }
                    }
                }

                mImgUrl.add(info);
            }
        }
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(getContext(), "刷新失败, 请检查网络", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), "刷新失败, 请检查网络", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshStart() {
        startRefreshData();
    }
}
