package com.app.teacup.fragment.mainPage;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.View;

import com.app.teacup.MusicDetailActivity;
import com.app.teacup.R;
import com.app.teacup.adapter.MusicRecyclerAdapter;
import com.app.teacup.bean.Music.MusicInfo;
import com.app.teacup.fragment.BaseFragment;
import com.app.teacup.util.urlUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据来源于落网音乐
 * @author henryblue
 */
public class MusicFragment extends BaseFragment {

    private List<MusicInfo> mMusicDatas;
    private MusicRecyclerAdapter mMusicRecyclerAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMusicDatas = new ArrayList<>();
        mRequestUrl = urlUtils.MUSIC_URL;
    }

    @Override
    protected void startRefreshData() {
        mMusicDatas.clear();
        super.startRefreshData();
    }

    @Override
    protected void onRecycleViewResponseLoadMore() {
        if (mMusicDatas.size() <= 0) {
            mRecyclerView.loadMoreComplete();
        } else {
            startLoadData(urlUtils.MUSIC_NEXT_URL, 50);
        }
    }

    @Override
    protected void setupRecycleViewAndAdapter() {
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

    @Override
    protected void parseData(String response) {
        Document document = Jsoup.parse(response);
        try {
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
        } catch (Exception e) {
            Log.i(TAG, "MusicFragment::parseData: ==error==" + e.getMessage());
        }
    }

    @Override
    protected void onLoadDataFinish() {
        super.onLoadDataFinish();
        mMusicRecyclerAdapter.reSetData(mMusicDatas);
    }

    @Override
    protected void onRefreshFinish() {
        super.onRefreshFinish();
        mMusicRecyclerAdapter.reSetData(mMusicDatas);
    }
}
