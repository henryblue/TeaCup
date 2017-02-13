package com.app.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.adapter.FanjuRecyclerAdapter;
import com.app.bean.fanju.FanjuInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.FanjuNewsActivity;
import com.app.teacup.FanjuVideoActivity;
import com.app.teacup.R;
import com.app.util.OkHttpUtils;
import com.app.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.squareup.okhttp.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import hb.xvideoplayer.MxVideoPlayer;

/**
 * 数据来源于第一弹
 * @author henry-blue
 */
public class FanjuFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "FanjuFragment";
    private List<FanjuInfo> mVideoDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private FanjuRecyclerAdapter mFanjuRecyclerAdapter;
    private int mPageNum = 1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoDatas = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fanju_fragment, container, false);
        initView(view);
        setupRecycleView();
        setupRefreshLayout();
        return view;
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.base_recycler_view);
    }

    private void setupRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                mPageNum = 1;
                startRefreshData();
            }

            @Override
            public void onLoadMore() {
                if (mVideoDatas.size() <= 0) {
                    mRecyclerView.loadMoreComplete();
                } else {
                    startLoadData();
                }
            }
        });

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
    public void onPause() {
        super.onPause();
        MxVideoPlayer.releaseAllVideos();
    }

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        mRefreshLayout.setOnRefreshListener(this);
        StartRefreshPage();
    }

    private void StartRefreshPage() {
        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(true);
                startRefreshData();
            }
        });
    }

    private void startRefreshData() {
        mVideoDatas.clear();
        OkHttpUtils.getAsyn(urlUtils.VIDEO_DIYIDAN_URL, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }

            @Override
            public void onResponse(String response) {
                boolean isSuccess = parseVideoData(response);
                if (isSuccess) {
                    sendParseDataMessage(REFRESH_FINISH);
                } else {
                    startLoadData();
                }
            }
        });
    }

    private void startLoadData() {
        mPageNum++;
        if (mPageNum > 15) {
            mRecyclerView.loadMoreComplete();
            Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String url = urlUtils.VIDEO_DIYIDAN_URL_NEXT + mPageNum;
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

            @Override
            public void onResponse(String response) {
                boolean isSuccess = parseVideoData(response);
                if (isSuccess) {
                    sendParseDataMessage(LOAD_DATA_FINISH);
                } else {
                    sendParseDataMessage(LOAD_DATA_NONE);
                }
            }
        });
    }

    private boolean parseVideoData(String response) {
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
                    String authorImgUrl = imgInfo.attr("src");
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
                    String videoContent = yuanMiddle.getElementsByClass("yuan_con").get(0)
                            .getElementsByClass("ie2").get(0).text();
                    fanjuInfo.setVideoName(videoName);
                    fanjuInfo.setVideoImgUrl(videoImgUrl);
                    fanjuInfo.setVideoContent(videoContent);
                    mVideoDatas.add(fanjuInfo);
                }
                return true;
            } catch (Exception e) {
                Log.i(TAG, "parseVideoData: ====parse data error:===" + e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public void onLoadDataNone() {
        super.onLoadDataNone();
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.loadMoreComplete();
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.loadMoreComplete();
    }

    @Override
    protected void onLoadDataFinish() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.loadMoreComplete();
        mFanjuRecyclerAdapter.reSetData(mVideoDatas);
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
        mFanjuRecyclerAdapter.reSetData(mVideoDatas);
    }

    @Override
    protected void onRefreshStart() {
        startRefreshData();
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }

}
