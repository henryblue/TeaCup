package com.app.teacup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.util.HttpUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class NewsDetailActivity extends AppCompatActivity {

    private static final int LOAD_DATA_FINISH = 0;
    private static final int LOAD_DATA_ERROR = 1;
    private static final String TAG = "NewsDetailActivity";

    private LinearLayout mLinearLayout;
    private TextView mTitle;
    private TextView mAuthor;
    private List<String> mDatas;
    private String msAuthor;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA_FINISH:
                    initData();
                    mRefreshLayout.setRefreshing(false);
                    break;
                case LOAD_DATA_ERROR:
                    mRefreshLayout.setRefreshing(false);
                    Toast.makeText(NewsDetailActivity.this, getString(R.string.not_have_more_data),
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            mRefreshLayout.setEnabled(false);
            super.handleMessage(msg);
        }
    };
    private SwipeRefreshLayout mRefreshLayout;

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(NewsDetailActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuthor.setText(msAuthor);
            mTitle.setText(getIntent().getStringExtra("newsTitle"));
            loadNewsContent();
        }

    }

    private void loadNewsContent() {
        int left = getResources().getDimensionPixelOffset(R.dimen.news_detail_item_marginLeft);
        int right = getResources().getDimensionPixelOffset(R.dimen.news_detail_item_marginRight);
        int bottom = getResources().getDimensionPixelOffset(R.dimen.news_detail_item_marginBottom);
        int imgHeight = getResources().getDimensionPixelOffset(R.dimen.news_detail_item_img_height);
        int textSize = getResources().getDimensionPixelSize(R.dimen.news_detail_item_txt_textSize);

        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, imgHeight);
        imgParams.setMargins(left, 0, right, bottom);

        LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        txtParams.setMargins(left, 0, right, bottom);

        for (int i = 0; i < mDatas.size(); i++) {
            String tag = mDatas.get(i);
            if (tag.startsWith("http")) {
                ImageView view = new ImageView(this);
                view.setLayoutParams(imgParams);
                tag = tag.replace("small", "medium");
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (!MainActivity.mIsLoadPhoto) {
                    Glide.with(this).load(tag)
                            .asBitmap()
                            .error(R.drawable.photo_loaderror)
                            .placeholder(R.drawable.main_load_bg)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(view);
                } else {
                    if (MainActivity.mIsWIFIState) {
                        Glide.with(this).load(tag)
                                .asBitmap()
                                .error(R.drawable.photo_loaderror)
                                .placeholder(R.drawable.main_load_bg)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .dontAnimate()
                                .into(view);
                    } else {
                        view.setImageResource(R.drawable.main_load_bg);
                    }
                }

                final String finalTag = tag;
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(NewsDetailActivity.this, ShowPhotoActivity.class);
                        intent.putExtra("ImageUrl", finalTag);
                        startActivity(intent);
                    }
                });

                mLinearLayout.addView(view);
            } else {
                TextView textView = new TextView(this);
                textView.setLayoutParams(txtParams);
                textView.setText(tag);
                textView.setTextColor(Color.parseColor("#aa000000"));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                textView.setLineSpacing(0, 1.45f);
                mLinearLayout.addView(textView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        initToolBar();
        initView();
    }

    private void startLoadData() {
        String newsUrl = getIntent().getStringExtra("newsDetailUrl");
        if (!TextUtils.isEmpty(newsUrl)) {
            HttpUtils.sendHttpRequest(newsUrl, new HttpUtils.HttpCallBackListener() {
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
            Element maincontent = document.getElementById("maincontent");
            if (maincontent != null) {
                Element postinfo = maincontent.getElementsByClass("postinfo").get(0);
                if (postinfo != null) {
                    msAuthor = postinfo.ownText();
                }
                Element entry = maincontent.getElementsByClass("entry").get(0);
                if (entry != null) {
                    Elements ps = entry.getElementsByTag("p");
                    if (ps != null) {
                        for (Element p : ps) {
                            String element = null;
                            String text = p.text();
                            if (!TextUtils.isEmpty(text)) {
                                element = text;
                            } else {
                                Element img = p.getElementsByTag("img").get(0);
                                String url = img.attr("data-original");
                                if (!TextUtils.isEmpty(url)) {
                                    element = "http:" + url;
                                }
                            }
                            mDatas.add(element);
                        }
                    }
                }
            }
        }
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_container);
        mTitle = (TextView) findViewById(R.id.tv_new_detail_title);
        mAuthor = (TextView) findViewById(R.id.tv_news_detail_author);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srf_new_layout);
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
                startLoadData();
            }
        });
    }
    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.item_news));
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
