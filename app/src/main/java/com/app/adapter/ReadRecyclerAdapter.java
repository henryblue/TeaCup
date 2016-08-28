package com.app.adapter;


import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.bean.Read.ReadCadInfo;
import com.app.bean.Read.ReadInfo;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class ReadRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TOPIC = 1;
    private static final int TYPE_NORMAL = 2;
    private static final String TAG = "ReadRecyclerAdapter";

    private Context mContext;
    private OnItemClickListener mListener;
    private List<ImageView> mImageViewList;
    private List<String> mImageViewUrls;
    private List<ReadInfo> mReadDatas;
    private List<ReadCadInfo> mCadInfos;
    private LayoutInflater mLayoutInflater;
    private HeaderViewHolder mHeaderViewHolder;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onTopicClick(int typePos, int position);
        void onLoadMore(int typePos);
    }

    public ReadRecyclerAdapter(Context context, List<ReadInfo> readInfos,
                               List<String> imageViewUrls, List<ReadCadInfo> cadInfos) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mReadDatas = readInfos;
        mImageViewUrls = imageViewUrls;
        mCadInfos = cadInfos;
        mImageViewList = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            if (mHeaderViewHolder == null) {
                mHeaderViewHolder = new HeaderViewHolder(mLayoutInflater.inflate(R.layout.item_news_header, parent, false));
            }
            return mHeaderViewHolder;
            } else if (viewType == TYPE_TOPIC) {
            return new ReadTopicViewHolder(mLayoutInflater.inflate(R.layout.item_read_topic_view, parent, false));
        } else {
            return new ReadViewHolder(mLayoutInflater.inflate(R.layout.item_read_view, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReadViewHolder) {
            onBindReadViewHolder(holder, position);
        } else if (holder instanceof HeaderViewHolder) {
            onBindHeaderItemViewHolder(holder, position);
        } else if (holder instanceof ReadTopicViewHolder) {
            onBindTopicItemViewHolder(holder, position);
        }
    }

    private void onBindHeaderItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        HeaderViewHolder myHolder = (HeaderViewHolder) holder;
        mImageViewList.clear();
        for (int i = 0; i < mImageViewUrls.size(); i++) {
            ImageView view = new ImageView(mContext);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(mContext).load(mImageViewUrls.get(i))
                    .asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(view);
            mImageViewList.add(view);
        }
        startHeaderAutoScrolled();
        setHeaderVisible(View.VISIBLE);

        if (myHolder.mAdapter != null) {
            myHolder.mAdapter.notifyDataSetChanged();
        }
    }

    private void onBindTopicItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ReadTopicViewHolder myHolder = (ReadTopicViewHolder) holder;
        ReadCadInfo info = mCadInfos.get(position - 1);
        myHolder.mTitle.setText(info.getCadTitle());
        myHolder.mTitles.setText(info.getCadContent());
        myHolder.mMores.setText(info.getMore());

        List<ReadInfo> readList = info.getReadList();
        for (int i = 0; i < myHolder.mViewHolder.size(); i++) {
            final ReadTopicViewHolder.ViewHolder viewHolder = myHolder.mViewHolder.get(i);
            Glide.with(mContext).load(readList.get(i).getImgurl()).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(viewHolder.mImage);

            viewHolder.mContent.setText(readList.get(i).getTitle());
            viewHolder.mTime.setText(readList.get(i).getAuthor());
            final int pos = i;
            if (mListener != null) {
                viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onTopicClick(position - 1, pos);
                    }
                });

                myHolder.mMores.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onLoadMore(position - 1);
                    }
                });
            }
        }
    }

    private void onBindReadViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ReadViewHolder myHolder = (ReadViewHolder) holder;
        if (position < 3) {
            return;
        }
        if (position - 3 > mReadDatas.size() - 1) {
            return;
        }
        ReadInfo info = mReadDatas.get(position - 3);

        Glide.with(mContext).load(info.getImgurl()).asBitmap()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.main_load_bg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(myHolder.mPhotoImg);
        if (TextUtils.isEmpty(info.getTitle())) {
            myHolder.mTitle.setText(mContext.getString(R.string.unknown_name));
        } else {
            myHolder.mTitle.setText(info.getTitle());
        }
        myHolder.mContent.setText(info.getRecommend());
        myHolder.mAuthor.setText(info.getAuthor());
        myHolder.mReadNum.setText(info.getReadNum());

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
        return position - 4;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == 1 || position == 2){
            return TYPE_TOPIC;
        } else if (position > 2) {
            return TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mReadDatas.size() + mCadInfos.size() + 1;
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
        if (mHeaderViewHolder != null) {
            mHeaderViewHolder.setGroupVisible(visible);
        }
    }

    /**
     * ReadViewHolder
     */
    private class ReadViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private ImageView mPhotoImg;
        private TextView mContent;
        private TextView mAuthor;
        private TextView mReadNum;
        private View mainView;

        public ReadViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_read_img);
            mTitle = (TextView) itemView.findViewById(R.id.tv_read_title);
            mContent = (TextView) itemView.findViewById(R.id.tv_read_content);
            mAuthor = (TextView) itemView.findViewById(R.id.tv_read_author);
            mReadNum = (TextView) itemView.findViewById(R.id.tv_read_num);
        }
    }

    /**
     * ReadTopicViewHolder
     */
    private class ReadTopicViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private TextView mTitles;
        private TextView mMores;

        private List<ViewHolder> mViewHolder;

        public ReadTopicViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.tv_topic_title);
            mTitles = (TextView) itemView.findViewById(R.id.tv_topic_title1);
            mMores = (TextView) itemView.findViewById(R.id.tv_topic_more);

            mViewHolder = new ArrayList<>();
            ViewHolder holder = new ViewHolder();
            holder.mImage = (ImageView) itemView.findViewById(R.id.iv_read_top_img1);
            holder.mContent = (TextView) itemView.findViewById(R.id.iv_read_topic_content1);
            holder.mTime = (TextView) itemView.findViewById(R.id.iv_read_topic_time1);
            mViewHolder.add(holder);

            ViewHolder holder1 = new ViewHolder();
            holder1.mImage = (ImageView) itemView.findViewById(R.id.iv_read_top_img2);
            holder1.mContent = (TextView) itemView.findViewById(R.id.iv_read_topic_content2);
            holder1.mTime = (TextView) itemView.findViewById(R.id.iv_read_topic_time2);
            mViewHolder.add(holder1);

            ViewHolder holder2 = new ViewHolder();
            holder2.mImage = (ImageView) itemView.findViewById(R.id.iv_read_top_img3);
            holder2.mContent = (TextView) itemView.findViewById(R.id.iv_read_topic_content3);
            holder2.mTime = (TextView) itemView.findViewById(R.id.iv_read_topic_time3);
            mViewHolder.add(holder2);

            ViewHolder holder3 = new ViewHolder();
            holder3.mImage = (ImageView) itemView.findViewById(R.id.iv_read_top_img4);
            holder3.mContent = (TextView) itemView.findViewById(R.id.iv_read_topic_content4);
            holder3.mTime = (TextView) itemView.findViewById(R.id.iv_read_topic_time4);
            mViewHolder.add(holder3);
        }

        private class ViewHolder {
            public ImageView mImage;
            public TextView mContent;
            public TextView mTime;
        }
    }

    /**
     * HeaderViewHolder
     */
    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private ReactViewPagerAdapter mAdapter;
        private ViewPager mViewPager;
        private LinearLayout mGroup;
        private int mLastPos = 0;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mViewPager = (ViewPager) itemView.findViewById(R.id.vp_news);
            mGroup = (LinearLayout) itemView.findViewById(R.id.ll_group);

            if (mImageViewUrls.size() <= 0) {
                return;
            }

            for (int i = 0; i < mImageViewUrls.size(); i++) {
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
                    (Integer.MAX_VALUE / 2 % mImageViewUrls.size()));

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    position = position % mImageViewUrls.size();
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
