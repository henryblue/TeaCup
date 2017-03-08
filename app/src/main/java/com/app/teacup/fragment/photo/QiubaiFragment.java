package com.app.teacup.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.app.teacup.ShowPhotoActivity;
import com.app.teacup.adapter.PhotoQiubaiRecyclerAdapter;
import com.app.teacup.bean.PhotoInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.urlUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class QiubaiFragment extends BaseFragment {

    private List<PhotoInfo> mImgUrl;
    private PhotoQiubaiRecyclerAdapter mPhotoRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
        mRequestUrl = urlUtils.QIUBAI18_URL;
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mPhotoRecyclerAdapter = new PhotoQiubaiRecyclerAdapter(getContext(),
                mImgUrl);
        mRecyclerView.setAdapter(mPhotoRecyclerAdapter);

        mPhotoRecyclerAdapter.setOnItemClickListener(new PhotoQiubaiRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url) && url.endsWith(".gif")) {
                    mPhotoRecyclerAdapter.startLoadImage(view, url);
                } else if (url.endsWith(".jpg")) {
                    Intent intent = new Intent(getContext(), ShowPhotoActivity.class);
                    intent.putExtra("ImageUrl", url);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                String url = mImgUrl.get(position).getImgUrl();
                if (!TextUtils.isEmpty(url) && url.endsWith(".gif")) {
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
    protected void onRecycleViewResponseLoadMore() {
        if (mImgUrl.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData(urlUtils.QIUBAI18_NEXT_URL, 50);
        }
    }

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        try {
            if (document != null) {
                Elements g = document.getElementsByClass("g");
                Element gif = g.get(1);
                Element main = gif.getElementById("main");
                Elements articles = main.getElementsByClass("row")
                        .get(0).getElementsByTag("article");

                for (Element e : articles) {
                    PhotoInfo info = new PhotoInfo();
                    Element img = e.getElementsByClass("card-bg").get(0)
                            .getElementsByClass("thumbnail-container")
                            .get(0).getElementsByTag("img").get(0);
                    String gifUrl = img.attr("src");
                    String title = img.attr("alt");
                    info.setTitle(title);
                    info.setImgUrl(gifUrl);
                    mImgUrl.add(info);
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "QuibaiFragment::parseData: ==error==" + e.getMessage());
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
