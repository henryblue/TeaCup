package com.app.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.app.adapter.FindRecycleAdapter;
import com.app.bean.BookInfo;
import com.app.bean.FindBookInfo;
import com.app.teacup.BookDetailActivity;
import com.app.teacup.R;
import com.app.util.HttpUtils;
import com.app.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class FindFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REFRESH_START = 0;
    private static final int REFRESH_FINISH = 1;
    private static final int LOAD_DATA = 2;

    private ArrayList<FindBookInfo> mDatas;
    private LinearLayoutManager mLayoutManager;
    private FloatingActionButton mAddBtn;
    private SwipeRefreshLayout mRefreshLayout;
    private FindRecycleAdapter mAdapter;

    private int mLoadIndex = 0;

    int[] ids = new int[] {1003078, 26733854, 26340138, 1919201,
            2340100, 22372723, 26768309, 25862578, 10763902, 1770782,
            26776393, 3056906, 1474773, 7060185, 3369793, 26613052,
            4207781, 26791998, 1001885, 6126821};

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_START:
                    initData();
                    break;
                case REFRESH_FINISH:
                    mRefreshLayout.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
                    break;
                case LOAD_DATA:
                    mLoadIndex++;
                    initData();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDatas = new ArrayList<>();
    }

    private void initData() {
        if (((mLoadIndex + 1) % 5) != 0 && mLoadIndex < ids.length) {
            String url = getString(R.string.url_address) + ids[mLoadIndex];
            HttpUtils.sendHttpRequest(url, new HttpUtils.HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    parseDataFromJson(response);
                    sendParseDataMessage();
                }

                @Override
                public void onError(Exception e) {
                    sendParseDataMessage();
                }
            });
        } else {
            Message msg = Message.obtain();
            msg.what = REFRESH_FINISH;
            mHandler.sendMessage(msg);
        }
    }

    public void sendParseDataMessage() {
        Message msg = Message.obtain();
        msg.what = LOAD_DATA;
        mHandler.sendMessage(msg);
    }

    private void parseDataFromJson(String response) {
        BookInfo bookInfo = JsonUtils.parseJsonData(response);
        Log.i("bookInfo", "======bookInfo===" + bookInfo.getTitle());

        FindBookInfo info = new FindBookInfo();
        info.setmBookTitle(bookInfo.getTitle());
        info.setmImgUrl(bookInfo.getImage());
        info.setmSummary(bookInfo.getSummary());
        info.setmAuthor(bookInfo.getAuthor_intro());

        List<String> authors = bookInfo.getAuthor();
        String authorArr = "";
        for (int i = 0; i < authors.size(); i++) {
            authorArr += authors.get(i) + " ";
        }
        String page = bookInfo.getPages();
        String price = bookInfo.getPrice();
        String pubdate = bookInfo.getPubdate();
        BookInfo.rating rating = bookInfo.getRating();
        String average = rating.getAverage();
        List<BookInfo.tags> tags = bookInfo.getTags();
        String type = tags.get(1).getName();
        String content = "作者: " + authorArr + "\n" +
                "类型: " + type + "\n" +
                "豆瓣评分: " + average + "\n" +
                "页数: " + page + "\n" +
                "价格: " + price + "\n" +
                "出版日期: " + pubdate;
        info.setmBookContent(content);
        mDatas.add(info);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.find_fragment, container, false);

        setupRecycleView(view);
        setupFAB(view);
        setupRefreshLayout(view);
        return view;
    }

    private void setupRefreshLayout(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        mRefreshLayout.setOnRefreshListener(this);
        StartRefreshPage();
    }

    private void StartRefreshPage() {
        mRefreshLayout.post(new Runnable(){
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    private void setupRecycleView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.base_recycler_view);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new FindRecycleAdapter(getContext(), mDatas);
        mAdapter.setOnItemClickListener(new FindRecycleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), BookDetailActivity.class);
                intent.putExtra("book", mDatas.get(position));

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                view.findViewById(R.id.iv_book_img),
                                getString(R.string.transition_book_img));

                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });

        recyclerView.setAdapter(mAdapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLoadIndex + 1 == mAdapter.getItemCount()) {
                    StartRefreshPage();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void setupFAB(View view) {
        mAddBtn = (FloatingActionButton) view.findViewById(R.id.fab_btn_add);
        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
    }

    private void showAddDialog() {
        final EditText editText = new EditText(getContext());
        editText.setHint(R.string.input_hint);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.add_book)
                .setView(editText, 30, 20, 20, 20)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doSearch(editText.getText().toString());
                    }
                });
        editText.requestFocus();
        builder.create().show();
    }

    private void doSearch(String bookName) {
        if (!TextUtils.isEmpty(bookName)) {

        }
    }

    @Override
    public void onRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = REFRESH_START;
                mHandler.sendMessage(msg);
            }
        }).start();
    }
}
