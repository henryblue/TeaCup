package com.app.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.adapter.PhotoQiubaiRecyclerAdapter;
import com.app.bean.PhotoInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.app.util.HttpUtils;
import com.app.util.urlUtils;

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
        HttpUtils.sendHttpRequest(urlUtils.QIUBAI18_URL, new HttpUtils.HttpCallBackListener() {
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
        if (mPageNum > 50) {
            mRecyclerView.loadMoreComplete();
            Toast.makeText(getContext(), R.string.not_have_more_data,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String url = urlUtils.QIUBAI18_NEXT_URL + mPageNum + "/";
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
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
        mPhotoRecyclerAdapter.reSetData(mImgUrl);
    }
}
