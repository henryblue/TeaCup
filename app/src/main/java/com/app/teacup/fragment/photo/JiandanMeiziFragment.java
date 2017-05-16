package com.app.teacup.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.teacup.R;
import com.app.teacup.ShowPhotoListActivity;
import com.app.teacup.adapter.PhotoRecyclerAdapter;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.manager.WrapContentGridLayoutManager;
import com.app.teacup.util.urlUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class JiandanMeiziFragment extends BaseFragment {

    private ArrayList<String> mImgUrl;
    private int mainPageId = -1;
    private PhotoRecyclerAdapter mPhotoRecyclerAdapter;
    private boolean mIsFirstEnter = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
        mRequestUrl = urlUtils.JIANDAN_URL;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirstEnter) {
            mIsInitData = true;
            super.onFragmentVisible();
        }
        mIsFirstEnter = false;
    }

    @Override
    protected void onRecyclerViewResponseRefresh() {
        mainPageId = -1;
        super.onRecyclerViewResponseRefresh();
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
        if (mImgUrl.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            mainPageId--;
            startLoadData(urlUtils.JIANDAN_NEXT_URL + mainPageId, -1);
        }
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        WrapContentGridLayoutManager manager = new WrapContentGridLayoutManager(getContext(), 2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        int itemSpace = getResources().
                getDimensionPixelSize(R.dimen.item_photo_view_item_margin);

        mPhotoRecyclerAdapter = new PhotoRecyclerAdapter(getContext(), mImgUrl);
        mPhotoRecyclerAdapter.setHasStableIds(true);
        mRecyclerView.addItemDecoration(mPhotoRecyclerAdapter.new SpaceItemDecoration(itemSpace));
        mRecyclerView.setAdapter(mPhotoRecyclerAdapter);

        mPhotoRecyclerAdapter.setOnItemClickListener(new PhotoRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = mImgUrl.get(position);
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(getContext(), ShowPhotoListActivity.class);
                    intent.putStringArrayListExtra("photoList", mImgUrl);
                    intent.putExtra("photoPos", position);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void startRefreshData() {
        mImgUrl.clear();
        mRecyclerView.removeAllViews();
        super.startRefreshData();
    }

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        try {
            Elements commentlist = document.getElementsByClass("commentlist");
            for (Element element : commentlist) {
                Elements li = element.getElementsByTag("li");
                for (Element li1 : li) {
                    Element test = li1.getElementsByClass("text").get(0);
                    Elements a = test.getElementsByTag("a");
                    for (Element a1 : a) {
                        String imgUrl = a1.attr("href");
                        if (imgUrl.contains(".jpg")) {
                            mImgUrl.add("http:" + imgUrl);
                        }
                    }
                }
            }
            if (mainPageId == -1) {
                Elements comments = document.getElementsByClass("comments");
                for (Element current : comments) {
                    Elements currentPage = current.getElementsByClass("current-comment-page");
                    String text = currentPage.text();
                    if (!TextUtils.isEmpty(text)) {
                        String[] splits = text.split(" ");
                        String tit = splits[0];
                        String subStr = tit.substring(1, tit.length() - 1);
                        mainPageId = Integer.valueOf(subStr);
                    }
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "JiandanMeiziFragment::parseData: ==error==" + e.getMessage());
        }
    }

    @Override
    protected void onLoadDataFinish() {
        super.onLoadDataFinish();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshFinish() {
        super.onRefreshFinish();
        if (mImgUrl.size() <= 0) {
            Toast.makeText(getContext(), getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            mPhotoRecyclerAdapter.refreshData(mImgUrl);
        }
    }
}
