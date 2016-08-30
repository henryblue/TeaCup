package com.app.teacup;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.app.adapter.ReadTopicRecyclerAdapter;
import com.app.bean.Read.ReadTopicInfo;
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
    private static final String TAG = "readCollectionActivity";

    private List<ReadTopicInfo> mDatas;
    private XRecyclerView mRecyclerView;
    private ReadTopicRecyclerAdapter mAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    mRecyclerView.refreshComplete();
                    //initData();
                    break;
                case LOAD_DATA_ERROR:
                    Toast.makeText(ReadCollectionActivity.this, getString(R.string.not_have_more_data),
                            Toast.LENGTH_SHORT).show();
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
        startRefreshData();
    }

    private void startRefreshData() {
        mDatas.clear();
        String readUrl = getIntent().getStringExtra("readTopicUrl");
        Log.i(TAG, "url===" + readUrl);
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
        Message msg = Message.obtain();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    private void parseData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element mainLeft = document.getElementsByClass("main_left").get(0);
            Element personalCont = mainLeft.getElementsByClass("personal_cont").get(0);
            Elements createContent = personalCont.getElementsByClass("create_content");
            for (Element e : createContent) {
                Element movingCont = e.getElementsByClass("moving_cont").get(0);
                Element heading = movingCont.getElementsByClass("heading").get(0);
                Element a = heading.getElementsByTag("a").get(0);
                String nextUrl = a.attr("href");
                String title = a.text();
                Log.i(TAG, "nextUrl=" + nextUrl);
                Log.i(TAG, "title==" + title);
                Element wordArticle = movingCont.getElementsByClass("word_article").get(0);
                Element clearfix = wordArticle.getElementsByClass("clearfix").get(0);
                Elements a1 = clearfix.getElementsByTag("a");
                for (Element e1 : a1) {
                    String text = e1.text();
                    if (TextUtils.isEmpty(text)) {
                        Element img = e1.getElementsByTag("img").get(0);
                        if (img != null) {
                            String[] srcs = img.attr("src").split("!");
                            Log.i(TAG, "imgUrl=" + srcs[0]);
                        }
                    } else {
                        Log.i(TAG, "text=" + text);
                    }
                }

                Element moreOperate = e.getElementsByClass("more_operate").get(0);
                String come = moreOperate.getElementsByClass("moving_come").get(0).text();
                String detail = moreOperate.getElementsByClass("icon").get(0).text();
                Log.i(TAG, "come=" + come);
                Log.i(TAG, "detail=" + detail);
            }
        }
    }

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(ReadCollectionActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mAdapter == null) {
                mAdapter = new ReadTopicRecyclerAdapter(ReadCollectionActivity.this,
                        mDatas);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new ReadTopicRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_normal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                return true;
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
