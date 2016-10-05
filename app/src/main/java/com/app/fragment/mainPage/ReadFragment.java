package com.app.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.adapter.ReadRecyclerAdapter;
import com.app.bean.News.NewsHeadInfo;
import com.app.bean.Read.ReadCadInfo;
import com.app.bean.Read.ReadInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.ListReadCollectionActivity;
import com.app.teacup.ListReadTopicActivity;
import com.app.teacup.R;
import com.app.teacup.ReadCollectionActivity;
import com.app.teacup.ReadDetailActivity;
import com.app.teacup.ReadTopicActivity;
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
 * 数据来源片刻网
 * @author henry-blue
 */
public class ReadFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mRefreshLayout;
    private XRecyclerView mRecyclerView;
    private List<ReadInfo> mReadDatas;
    private List<NewsHeadInfo> mHeadDatas;
    private List<ReadCadInfo> mTopicDatas;
    private ReadRecyclerAdapter mReadRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mReadDatas = new ArrayList<>();
        mHeadDatas = new ArrayList<>();
        mTopicDatas = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reading_fragment, container, false);
        initView(view);
        setupRefreshLayout();
        setupRecycleView();
        return view;
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
        mRecyclerView = (XRecyclerView) view.findViewById(R.id.base_recycler_view);
    }

    private void setupRefreshLayout() {
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mRefreshLayout.setProgressViewEndTarget(true, 100);
        mRefreshLayout.setOnRefreshListener(this);
        StartRefreshPage();
    }

    private void setupRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLoadingMoreEnabled(false);
        mRecyclerView.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);

        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                startRefreshData();
            }

            @Override
            public void onLoadMore() {
            }
        });
    }

    private void startLoadData() {
        if (mReadRecyclerAdapter == null) {
            mReadRecyclerAdapter = new ReadRecyclerAdapter(getContext(), mReadDatas, mHeadDatas, mTopicDatas);
            mRecyclerView.setAdapter(mReadRecyclerAdapter);
            mReadRecyclerAdapter.setOnItemClickListener(new ReadItemClickListener());
        } else {
            mReadRecyclerAdapter.notifyDataSetChanged();
        }
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
        mReadDatas.clear();
        mHeadDatas.clear();
        HttpUtils.sendHttpRequest(urlUtils.READ_URL, new HttpUtils.HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseNewsData(response);
                sendParseDataMessage(REFRESH_FINISH);
            }

            @Override
            public void onError(Exception e) {
                sendParseDataMessage(REFRESH_ERROR);
            }
        });
    }

    private void parseNewsData(String response) {
        Document document = Jsoup.parse(response);
        Element mainLeft = document.getElementsByClass("main_left").get(0);
        if (mainLeft == null) {
            return;
        }
        Element banner = mainLeft.getElementsByClass("column-banner").get(0);
        Element slider = banner.getElementsByClass("slide-main").get(0);
        Element ul = slider.getElementsByTag("ul").get(0);
        Elements lis = ul.getElementsByTag("li");
        for (Element li : lis) {
            NewsHeadInfo info = new NewsHeadInfo();
            Element element = li.getElementsByTag("a").get(0);
            String href = element.attr("href");
            info.setNewsUrl(urlUtils.READ_URL_NEXT_HEAD + href);
            Element img = element.getElementsByTag("img").get(0);
            info.setImgUrl(img.attr("src"));
            Element cover = li.getElementsByClass("cover").get(0);
            String title = cover.getElementsByClass("article-name").get(0).text();
            info.setNewsTitle(title);
            mHeadDatas.add(info);
        }

        Element story = mainLeft.getElementsByClass("story-category").get(0);
        Element bd = story.getElementsByClass("bd").get(0);
        Elements ul1 = bd.getElementsByTag("ul");
        for (Element e : ul1) {
            Elements liNum = e.getElementsByTag("li");
            for (Element lie : liNum) {
                ReadInfo info = new ReadInfo();
                Element a = lie.getElementsByClass("cover").get(0).getElementsByTag("a").get(0);
                String nextUrl = urlUtils.READ_URL_NEXT_HEAD + a.attr("href");
                String imgurl = a.getElementsByTag("img").get(0).attr("src");
                info.setNextUrl(nextUrl);
                info.setImgurl(imgurl);
                String title = lie.getElementsByClass("title").get(0).getElementsByTag("a").text();
                info.setTitle(title);
                String author = lie.getElementsByClass("writer").get(0).text();
                info.setAuthor(author);
                String recommend = lie.getElementsByClass("intro").get(0).text();
                info.setRecommend(recommend);
                String readNum = lie.getElementsByClass("times").get(0).text();
                info.setReadNum(readNum);
                mReadDatas.add(info);
            }
        }

        Element specialTopic = mainLeft.getElementsByClass("column-category").get(0);
        setTopicCollectData(specialTopic);
        Element collect = mainLeft.getElementsByClass("articles-category").get(0);
        setTopicCollectData(collect);
    }

    private void setTopicCollectData(Element specialTopic) {
        ReadCadInfo topicInfo = new ReadCadInfo();
        Element lined = specialTopic.getElementsByClass("lined").get(0);
        String[] text = lined.text().split(" ");
        topicInfo.setCadTitle(text[0]);
        topicInfo.setCadContent(text[1]);
        topicInfo.setMore(text[2]);
        String moreUrl = urlUtils.READ_URL_NEXT_HEAD + lined.getElementsByTag("a").get(0).attr("href");
        topicInfo.setMoreUrl(moreUrl);
        List<ReadInfo> list = new ArrayList<>();
        Element bd1 = specialTopic.getElementsByClass("bd").get(0);
        Elements topicLis = bd1.getElementsByTag("li");
        for (Element e : topicLis) {
            ReadInfo info = new ReadInfo();
            Element a = e.getElementsByClass("cover").get(0).getElementsByTag("a").get(0);
            String nextUrl = urlUtils.READ_URL_NEXT_HEAD + a.attr("href");
            info.setNextUrl(nextUrl);
            String imgurl = a.getElementsByTag("img").get(0).attr("src");
            info.setImgurl(imgurl);
            String title = e.getElementsByClass("title").get(0).text();
            info.setTitle(title);
            String author = e.getElementsByClass("total").get(0).text();
            info.setAuthor(author);
            list.add(info);
        }
        topicInfo.setReadList(list);
        mTopicDatas.add(topicInfo);
    }

    @Override
    protected void onLoadDataError() {
    }

    @Override
    protected void onLoadDataFinish() {
    }

    @Override
    protected void onRefreshError() {
        mRefreshLayout.setRefreshing(false);
        mRecyclerView.refreshComplete();
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        if (mReadDatas.size() <= 0) {
            Toast.makeText(getContext(), getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            startLoadData();
        }
        mRecyclerView.refreshComplete();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onRefreshStart() {
        startRefreshData();
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class ReadItemClickListener implements ReadRecyclerAdapter.OnItemClickListener {

        @Override
        public void onHeadClick(View view, int position) {
            Intent intent = new Intent(getContext(), ReadDetailActivity.class);
            intent.putExtra("readDetailUrl", mHeadDatas.get(position).getNewsUrl());
            intent.putExtra("readTitle", mHeadDatas.get(position).getNewsTitle());
            startActivity(intent);
        }

        @Override
        public void onItemClick(View view, int position) {
            Intent intent = new Intent(getContext(), ReadDetailActivity.class);
            intent.putExtra("readDetailUrl", mReadDatas.get(position).getNextUrl());
            intent.putExtra("readTitle", mReadDatas.get(position).getTitle());
            startActivity(intent);
        }

        @Override
        public void onTopicClick(int typePos, int position) {
            switch (typePos) {
                case 0:
                    enterNextPage(typePos, position, ReadTopicActivity.class);
                    break;
                case 1:
                    enterNextPage(typePos, position, ReadCollectionActivity.class);
                    break;
                default:
                    break;
            }
        }

        private void enterNextPage(int typePos, int position, Class<?> activityClass) {
            ReadCadInfo cadInfo = mTopicDatas.get(typePos);
            ReadInfo readInfo = cadInfo.getReadList().get(position);
            Intent intent = new Intent(getContext(), activityClass);
            intent.putExtra("readTitle", readInfo.getTitle());
            intent.putExtra("readTopicUrl", readInfo.getNextUrl());
            startActivity(intent);
        }

        private void enterMorePage(int typePos, String title, Class<?> activityClass) {
            ReadCadInfo cadInfo = mTopicDatas.get(typePos);
            Intent intent = new Intent(getContext(), activityClass);
            intent.putExtra("readTitle", title);
            intent.putExtra("readTopicUrl", cadInfo.getMoreUrl());
            startActivity(intent);
        }

        @Override
        public void onLoadMore(int typePos) {
            switch (typePos) {
                case 0:
                    enterMorePage(typePos, mTopicDatas.get(typePos).getCadContent(),
                            ListReadTopicActivity.class);
                    break;
                case 1:
                    enterMorePage(typePos, mTopicDatas.get(typePos).getCadContent(),
                            ListReadCollectionActivity.class);
                    break;
                default:
                    break;
            }
        }
    }
}

