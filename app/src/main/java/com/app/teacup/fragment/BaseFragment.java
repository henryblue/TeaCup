package com.app.teacup.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.app.teacup.R;
import com.app.teacup.util.OkHttpUtils;
import com.app.teacup.util.ToolUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.squareup.okhttp.Request;

public abstract class BaseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    protected static final String TAG = "TeacupBaseFragment";
    protected static final int REFRESH_START = 0;
    protected static final int REFRESH_FINISH = 1;
    protected static final int REFRESH_ERROR = 2;
    protected static final int LOAD_DATA_FINISH = 3;
    protected static final int LOAD_DATA_ERROR = 4;
    protected static final int LOAD_DATA_NONE = 5;
    protected boolean mIsInitData = false;
    protected int mPageNum = 1;
    protected String mRequestUrl;

    protected SwipeRefreshLayout mRefreshLayout;
    protected XRecyclerView mRecyclerView;

    @SuppressLint("HandlerLeak")
    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (((Activity)getContext()).isFinishing()){
                return;
            }
            switch (msg.what) {
                case REFRESH_START:
                    onRefreshStart();
                    break;
                case REFRESH_FINISH:
                    onRefreshFinish();
                    break;
                case REFRESH_ERROR:
                    onRefreshError();
                    break;
                case LOAD_DATA_FINISH:
                    onLoadDataFinish();
                    break;
                case LOAD_DATA_ERROR:
                    onLoadDataError();
                    break;
                case LOAD_DATA_NONE:
                    onLoadDataNone();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_fragment, container, false);
        initView(view);
        mIsInitData = true;
        return view;
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.base_recycler_view);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onFragmentVisible();
        } else {
            onFragmentInvisible();
        }
    }

    protected void onFragmentInvisible() {
    }

    protected void onFragmentVisible() {
        if (mIsInitData) {
            mIsInitData = false;
            setupRecycleView();
            setupRefreshLayout();
        }
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
                onRecyclerViewResponseRefresh();
            }

            @Override
            public void onLoadMore() {
                onRecycleViewResponseLoadMore();
            }
        });

        setupRecycleViewAndAdapter();
    }

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeColors(ToolUtils.getThemeColorPrimary(getContext()));
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

    protected void startRefreshData() {
        mPageNum = 1;
        if (TextUtils.isEmpty(mRequestUrl)) {
            throw new RuntimeException(
                    "Can't start request data that has not set RequestUrl");
        }
        OkHttpUtils.getAsyn(mRequestUrl, new OkHttpUtils.ResultCallback<String>() {

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

    protected void startLoadData(String loadUrl, int maxLoadNum) {
        if (maxLoadNum > 0) {
            mPageNum++;
            if (mPageNum > maxLoadNum) {
                mRecyclerView.loadMoreComplete();
                Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            loadUrl = loadUrl + mPageNum;
        }

        OkHttpUtils.getAsyn(loadUrl, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parseLoadData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }
        });
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }

    protected void sendParseDataMessage(int message) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = message;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeMessages(REFRESH_START);
            mHandler.removeMessages(REFRESH_FINISH);
            mHandler.removeMessages(REFRESH_ERROR);
            mHandler.removeMessages(LOAD_DATA_FINISH);
            mHandler.removeMessages(LOAD_DATA_ERROR);
            mHandler = null;
        }
    }

    protected void onRecyclerViewResponseRefresh() {
        mPageNum = 1;
        startRefreshData();
    }

    protected void onLoadDataNone() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.loadMoreComplete();
        mRecyclerView.refreshComplete();
    }

    protected void onRefreshStart() {
        startRefreshData();
    }

    protected void onRefreshFinish() {
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
    }

    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
    }

    protected void onLoadDataFinish() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.loadMoreComplete();
    }

    protected void onLoadDataError() {
        Toast.makeText(getContext(), getString(R.string.load_data_error), Toast.LENGTH_SHORT).show();
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.loadMoreComplete();
    }

    protected void parseLoadData(String response) {
        parseData(response);
    }

    protected abstract void parseData(String response);

    protected abstract void onRecycleViewResponseLoadMore();

    protected abstract void setupRecycleViewAndAdapter();

}
