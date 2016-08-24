package com.app.fragment.mainPage;


import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.bean.Read.ReadCadInfo;
import com.app.bean.Read.ReadInfo;
import com.app.fragment.BaseFragment;
import com.app.teacup.R;
import com.app.util.HttpUtils;
import com.app.util.urlUtils;

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
    private List<ReadInfo> mReadDatas;
    private List<String> mHeadDatas;
    private ReadCadInfo mSpecialTopic;
    private ReadCadInfo mCollection;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mReadDatas = new ArrayList<>();
        mHeadDatas = new ArrayList<>();
        mSpecialTopic = new ReadCadInfo();
        mCollection = new ReadCadInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reading_fragment, container, false);
        initView(view);
        setupRefreshLayout();
        return view;
    }

    private void initView(View view) {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_refresh);
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
            Element img = li.getElementsByTag("a").get(0).getElementsByTag("img").get(0);
            String imgUrl = img.attr("src");
            mHeadDatas.add(imgUrl);
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
        setTopicCollectData(specialTopic, mSpecialTopic);
        Element collect = mainLeft.getElementsByClass("articles-category").get(0);
        setTopicCollectData(collect, mCollection);
    }

    private void setTopicCollectData(Element specialTopic, ReadCadInfo topicInfo) {
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
        Toast.makeText(getContext(), getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRefreshFinish() {
        if (mReadDatas.size() <= 0) {
            Toast.makeText(getContext(), getString(R.string.screen_shield),
                    Toast.LENGTH_SHORT).show();
        } else {
            mRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onRefreshStart() {
        startRefreshData();
    }

    @Override
    public void onRefresh() {
        sendParseDataMessage(REFRESH_START);
    }

    public void sendParseDataMessage(int message) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = message;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}

