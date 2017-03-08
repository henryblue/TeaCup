package com.app.teacup.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.app.teacup.ShowPhotoListActivity;
import com.app.teacup.adapter.PhotoDoubanRecyclerAdapter;
import com.app.teacup.bean.PhotoInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.urlUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class DoubanMeiziFragment extends BaseFragment {

    private List<PhotoInfo> mImgUrl;
    private ArrayList<String> mImageUrls;
    private PhotoDoubanRecyclerAdapter mPhotoRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
        mImageUrls = new ArrayList<>();
        mRequestUrl = urlUtils.DOUBAN_MEINV_URL;
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
        if (mImgUrl.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData(urlUtils.DOUBAN_MEINV_NEXT_URL, 50);
        }
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mPhotoRecyclerAdapter = new PhotoDoubanRecyclerAdapter(getContext(),
                mImgUrl);
        mRecyclerView.setAdapter(mPhotoRecyclerAdapter);
        mPhotoRecyclerAdapter.setOnItemClickListener(new PhotoDoubanRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(getContext(), ShowPhotoListActivity.class);
                    intent.putStringArrayListExtra("photoList", mImageUrls);
                    intent.putExtra("photoPos", position);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        try {
            Element main = document.getElementById("main");
            Elements liElements = main.getElementsByTag("li");
            for (Element element : liElements) {

                Elements aElements = element.getElementsByTag("a");
                for (Element a : aElements) {
                    Elements height_min = a.getElementsByClass("height_min");
                    for (Element height : height_min) {
                        PhotoInfo info = new PhotoInfo();
                        String url = height.attr("src");
                        if (url.contains(".jpg")) {
                            info.setImgUrl(url);
                            mImageUrls.add(url);
                        }
                        String title = height.attr("title");
                        info.setTitle(title);
                        mImgUrl.add(info);
                    }
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "DoubanMeiziFragment::parseData: ==error==" + e.getMessage());
        }
    }

    @Override
    protected void startRefreshData() {
        mImgUrl.clear();
        mImageUrls.clear();
        super.startRefreshData();
    }

    @Override
    protected void onLoadDataFinish() {
        super.onLoadDataFinish();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshFinish() {
        super.onRefreshFinish();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }
}
