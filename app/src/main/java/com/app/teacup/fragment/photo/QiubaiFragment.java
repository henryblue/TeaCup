package com.app.teacup.fragment.photo;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.app.teacup.adapter.PhotoQiubaiRecyclerAdapter;
import com.app.teacup.bean.PhotoInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.HttpUtils;
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
        mPageNum = 1;
        if (TextUtils.isEmpty(mRequestUrl)) {
            throw new RuntimeException(
                    "Can't start request data that has not set RequestUrl");
        }

        HttpUtils.sendHttpRequestWithCharset(mRequestUrl, "GBK", new HttpUtils.HttpCallBackListener() {

            @Override
            public void onFinish(String response) {
                parseLoadData(response);
                sendParseDataMessage(REFRESH_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }
        });
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
        if (mImgUrl.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData(50);
        }
    }

    @SuppressLint("DefaultLocale")
    private void startLoadData(int maxLoadNum) {
        String loadUrl = urlUtils.QIUBAI18_URL;
        if (maxLoadNum > 0) {
            mPageNum++;
            if (mPageNum > maxLoadNum) {
                mRecyclerView.loadMoreComplete();
                Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            loadUrl = String.format("http://m.qiubaichengren.net/gif/list_%d.html", mPageNum);
        }

        HttpUtils.sendHttpRequestWithCharset(loadUrl, "GBK", new HttpUtils.HttpCallBackListener() {

            @Override
            public void onFinish(String response) {
                parseLoadData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }
        });
    }

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        try {
            if (document != null) {
                Element article = document.getElementsByClass("article").get(0);
                Elements lis = article.getElementsByTag("li");
                for (Element li : lis) {
                    PhotoInfo info = new PhotoInfo();
                    Element img = li.getElementsByTag("img").get(0);
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
