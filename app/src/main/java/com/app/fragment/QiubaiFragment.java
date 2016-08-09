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

import com.app.adapter.PhotoQiubaiRecyclerAdapter;
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

public class QiubaiFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REFRESH_START = 0;
    private static final int REFRESH_FINISH = 1;
    private static final int REFRESH_ERROR = 2;
    private static final int LOAD_DATA_FINISH = 3;
    private static final int LOAD_DATA_ERROR = 4;

    private List<PhotoInfo> mImgUrl;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private PhotoQiubaiRecyclerAdapter mPhotoRecyclerAdapter;
    private int mPageNum = 1;

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
        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                startLoadData();
            }
        });

        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);

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
        if (mPageNum > 9) {
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
        Message msg = Message.obtain();
        msg.what = REFRESH_START;
        mHandler.sendMessage(msg);
    }
}
