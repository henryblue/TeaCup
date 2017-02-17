package com.app.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.adapter.MusicRecyclerAdapter;
import com.app.bean.Music.MusicInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.MusicDetailActivity;
import com.app.teacup.R;
import com.app.util.HttpUtils;
import com.app.util.urlUtils;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源于落网音乐
 * @author henry-blue
 */
public class MusicFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

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
        View view = inflater.inflate(R.layout.music_fragment, container, false);
//        initView(view);
//        setupRecycleView();
//        setupRefreshLayout();
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

        mMusicRecyclerAdapter = new MusicRecyclerAdapter(getContext(), mMusicDatas);
        mRecyclerView.setAdapter(mMusicRecyclerAdapter);
        mMusicRecyclerAdapter.setOnItemClickListener(new MusicRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getContext(), MusicDetailActivity.class);
                intent.putExtra("music", mMusicDatas.get(position));

                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                view.findViewById(R.id.iv_music),
                                getString(R.string.transition_music_img));

                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });
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
        HttpUtils.sendHttpRequest(urlUtils.MUSIC_URL, new HttpUtils.HttpCallBackListener() {
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
        if (mPageNum > 85) {
            mRecyclerView.loadMoreComplete();
            Toast.makeText(getContext(), getString(R.string.not_have_more_data),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String url = urlUtils.MUSIC_NEXT_URL + mPageNum;
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
        Element container = document.getElementsByClass("container").get(0);
        Element clearfix = container.getElementsByClass("clearfix").get(0);
        Element article = clearfix.getElementsByClass("article").get(0);
        Element volList = article.getElementsByClass("vol-list").get(0);
        Elements items = volList.getElementsByClass("item");
        for (Element item : items) {
            MusicInfo info = new MusicInfo();
            Element element = item.getElementsByClass("cover-wrapper").get(0);
            String url = element.attr("href");
            info.setNextUrl(url);
            Element img = element.getElementsByTag("img").get(0);
            String src = img.attr("src");
            String[] split = src.split("\\?");
            info.setImgUrl(split[0]);

            Element meta = item.getElementsByClass("meta").get(0);
            Element name = meta.getElementsByClass("name").get(0);
            info.setTitle(name.text());

            String comments = meta.getElementsByClass("comments").get(0).text();
            String favs = meta.getElementsByClass("favs").get(0).text();
            info.setHappyNum(favs);
            info.setInfoNum(comments);
            mMusicDatas.add(info);
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
