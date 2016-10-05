package com.app.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.adapter.MusicRecyclerAdapter;
import com.app.bean.Music.MusicInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.MusicDetailActivity;
import com.app.teacup.R;
import com.app.teacup.TingDetailActivity;
import com.app.util.HttpUtils;
import com.app.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerManager;

/**
 * 数据来源于悦耳有声
 * @author henry-blue
 */
public class TingFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private List<MusicInfo> mMusicDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private MusicRecyclerAdapter mMusicRecyclerAdapter;
    private int mPageNum = 1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMusicDatas = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ting_fragment, container, false);
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
                if (mMusicDatas.size() <= 0) {
                    mRecyclerView.loadMoreComplete();
                } else {
                    startLoadData();
                }
            }
        });

        mMusicRecyclerAdapter = new MusicRecyclerAdapter(getContext(), mMusicDatas, 1);
        mRecyclerView.setAdapter(mMusicRecyclerAdapter);
        mMusicRecyclerAdapter.setOnItemClickListener(new MusicRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), TingDetailActivity.class);
                intent.putExtra("ting", mMusicDatas.get(position));
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                view.findViewById(R.id.iv_ting_img),
                                getString(R.string.transition_ting_img));

                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
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
        mMusicDatas.clear();
        HttpUtils.sendHttpRequest(urlUtils.TING_YUETING_URL, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseMusicData(response);
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
        if (mPageNum > 10) {
            mRecyclerView.loadMoreComplete();
            Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String url = urlUtils.TING_NEXT_URL_HEAD + mPageNum;
        HttpUtils.sendHttpRequest(url, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseMusicData(response);
                sendParseDataMessage(LOAD_DATA_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }
        });
    }

    private void parseMusicData(String response) {
        Document document = Jsoup.parse(response);
        if (document != null) {
            Element home = document.getElementsByClass("home").get(0);
            Element content = home.getElementById("content");
            Elements posts = content.getElementsByClass("post");
            for (Element post : posts) {
                MusicInfo info = new MusicInfo();
                Element posTitle = post.getElementsByClass("post-title").get(0);
                String nextUrl = posTitle.getElementsByTag("a").get(0).attr("href");
                info.setNextUrl(nextUrl);
                info.setTitle(posTitle.text());
                String detail = post.getElementsByClass("post-meta").get(0).text();
                info.setInfoNum(detail);
                Element posContent = post.getElementsByClass("post-content").get(0);
                Element thumb = posContent.getElementsByClass("post-thumbnail").get(0);
                String imgUrl = thumb.getElementsByTag("img").get(0).attr("src");
                String[] urlSplit = imgUrl.split("\\?");
                info.setImgUrl(urlSplit[0]);
                String text = posContent.getElementsByClass("post-text").get(0).text();
                info.setHappyNum(text);
                mMusicDatas.add(info);
            }
        }
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
        mRecyclerView.loadMoreComplete();
    }

    @Override
    protected void onLoadDataFinish() {
        mRecyclerView.loadMoreComplete();
        mMusicRecyclerAdapter.reSetData(mMusicDatas);
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
        mMusicRecyclerAdapter.reSetData(mMusicDatas);
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
