package com.app.teacup.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.app.teacup.ShowPhotoActivity;
import com.app.teacup.adapter.PhotoGaoxiaoRecyclerAdapter;
import com.app.teacup.bean.PhotoInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.urlUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GaoxiaoFragment extends BaseFragment {

    private List<PhotoInfo> mImgUrl;
    private PhotoGaoxiaoRecyclerAdapter mPhotoRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
        mRequestUrl = urlUtils.GAOXIAO_URL;
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
        if (mImgUrl.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData(urlUtils.GAOXIAO_URL_NEXT, 35);
        }
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mPhotoRecyclerAdapter = new PhotoGaoxiaoRecyclerAdapter(getContext(),
                mImgUrl);
        mRecyclerView.setAdapter(mPhotoRecyclerAdapter);
        mPhotoRecyclerAdapter.setOnItemClickListener(new PhotoGaoxiaoRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(getContext(), ShowPhotoActivity.class);
                    intent.putExtra("ImageUrl", url);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void startRefreshData() {
        mImgUrl.clear();
        super.startRefreshData();
    }

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        try {
            Element content = document.getElementById("content-left");
            if (content == null) {
                return;
            }
            Elements articles = content.getElementsByClass("article");
            if (articles == null) {
                return;
            }
            for (Element art : articles) {
                PhotoInfo info = new PhotoInfo();

                Elements authors = art.getElementsByClass("author");
                for (Element author : authors) {
                    Elements as = author.getElementsByTag("a");
                    for (Element tit : as) {
                        String title = tit.attr("title");
                        if (!TextUtils.isEmpty(title)) {
                            info.setTitle(title);
                        }
                    }
                }

                Elements title = art.getElementsByClass("content");
                info.setContent(title.text());
                Elements thumbs = art.getElementsByClass("thumb");
                for (Element thumb : thumbs) {
                    Elements img = thumb.getElementsByTag("img");
                    for (Element e : img) {
                        String url = e.attr("src");
                        if (url.contains(".jpg") || url.contains(".gif")) {
                            info.setImgUrl(url);
                        }
                    }
                }
                mImgUrl.add(info);
            }
        } catch (Exception e) {
            Log.i(TAG, "GaoxiaoFragment::parseData: ==error==" + e.getMessage());
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
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }
}
