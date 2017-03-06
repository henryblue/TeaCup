package com.app.fragment.photo;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.app.adapter.PhotoRecyclerAdapter;
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

public class JiandanMeiziFragment extends BaseFragment {

    private ArrayList<String> mImgUrl;
    private int mainPageId = -1;
    private PhotoRecyclerAdapter mPhotoRecyclerAdapter;
    private boolean mIsFirstEnter = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mImgUrl = new ArrayList<>();
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
    protected void onResponseRefresh() {
        mainPageId = -1;
        super.onResponseRefresh();
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
        mPhotoRecyclerAdapter = new PhotoRecyclerAdapter(getContext(),
                mImgUrl);
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
        HttpUtils.sendHttpRequest(urlUtils.JIANDAN_URL, new HttpUtils.HttpCallBackListener() {
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
        mainPageId--;
        String url = urlUtils.JIANDAN_NEXT_URL + mainPageId + urlUtils.JIANDAN_NEXT_ID;
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
        if (mImgUrl.size() <= 0) {
            Toast.makeText(getContext(), getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            mPhotoRecyclerAdapter.reSetData(mImgUrl);
        }
    }
}
