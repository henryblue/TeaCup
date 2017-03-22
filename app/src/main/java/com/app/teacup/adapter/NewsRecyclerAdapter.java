package com.app.teacup.adapter;


import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.teacup.bean.News.NewsInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NORMAL = 1;

    private final Context mContext;
    private List<NewsInfo> mDatas;
    private final List<View> mHeaderList;
    private OnItemClickListener mListener;
    private final LayoutInflater mLayoutInflater;
    private HeaderViewHolder mHeaderViewHolder;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public NewsRecyclerAdapter(Context context, List<NewsInfo> datas, List<View> headerData) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        mHeaderList = headerData;
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
            onBindHeaderItemViewHolder(holder);
        }
    }

    private void onBindHeaderItemViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder myHolder = (HeaderViewHolder) holder;
        myHolder.mAdapter.notifyDataSetChanged();
    }

    private void onBindSingleItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        NewsInfo info = mDatas.get(position - 1);
        String url = info.getImgUrl();

        final NewsViewHolder myHolder = (NewsViewHolder) holder;
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(myHolder.mPhotoImg);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(myHolder.mPhotoImg);
            } else {
                myHolder.mPhotoImg.setImageResource(R.drawable.main_load_bg);
            }
        }

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

     private int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return position - 2;
    }

    public void reSetData(List<NewsInfo> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    public void stopHeaderAutoScrolled() {
        if (mHeaderViewHolder != null) {
            mHeaderViewHolder.stopAutoScrolled();
        }
    }

    public void startHeaderAutoScrolled() {
        if (mHeaderViewHolder != null) {
            mHeaderViewHolder.startAutoScrolled();
        }
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

    public void setHeaderVisible(int visible) {
        mHeaderViewHolder.setHeaderVisible(visible);
    }

    private class NewsViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTitle;
        private final ImageView mPhotoImg;
        private final TextView mLabel;
        private final View mainView;

        NewsViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_news_img);
            mTitle = (TextView) itemView.findViewById(R.id.tv_news_title);
            mLabel = (TextView) itemView.findViewById(R.id.tv_news_label);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private ReactViewPagerAdapter mAdapter;
        private final ViewPager mViewPager;
        private final LinearLayout mGroup;
        private final View mHeaderView;
        private int mLastPos = 0;

        HeaderViewHolder(View itemView) {
            super(itemView);
            mViewPager = (ViewPager) itemView.findViewById(R.id.vp_news);
            mGroup = (LinearLayout) itemView.findViewById(R.id.ll_group);
            mHeaderView = itemView;

            if (mHeaderList.size() <= 0) {
                return;
            }

            for (int i = 0; i < mHeaderList.size(); i++) {
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

            mAdapter = new ReactViewPagerAdapter(mViewPager, mHeaderList);
            mViewPager.setAdapter(mAdapter);
            mViewPager.setCurrentItem(Integer.MAX_VALUE / 2 -
                    (Integer.MAX_VALUE / 2 % mHeaderList.size()));

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    position = position % mHeaderList.size();
                    mGroup.getChildAt(position).setEnabled(true);
                    mGroup.getChildAt(mLastPos).setEnabled(false);
                    mLastPos = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            mViewPager.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                    startAutoScrolled();
                }

                @Override
                public void onViewDetachedFromWindow(View view) {
                    stopAutoScrolled();
                }
            });
        }
        void startAutoScrolled() {
            if (mAdapter != null) {
                mAdapter.startAutoScrolled();
            }
        }

        void stopAutoScrolled() {
            if (mAdapter != null) {
                mAdapter.stopAutoScrolled();
            }
        }

        void setHeaderVisible(int visible) {
            mHeaderView.setVisibility(visible);
            mGroup.setVisibility(visible);
        }
    }

}
