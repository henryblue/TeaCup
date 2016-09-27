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
import com.app.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class ListReadCollectionActivity extends AppCompatActivity {

    private static final int LOAD_DATA_FINISH = 0;
    private static final int LOAD_DATA_ERROR = 1;

    private List<ReadCollectInfo> mDatas;
    private XRecyclerView mRecyclerView;
    private ReadCollectionRecyclerAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

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
                    Toast.makeText(ListReadCollectionActivity.this, getString(R.string.not_have_more_data),
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
            Element main = document.getElementsByClass("main").get(0);
            Element nMainLeft = main.getElementsByClass("n_main_left").get(0);
            Element listBlock = nMainLeft.getElementsByClass("list_block").get(0);
            Element albumCont = listBlock.getElementsByClass("album_cont").get(0);
            Elements albumBox = albumCont.getElementsByClass("album_box");
            for (Element e : albumBox) {
                ReadCollectInfo info = new ReadCollectInfo();
                Element a = e.getElementsByClass("a_left").get(0).getElementsByTag("a").get(0);
                String nextUrl = urlUtils.READ_URL_NEXT_HEAD + a.attr("href");
                info.setNextUrl(nextUrl);
                Element img = a.getElementsByTag("img").get(0);
                if (img != null) {
                    info.setImgUrl(img.attr("src"));
                }
                Element aRight = e.getElementsByClass("a_right").get(0);
                String head = aRight.getElementsByTag("h1").get(0).text();
                info.setTitle(head);
                String author = aRight.getElementsByTag("p").get(0).text();
                info.setCome(author);
                String text = aRight.getElementsByClass("a_article").get(0).text();
                info.setText(text);
                Elements tags = aRight.getElementsByClass("a_idea").get(0).getElementsByTag("a");
                String detail = "";
                for (Element tag : tags) {
                    detail += tag.text() + "  ";
                }
                info.setDetail(detail);

                mDatas.add(info);
            }
        }
    }

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(ListReadCollectionActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mAdapter == null) {
                mAdapter = new ReadCollectionRecyclerAdapter(ListReadCollectionActivity.this,
                        mDatas, 1);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new ReadCollectionRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(ListReadCollectionActivity.this, ReadCollectionActivity.class);
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
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setLoadingMoreEnabled(false);
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
