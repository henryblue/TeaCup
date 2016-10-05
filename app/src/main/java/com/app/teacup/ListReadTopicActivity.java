package com.app.teacup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.adapter.ReadTopicRecyclerAdapter;
import com.app.bean.Read.ReadTopicInfo;
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


public class ListReadTopicActivity extends BaseActivity {

    private List<ReadTopicInfo> mDatas;
    private XRecyclerView mRecyclerView;
    private ReadTopicRecyclerAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_read_topic);
        initView();
        initToolBar();
        setupRefreshLayout();
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(ListReadTopicActivity.this, getString(R.string.not_have_more_data),
                Toast.LENGTH_SHORT).show();
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
        initData();
    }

    @Override
    protected void onRefreshError() {

    }

    @Override
    protected void onRefreshFinish() {

    }

    @Override
    protected void onRefreshStart() {

    }

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
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
        mDatas.clear();
        String readUrl = getIntent().getStringExtra("readTopicUrl");
        if (!TextUtils.isEmpty(readUrl)) {
            HttpUtils.sendHttpRequest(readUrl, new HttpUtils.HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    parseData(response);
                    sendParseDataMessage(LOAD_DATA_FINISH);
                }

                @Override
                public void onError(Exception e) {
                    sendParseDataMessage(LOAD_DATA_ERROR);
                }
            });
        }

    }

    private void parseData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element main = document.getElementsByClass("main").get(0);
            Element specialCont = main.getElementsByClass("special_cont").get(0);
            Element container = specialCont.getElementsByClass("collect_container").get(0);
            Element topicList = container.getElementsByClass("topic_list").get(0);
            Element clearfix = topicList.getElementsByClass("clearfix").get(0);
            Elements lis = clearfix.getElementsByTag("li");
            for (Element li : lis) {
                ReadTopicInfo info = new ReadTopicInfo();
                Element tListCont = li.getElementsByClass("t_list_cont").get(0);
                Element lImg = tListCont.getElementsByClass("l_img").get(0);
                Element a = lImg.getElementsByTag("a").get(0);
                String nextUrl = urlUtils.READ_URL_NEXT_HEAD + a.attr("href");
                info.setNextUrl(nextUrl);
                Element img = a.getElementsByTag("img").get(0);
                info.setImgUrl(img.attr("src"));
                Element lCont = tListCont.getElementsByClass("l_cont").get(0);
                String head = lCont.getElementsByTag("h1").get(0).text();
                String detail = lCont.getElementsByTag("p").get(0).text();
                info.setTitle(head);
                info.setDetail(detail);
                mDatas.add(info);
            }
        }
    }

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(ListReadTopicActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mAdapter == null) {
                mAdapter = new ReadTopicRecyclerAdapter(ListReadTopicActivity.this,
                        mDatas, 1);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new ReadTopicRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(ListReadTopicActivity.this, ReadTopicActivity.class);
                        intent.putExtra("readTitle", mDatas.get(position).getTitle());
                        intent.putExtra("readTopicUrl", mDatas.get(position).getNextUrl());
                        startActivity(intent);
                    }
                });
            } else {
                mAdapter.reSetData(mDatas);
                mAdapter.notifyDataSetChanged();
            }

        }
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mRecyclerView = (XRecyclerView) findViewById(R.id.base_recycler_view);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreEnabled(false);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
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

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getIntent().getStringExtra("readTitle"));
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
