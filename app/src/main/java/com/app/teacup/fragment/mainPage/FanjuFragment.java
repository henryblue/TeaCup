package com.app.teacup.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.app.teacup.FanjuNewsActivity;
import com.app.teacup.FanjuVideoActivity;
import com.app.teacup.adapter.FanjuRecyclerAdapter;
import com.app.teacup.bean.fanju.FanjuInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.urlUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源于第一弹
 *
 * @author henryblue
 */
public class FanjuFragment extends BaseFragment {

    private static final String TAG = "FanjuFragment";
    private List<FanjuInfo> mVideoDatas;
    private FanjuRecyclerAdapter mFanjuRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoDatas = new ArrayList<>();
        mRequestUrl = urlUtils.VIDEO_DIYIDAN_URL;
    }

    @Override
    protected void startRefreshData() {
        mVideoDatas.clear();
        mRecyclerView.removeAllViews();
        super.startRefreshData();
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
        if (mVideoDatas.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData(urlUtils.VIDEO_DIYIDAN_URL_NEXT, 18);
        }
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
        mFanjuRecyclerAdapter = new FanjuRecyclerAdapter(getContext(), mVideoDatas);
        mRecyclerView.setAdapter(mFanjuRecyclerAdapter);
        mFanjuRecyclerAdapter.setOnItemClickListener(new FanjuRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                FanjuInfo fanjuInfo = mVideoDatas.get(position);
                String videoIndexUrl = fanjuInfo.getVideoIndexUrl();

                if (TextUtils.isEmpty(videoIndexUrl)) {
                    Intent intent = new Intent(getContext(), FanjuNewsActivity.class);
                    intent.putExtra("fanjuNewsUrl", fanjuInfo.getNextUrl());
                    intent.putExtra("fanjuNewsTitle", fanjuInfo.getVideoName());
                    intent.putExtra("fanjuNewsUserImgUrl", fanjuInfo.getAuthorImgUrl());
                    intent.putExtra("fanjuNewsUserName", fanjuInfo.getAuthorName());
                    intent.putExtra("fanjuNewsUserTime", fanjuInfo.getPublishTime());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), FanjuVideoActivity.class);
                    intent.putExtra("fanjuVideoUrl", fanjuInfo.getNextUrl());
                    intent.putExtra("fanjuVideoName", fanjuInfo.getVideoName());
                    intent.putExtra("fanjuVideoImgUrl", fanjuInfo.getVideoImgUrl());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            try {
                Element content = document.getElementsByClass("content").get(0);
                Element contentLeft = content.getElementsByClass("content_left").get(0);
                Element postListBlockDiv = contentLeft.getElementsByClass("post_list_block_div").get(0);
                Element list = postListBlockDiv.getElementsByClass("hot-list").get(0);
                Elements lis = list.getElementsByTag("li");
                for (Element li : lis) {
                    FanjuInfo fanjuInfo = new FanjuInfo();
                    String originUrl = li.attr("onclick");
                    String[] split = originUrl.split("'");
                    String nextUrl = "http://www.diyidan.com" + split[1];
                    fanjuInfo.setNextUrl(nextUrl);

                    Element yuanTop = li.getElementsByClass("yuan_top").get(0);
                    Element yuanImg = yuanTop.getElementsByClass("yuan_img").get(0);
                    Element imgInfo = yuanImg.getElementsByTag("img").get(0);
                    String authorName = imgInfo.attr("alt");
                    String authorImgUrl = "http:" + imgInfo.attr("src");
                    fanjuInfo.setAuthorName(authorName);
                    fanjuInfo.setAuthorImgUrl(authorImgUrl);

                    String publishTime = yuanImg.getElementsByTag("span").text();
                    fanjuInfo.setPublishTime(publishTime);

                    Element yuanMiddle = li.getElementsByClass("yuan_middle").get(0);
                    Element shengImg = yuanMiddle.getElementsByClass("sheng_img").get(0);
                    Elements markBg = shengImg.getElementsByClass("mark_bg");
                    if (markBg.size() > 0) {
                        String indexUrl = markBg.get(0).getElementsByClass("yuan_mask")
                                .get(0).attr("src");
                        fanjuInfo.setVideoIndexUrl("http:" + indexUrl);
                    }
                    Element midImgInfo = shengImg.getElementsByTag("img").get(0);
                    String videoName = midImgInfo.attr("alt");
                    String videoImgUrl = midImgInfo.attr("src");
                    if (!videoImgUrl.startsWith("http")) {
                        videoImgUrl = "http:" + videoImgUrl;
                    }
                    String videoContent = yuanMiddle.getElementsByClass("yuan_con").get(0)
                            .getElementsByClass("ie2").get(0).text();
                    fanjuInfo.setVideoName(videoName);
                    fanjuInfo.setVideoImgUrl(videoImgUrl);
                    fanjuInfo.setVideoContent(videoContent);
                    mVideoDatas.add(fanjuInfo);
                }
            } catch (Exception e) {
                Log.i(TAG, "FanjuFragment::parseData: =error==" + e.getMessage());
            }
        }
    }

    @Override
    protected void onLoadDataFinish() {
        super.onLoadDataFinish();
        mFanjuRecyclerAdapter.reSetData(mVideoDatas);
    }

    @Override
    protected void onRefreshFinish() {
        super.onRefreshFinish();
        mFanjuRecyclerAdapter.reSetData(mVideoDatas);
    }
}
