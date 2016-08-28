package com.app.teacup;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class ReadDetailActivity extends AppCompatActivity {

    private static final int LOAD_DATA_FINISH = 0;
    private static final int LOAD_DATA_ERROR = 1;
    private static final String TAG = "readDetailActivity";

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
                    break;
                case LOAD_DATA_ERROR:
                    Toast.makeText(ReadDetailActivity.this, getString(R.string.not_have_more_data),
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void initData() {
        if (mDatas.isEmpty()) {
            Toast.makeText(ReadDetailActivity.this, getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuthor.setText(msAuthor);
            mTitle.setText(getIntent().getStringExtra("readTitle"));
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
            if (tag.startsWith("http://")) {
                ImageView view = new ImageView(this);
                view.setLayoutParams(imgParams);
                tag = tag.replace("small", "medium");
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(this).load(tag)
                        .asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(view);
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
        setContentView(R.layout.activity_read_detail);
        initView();
        initToolBar();
        startLoadData();
    }

    private void startLoadData() {
        String readUrl = getIntent().getStringExtra("readDetailUrl");
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
        String newRes = document.html().replace("<br>", "\n");
        document = Jsoup.parse(newRes);
        String text = "";
        if (document != null) {
            Element article = document.getElementsByClass("article").get(0);
            Element h3 = article.getElementsByTag("h3").get(0);
            msAuthor = h3.text();
            Elements tags = article.getElementsByTag("p");
            if (tags != null) {
                for (Element tag : tags) {
                    if (!TextUtils.isEmpty(tag.text())) {
                        text += tag.text() + "\n\n";
                    } else {
                        Elements img = tag.getElementsByTag("img");
                        if (img != null) {
                            String imageUrl = img.attr("src");
                            if (!TextUtils.isEmpty(imageUrl)) {
                                mDatas.add(imageUrl);
                            }
                        }
                    }
                }
                if (!TextUtils.isEmpty(text)) {
                    mDatas.add(text);
                }
            }

            Elements pres = article.getElementsByTag("pre");
            if (pres != null) {
                for (Element pre : pres) {
                    if (!TextUtils.isEmpty(pre.text())) {
                        mDatas.add(pre.text());
                    }
                }
            }
        }
    }

    private void initView() {
        mDatas = new ArrayList<>();
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_container);
        mTitle = (TextView) findViewById(R.id.tv_read_detail_title);
        mAuthor = (TextView) findViewById(R.id.tv_read_detail_author);
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.item_read));
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
