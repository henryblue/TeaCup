package com.app.adapter;


import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.bean.News.NewsInfo;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;
    private List<NewsInfo> mDatas;
    private List<ImageView> mImageViewList;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;
    private HeaderViewHolder mHeaderViewHolder;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public NewsRecyclerAdapter(Context context, List<NewsInfo> datas, List<ImageView> imageViews) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        mImageViewList = imageViews;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            if (mHeaderViewHolder == null) {
                mHeaderViewHolder = new HeaderViewHolder(mLayoutInflater.inflate(R.layout.item_news_header, parent, false));
            }
            return mHeaderViewHolder;
            } else {
            return new NewsViewHolder(mLayoutInflater.inflate(R.layout.item_news_view, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NewsViewHolder) {
            onBindSingleItemViewHolder(holder, position);
        } else if (holder instanceof HeaderViewHolder) {
            onBindHeaderItemViewHolder(holder, position);
        }
    }

    private void onBindHeaderItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        HeaderViewHolder myHolder = (HeaderViewHolder) holder;
        myHolder.mAdapter.notifyDataSetChanged();
    }

    private void onBindSingleItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        NewsInfo info = mDatas.get(position - 1);
        String url = info.getImgUrl();

        final NewsViewHolder myHolder = (NewsViewHolder) holder;
        Glide.with(mContext).load(url).asBitmap()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.main_load_bg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(myHolder.mPhotoImg);

        myHolder.mTitle.setText(info.getTitle());
        myHolder.mLabel.setText(info.getLabel());

        if (mListener != null) {
            myHolder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getRealPosition(myHolder);
                    mListener.onItemClick(myHolder.itemView, pos);
                }
            });
        }
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return position - 2;
    }

    public void reSetData(List<NewsInfo> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    public void startHeaderAutoScrolled() {
        if (mHeaderViewHolder != null) {
            mHeaderViewHolder.startAutoScrolled();
        }
    }

    public void stopHeaderAutoScrolled() {
        if (mHeaderViewHolder != null) {
            mHeaderViewHolder.stopAutoScrolled();
        }
    }

    public void setHeaderVisible(int visible) {
        mHeaderViewHolder.setGroupVisible(visible);
    }

    private class NewsViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private ImageView mPhotoImg;
        private TextView mLabel;
        private View mainView;

        public NewsViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_news_img);
            mTitle = (TextView) itemView.findViewById(R.id.tv_news_title);
            mLabel = (TextView) itemView.findViewById(R.id.tv_news_label);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private ReactViewPagerAdapter mAdapter;
        private ViewPager mViewPager;
        private LinearLayout mGroup;
        private int mLastPos = 0;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mViewPager = (ViewPager) itemView.findViewById(R.id.vp_news);
            mGroup = (LinearLayout) itemView.findViewById(R.id.ll_group);

            if (mImageViewList.size() <= 0) {
                return;
            }

            for (int i = 0; i < mImageViewList.size(); i++) {
                ImageView point = new ImageView(mContext);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                        (ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                params.rightMargin = 16;
                point.setLayoutParams(params);
                point.setBackgroundResource(R.drawable.point_bg);
                if (i == 0) {
                    point.setEnabled(true);
                } else {
                    point.setEnabled(false);
                }
                mGroup.addView(point);
            }

            mAdapter = new ReactViewPagerAdapter(mViewPager, mImageViewList);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 -
                    (Integer.MAX_VALUE / 2 % mImageViewList.size()));

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    position = position % mImageViewList.size();
                    mGroup.getChildAt(position).setEnabled(true);
                    mGroup.getChildAt(mLastPos).setEnabled(false);
                    mLastPos = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }
        public void startAutoScrolled() {
            if (mAdapter != null) {
                mAdapter.startAutoScrolled();
            }
        }

        public void stopAutoScrolled() {
            if (mAdapter != null) {
                mAdapter.stopAutoScrolled();
            }
        }

        public void setGroupVisible(int visible) {
            mGroup.setVisibility(visible);
        }
    }

}
