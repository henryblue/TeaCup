package com.app.fragment.mainPage;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.fragment.BaseFragment;
import com.app.teacup.R;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 数据来源片刻网
 * @author henry-blue
 */
public class ReadFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reading_fragment, container, false);
        initView(view);
        setupRefreshLayout();
        setupRecycleView();
        return view;
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.base_recycler_view);
    }

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        mRefreshLayout.setOnRefreshListener(this);
        // TODO
        mRefreshLayout.setEnabled(false);
        //StartRefreshPage();
    }

    private void setupRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreEnabled(false);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                startRefreshData();
            }

            @Override
            public void onLoadMore() {
            }
        });
    }

    private void startLoadData() {
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

    }

    private void parseNewsData(String response) {
        Document document = Jsoup.parse(response);

    }

    @Override
    protected void onLoadDataError() {
    }

    @Override
    protected void onLoadDataFinish() {
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onRefreshStart() {
        startRefreshData();
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}

