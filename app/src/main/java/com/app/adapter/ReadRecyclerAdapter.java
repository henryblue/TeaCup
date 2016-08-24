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

import com.app.bean.Read.ReadInfo;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class ReadRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TOPIC = 1;
    private static final int TYPE_NORMAL = 2;

    private Context mContext;
    private OnItemClickListener mListener;
    private List<ImageView> mImageViewList;
    private List<ReadInfo> mReadDatas;
    private LayoutInflater mLayoutInflater;
    private HeaderViewHolder mHeaderViewHolder;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, int type);
    }

    public ReadRecyclerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            if (mHeaderViewHolder == null) {
                mHeaderViewHolder = new HeaderViewHolder(mLayoutInflater.inflate(R.layout.item_news_header, parent, false));
            }
            return mHeaderViewHolder;
            } else if (viewType == TYPE_TOPIC) {
            return new ReadViewHolder(mLayoutInflater.inflate(R.layout.item_news_view, parent, false));
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

        }
    }

    private void onBindHeaderItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        HeaderViewHolder myHolder = (HeaderViewHolder) holder;
        myHolder.mAdapter.notifyDataSetChanged();
    }

    private void onBindReadViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ReadViewHolder myHolder = (ReadViewHolder) holder;
        ReadInfo info = mReadDatas.get(position - 3);

        Glide.with(mContext).load(info.getImgurl()).asBitmap()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.main_load_bg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(myHolder.mPhotoImg);
        myHolder.mTitle.setText(info.getTitle());
        myHolder.mContent.setText(info.getRecommend());
        myHolder.mAuthor.setText(String.format("%s %s", info.getAuthor(), info.getReadNum()));
        if (mListener != null) {
            myHolder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getRealPosition(myHolder);
                    mListener.onItemClick(myHolder.itemView, pos, TYPE_NORMAL);
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
        return 10;
    }


    private class ReadViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private ImageView mPhotoImg;
        private TextView mContent;
        private TextView mAuthor;
        private View mainView;

        public ReadViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_read_img);
            mTitle = (TextView) itemView.findViewById(R.id.tv_read_title);
            mContent = (TextView) itemView.findViewById(R.id.tv_read_content);
            mAuthor = (TextView) itemView.findViewById(R.id.tv_read_author);
        }
    }

    private class ReadTopicViewHolder extends RecyclerView.ViewHolder {

        public ReadTopicViewHolder(View itemView) {
            super(itemView);
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
    }

}
