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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.app.adapter.FindRecycleAdapter;
import com.app.bean.FindBookInfo;
import com.app.teacup.BookDetailActivity;
import com.app.teacup.R;


import java.util.ArrayList;

public class FindFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REFRESH_FINISH = 1;

    private ArrayList<FindBookInfo> mDatas;
    private LinearLayoutManager mLayoutManager;
    private FloatingActionButton mAddBtn;
    private SwipeRefreshLayout mRefreshLayout;
    private FindRecycleAdapter mAdapter;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_FINISH:
                    mRefreshLayout.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
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
        String title = "一个人的朝圣";
        String content = "作者: 凯文\n" +
                "标题: 文学\n" +
                "页数: 305\n" +
                "定价: 45\n" +
                "出版时间: 2013-06-12\n";
        for (int i = 0; i < 15; i++) {
            FindBookInfo info = new FindBookInfo();
            info.setmBookTitle(title);
            info.setmBookContent(content);
            mDatas.add(info);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        mAddBtn.setTranslationY(120 * 2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.find_fragment, container, false);

        setupRefreshLayout(view);
        setupRecycleView(view);
        setupFAB(view);
        return view;
    }

    private void setupRefreshLayout(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        mRefreshLayout.setOnRefreshListener(this);
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
                mDatas.clear();
                initData();
                Message msg = Message.obtain();
                msg.what = REFRESH_FINISH;
                mHandler.sendMessageDelayed(msg, 1000);
            }
        }).start();
    }
}
