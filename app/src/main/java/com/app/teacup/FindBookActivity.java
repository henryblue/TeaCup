package com.app.teacup;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.teacup.adapter.FindRecycleAdapter;
import com.app.teacup.bean.book.Book;
import com.app.teacup.bean.book.BookInfo;
import com.app.teacup.bean.book.FindBookInfo;
import com.app.teacup.util.JsonUtils;
import com.app.teacup.util.OkHttpUtils;
import com.app.teacup.util.ToolUtils;
import com.app.teacup.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.okhttp.Request;

import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FindBookActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String filename = "findBook.json";

    private ArrayList<FindBookInfo> mDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private FindRecycleAdapter mAdapter;
    private XRecyclerView recyclerView;

    private void initLoadData() {
        SharedPreferences pref = getSharedPreferences("config",
                Context.MODE_PRIVATE);
        String url = pref.getString("url", "");
        if (TextUtils.isEmpty(url)) {
            url = urlUtils.DOUBAN_URL_ADDR;
        }
        mDatas.clear();
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parseDataFromJson(response);
                writeDataToFile(response);
                sendParseDataMessage(REFRESH_FINISH);
            }
        });
    }

    private void parseDataFromJson(String response) {
        Book book = JsonUtils.parseJsonData(response);
        for (int i = 0; i < book.getCount(); i++) {
            BookInfo bookInfo = book.getBooks().get(i);
            FindBookInfo info = new FindBookInfo();
            info.setmBookTitle(bookInfo.getTitle());
            info.setmImgUrl(bookInfo.getImages().getLarge());
            info.setmSummary(bookInfo.getSummary());
            info.setmAuthor(bookInfo.getAuthor_intro());
            String authorArr = null;
            if (bookInfo.getAuthor().size() > 0) {
                authorArr = bookInfo.getAuthor().get(0);
            }
            String page = bookInfo.getPages();
            String price = bookInfo.getPrice();
            String pubdate = bookInfo.getPubdate();
            BookInfo.rating rating = bookInfo.getRating();
            String average = rating.getAverage();
            List<BookInfo.tags> tags = bookInfo.getTags();
            String type = null;
            if (tags.size() > 1) {
                type = tags.get(1).getName();
            } else if (tags.size() > 0) {
                type = tags.get(0).getName();
            }
            String content = "作者: " + authorArr + "\n" +
                    "类型: " + type + "\n" +
                    "豆瓣评分: " + average + "\n" +
                    "页数: " + page + "\n" +
                    "价格: " + price + "\n" +
                    "出版日期: " + pubdate;
            info.setmBookContent(content);
            info.setmTable(bookInfo.getCatalog());
            mDatas.add(info);
        }
    }

    private void writeDataToFile(String data) {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readDataFromFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fis;
                    fis = openFileInput(filename);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    fis.close();
                    String fileContent = EncodingUtils.getString(buffer, "UTF-8");
                    parseDataFromJson(fileContent);
                    sendParseDataMessage(REFRESH_ERROR);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_book_activity);
        mDatas = new ArrayList<>();
        initToolBar();
        setupRecycleView();
        setupRefreshLayout();
        setupFAB();
    }

    @Override
    protected void onLoadDataError() {
        recyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
        if (mDatas.size() <= 0) {
            readDataFromFile();
        } else {
            mAdapter.reSetData(mDatas);
        }
    }

    @Override
    protected void onLoadDataFinish() {

    }

    @Override
    protected void onRefreshError() {
        Toast.makeText(FindBookActivity.this, getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
        mAdapter.reSetData(mDatas);
    }

    @Override
    protected void onRefreshFinish() {
        recyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
        mAdapter.reSetData(mDatas);
    }

    @Override
    protected void onRefreshStart() {
        initLoadData();
    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.item_book));
        }
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRefreshLayout() {
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        if (mRefreshLayout != null) {
            mRefreshLayout.setColorSchemeColors(ToolUtils.getThemeColorPrimary(this));
            mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
            mRefreshLayout.setProgressViewEndTarget(true, 100);
            mRefreshLayout.setOnRefreshListener(this);
        }
        StartRefreshPage();
    }

    private void StartRefreshPage() {
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                initLoadData();
            }
        });
    }

    private void setupFAB() {
        FloatingActionButton mAddBtn = (FloatingActionButton) findViewById(R.id.fab_btn_add);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
    }

    private void setupRecycleView() {
        recyclerView = (XRecyclerView) findViewById(R.id.base_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(FindBookActivity.this);
        mLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLoadingMoreEnabled(false);
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        recyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                sendParseDataMessage(REFRESH_START);
            }

            @Override
            public void onLoadMore() {

            }
        });

        mAdapter = new FindRecycleAdapter(this, mDatas);
        mAdapter.setOnItemClickListener(new FindRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(FindBookActivity.this, BookDetailActivity.class);
                intent.putExtra("book", mDatas.get(position));

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(FindBookActivity.this,
                                view.findViewById(R.id.iv_book_img),
                                getString(R.string.transition_book_img));

                ActivityCompat.startActivity(FindBookActivity.this, intent, options.toBundle());
            }
        });

        recyclerView.setAdapter(mAdapter);
    }

    private void showAddDialog() {
        final MaterialEditText editText = new MaterialEditText(FindBookActivity.this);
        editText.setHint(R.string.input_hint);
        editText.setMetTextColor(Color.parseColor("#009688"));
        editText.setPrimaryColor(Color.parseColor("#009688"));
        editText.setMaxCharacters(20);
        editText.setErrorColor(Color.parseColor("#ff0000"));

        AlertDialog.Builder builder = new AlertDialog.Builder(FindBookActivity.this)
                .setTitle(R.string.add_book)
                .setView(editText, 30, 20, 20, 20)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doSearch(editText.getText().toString());
                    }
                });
        builder.create().show();
    }

    private void doSearch(String bookName) {
        if (!TextUtils.isEmpty(bookName)) {
            String url = urlUtils.DOUBAN_URL_SEARCH + "search?q=" + bookName + "&fields=all";
            SharedPreferences.Editor edit = getSharedPreferences("config",
                    Context.MODE_PRIVATE).edit();
            edit.putString("url", url);
            edit.apply();
            StartRefreshPage();
        }
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }
}
