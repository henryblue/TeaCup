package com.app.teacup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.adapter.ReadCollectionRecyclerAdapter;
import com.app.bean.Read.ReadCollectInfo;
import com.app.util.HttpUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class ReadCollectionActivity extends AppCompatActivity {

    private static final int LOAD_DATA_FINISH = 0;
    private static final int LOAD_DATA_ERROR = 1;

    private List<ReadCollectInfo> mDatas;
    private XRecyclerView mRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private ReadCollectionRecyclerAdapter mAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    mRecyclerView.refreshComplete();
                    mRefreshLayout.setRefreshing(false);
                    initData();
                    break;
                case LOAD_DATA_ERROR:
                    Toast.makeText(ReadCollectionActivity.this, getString(R.string.not_have_more_data),
                            Toast.LENGTH_SHORT).show();
                    mRecyclerView.refreshComplete();
                    mRefreshLayout.setRefreshing(false);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_read_topic);
        initView();
        initToolBar();
        setupRefreshLayout();
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
                    sendLoadStateMessage(LOAD_DATA_FINISH);
                }

                @Override
                public void onError(Exception e) {
                    sendLoadStateMessage(LOAD_DATA_ERROR);
                }
            });
        }

    }

    private void sendLoadStateMessage(int what) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = what;
            mHandler.sendMessage(msg);
        }
    }

    private void parseData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element mainLeft = document.getElementsByClass("main_left").get(0);
            Element personalCont = mainLeft.getElementsByClass("personal_cont").get(0);
            Elements createContent = personalCont.getElementsByClass("create_content");
            for (Element e : createContent) {
                ReadCollectInfo info = new ReadCollectInfo();
                Element movingCont = e.getElementsByClass("moving_cont").get(0);
                Element heading = movingCont.getElementsByClass("heading").get(0);
                Element a = heading.getElementsByTag("a").get(0);
                String nextUrl = a.attr("href");
                String title = a.text();
                info.setNextUrl(nextUrl);
                info.setTitle(title);
                Element wordArticle = movingCont.getElementsByClass("word_article").get(0);
                Element clearfix = wordArticle.getElementsByClass("clearfix").get(0);
                Elements a1 = clearfix.getElementsByTag("a");
                for (Element e1 : a1) {
                    String text = e1.text();
                    if (TextUtils.isEmpty(text)) {
                        Element img = e1.getElementsByTag("img").get(0);
                        if (img != null) {
                            String[] srcs = img.attr("src").split("!");
                            info.setImgUrl(srcs[0]);
                        }
                    } else {
                        info.setText(text);
                    }
                }

                Element moreOperate = e.getElementsByClass("more_operate").get(0);
                String come = moreOperate.getElementsByClass("moving_come").get(0).text();
                String detail = "";
                Elements icon = moreOperate.getElementsByClass("icon").get(0).getElementsByTag("a");
                for (Element ic : icon) {
                    detail += ic.text() + " ";
                }

                info.setCome(come);
                info.setDetail(detail);
                mDatas.add(info);
            }
        }
    }

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(ReadCollectionActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mAdapter == null) {
                mAdapter = new ReadCollectionRecyclerAdapter(ReadCollectionActivity.this, mDatas, 0);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new ReadCollectionRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(ReadCollectionActivity.this, ReadDetailActivity.class);
                        intent.putExtra("readTitle", mDatas.get(position).getTitle());
                        intent.putExtra("readDetailUrl", mDatas.get(position).getNextUrl());
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
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setLoadingMoreEnabled(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeMessages(LOAD_DATA_ERROR);
            mHandler.removeMessages(LOAD_DATA_FINISH);
            mHandler = null;
        }
    }
}
