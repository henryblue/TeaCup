package com.app.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.adapter.PhotoGaoxiaoRecyclerAdapter;
import com.app.bean.PhotoInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.app.util.OkHttpUtils;
import com.app.util.urlUtils;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class GaoxiaoFragment extends BaseFragment {

    private int mPageNum = 1;
    private List<PhotoInfo> mImgUrl;
    private PhotoGaoxiaoRecyclerAdapter mPhotoRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
    }

    @Override
    protected void onResponseLoadMore() {
        if (mImgUrl.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData();
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
        OkHttpUtils.getAsyn(urlUtils.GAOXIAO_URL, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parsePhotoData(response);
                sendParseDataMessage(REFRESH_FINISH);
            }
        });
    }

    private void startLoadData() {
        if (mPageNum > 35) {
            Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                    Toast.LENGTH_SHORT).show();
            mRecyclerView.loadMoreComplete();
            return;
        }
        mPageNum++;

        String url = urlUtils.GAOXIAO_URL_NEXT + mPageNum + urlUtils.GAOXIAO_URL_NEXT_ID;
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

            @Override
            public void onResponse(String response) {
                parsePhotoData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }
        });
    }

    private void parsePhotoData(String response) {
        Document document = Jsoup.parse(response);
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
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(getContext(), getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
        mRecyclerView.loadMoreComplete();
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.loadMoreComplete();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }
}
