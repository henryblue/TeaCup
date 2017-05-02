package com.app.teacup;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.teacup.adapter.FanjuNewsRecyclerAdapter;
import com.app.teacup.bean.fanju.FanjuNewInfo;
import com.app.teacup.util.OkHttpUtils;
import com.app.teacup.util.ToolUtils;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class FanjuNewsActivity extends BaseActivity {

    private List<FanjuNewInfo> mDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private FloatingActionButton mSearchBtn;
    private FanjuNewsRecyclerAdapter mAdapter;
    private boolean mIsShowSearchBtn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_fanju_news_view);
        initView();
        initToolBar();
        setupRefreshLayout();
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) findViewById(R.id.base_recycler_view);
        mSearchBtn = (FloatingActionButton) findViewById(R.id.fanju_new_btn_search);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadingMoreEnabled(false);

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });

    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getIntent().getStringExtra("fanjuNewsTitle"));
        }
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(FanjuNewsActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mAdapter == null) {
                mAdapter = new FanjuNewsRecyclerAdapter(FanjuNewsActivity.this, mDatas);
                mRecyclerView.setAdapter(mAdapter);
            }
            if (mIsShowSearchBtn) {
                mSearchBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showSearchDialog() {
        final MaterialEditText editText = new MaterialEditText(FanjuNewsActivity.this);
        editText.setHint(R.string.input_http);
        editText.setMetTextColor(Color.parseColor("#009688"));
        editText.setPrimaryColor(Color.parseColor("#009688"));
        editText.setMaxCharacters(100);
        editText.setErrorColor(Color.parseColor("#ff0000"));

        AlertDialog.Builder builder = new AlertDialog.Builder(FanjuNewsActivity.this)
                .setTitle(R.string.search_video)
                .setView(editText, 30, 20, 20, 20)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doSearch(editText.getText().toString());
                    }
                });
        builder.create().show();
    }

    private void doSearch(String url) {
        if (url.startsWith("http") && (url.endsWith("detail/1") || url.endsWith("channel=share"))) {
            Intent intent = new Intent(FanjuNewsActivity.this, FanjuVideoActivity.class);
            intent.putExtra("fanjuVideoUrl", url);
            intent.putExtra("fanjuVideoName", "");
            startActivity(intent);
        } else {
            Toast.makeText(FanjuNewsActivity.this, "解析地址失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(FanjuNewsActivity.this, getString(R.string.not_have_more_data),
                Toast.LENGTH_SHORT).show();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onLoadDataFinish() {
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setEnabled(false);
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
        mRefreshLayout.setColorSchemeColors(ToolUtils.getThemeColorPrimary(this));
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
        String newsUrl = getIntent().getStringExtra("fanjuNewsUrl");
        if (!TextUtils.isEmpty(newsUrl)) {
            OkHttpUtils.getAsyn(newsUrl, new OkHttpUtils.ResultCallback<String>() {

                @Override
                public void onError(Request request, Exception e) {
                    sendParseDataMessage(LOAD_DATA_ERROR);
                }

                @Override
                public void onResponse(String response) {
                    boolean isSuccess = parseData(response);
                    if (isSuccess) {
                        sendParseDataMessage(LOAD_DATA_FINISH);
                    } else {
                        sendParseDataMessage(LOAD_DATA_ERROR);
                    }
                }
            });
        }

    }

    private boolean parseData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            try {
                // parse post host info
                FanjuNewInfo newInfo = new FanjuNewInfo();
                Element content = document.getElementsByClass("content").get(0);
                Element postSection = content.getElementsByClass("content_left").get(0)
                        .getElementsByClass("post_section").get(0);
                Element userPost = postSection.getElementsByClass("user_post").get(0);
                Element userPostContent = userPost.getElementsByClass("user_post_content").get(0);
                newInfo.setDatas(parseUserCommits(userPostContent));
                Intent intent = getIntent();
                if (intent != null) {
                    newInfo.setUserName(intent.getStringExtra("fanjuNewsUserName"));
                    newInfo.setUserImgUrl(intent.getStringExtra("fanjuNewsUserImgUrl"));
                    newInfo.setPublishTime(intent.getStringExtra("fanjuNewsUserTime"));
                }
                mDatas.add(newInfo);

                //parse user commit
                Element userCommit = postSection.getElementsByClass("user_comments").get(0)
                        .getElementsByClass("user_comment_section").get(0);
                Elements userReply = userCommit.getElementsByClass("user_reply");
                if (userReply.size() > 0) {
                    Elements lis = userReply.get(0).getElementsByClass("clr");
                    for (Element li : lis) {
                        FanjuNewInfo info = new FanjuNewInfo();
                        Elements imgs = li.getElementsByClass("user_ti");
                        if (imgs.size() <= 0) {
                            continue;
                        }
                        Element img = imgs.get(0).getElementsByTag("img").get(0);
                        String userName = img.attr("alt");
                        String imgUrl = img.attr("src");
                        if (!imgUrl.startsWith("http")) {
                            imgUrl = "http:" + imgUrl;
                        }
                        info.setUserName(userName);
                        info.setUserImgUrl(imgUrl);

                        Elements replyRight = li.getElementsByClass("reply_right");
                        if (replyRight.size() <= 0) {
                            continue;
                        }
                        Element reply = replyRight.get(0);
                        info.setDatas(parseUserCommits(reply));
                        Elements moreUserReply = reply.getElementsByClass("more_user_reply");
                        if (moreUserReply.size() > 0) {
                            Element moreUser = moreUserReply.get(0);
                            Elements userReplyTime = moreUser.getElementsByClass("user_reply_time");
                            if (userReplyTime.size() > 0) {
                                Element userTime = userReplyTime.get(0);
                                String replyTime = userTime.getElementsByTag("span").get(0).text();
                                info.setPublishTime(replyTime);
                            }
                        }
                        mDatas.add(info);
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private List<String> parseUserCommits(Element userPostContent) {
        Elements ps = userPostContent.getElementsByTag("p");
        List<String> datas = new ArrayList<>();

        for (Element p : ps) {
            Elements as = p.getElementsByTag("a");
            String tmp;
            if (as.size() > 0) {
                tmp = "http:" + as.get(0).attr("href");
            } else {
                tmp = p.text();
            }
            if (!TextUtils.isEmpty(tmp)) {
                if (!mIsShowSearchBtn) {
                    if (tmp.contains("detail/1?channel=share")) {
                        mIsShowSearchBtn = true;
                    }
                }
                datas.add(tmp);
            }
        }

        Elements pImgs = userPostContent.getElementsByTag("post_content_img");
        for (Element img : pImgs) {
            Elements as = img.getElementsByTag("a");
            String tmp = as.attr("href");
            if (!TextUtils.isEmpty(tmp)) {
                datas.add(tmp);
            }
        }

        return datas;
    }
}
