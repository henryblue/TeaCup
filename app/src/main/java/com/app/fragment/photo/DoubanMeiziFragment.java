package com.app.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.adapter.PhotoDoubanRecyclerAdapter;
import com.app.bean.PhotoInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.R;
import com.app.teacup.ShowPhotoListActivity;
import com.app.util.HttpUtils;
import com.app.util.urlUtils;

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
    protected void startRefreshData() {
        mImgUrl.clear();
        mImageUrls.clear();
        HttpUtils.sendHttpRequest(urlUtils.DOUBAN_MEINV_URL, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parsePhotoData(response);
                sendParseDataMessage(REFRESH_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }
        });
    }

    private void startLoadData() {
        mPageNum++;
        String url = urlUtils.DOUBAN_MEINV_NEXT_URL + mPageNum;
        HttpUtils.sendHttpRequest(url, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parsePhotoData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }
        });
    }

    private void parsePhotoData(String response) {
        Document document = Jsoup.parse(response);
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
    }

    @Override
    public void onRefresh() {
        Message msg = Message.obtain();
        msg.what = REFRESH_START;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onLoadDataError() {
        mRecyclerView.loadMoreComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.loadMoreComplete();
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }

    @Override
    protected void onRefreshError() {
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
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
