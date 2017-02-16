package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.bean.movie.MovieDetailInfo;
import com.app.bean.movie.MovieItemInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import static com.app.teacup.R.id.item_movie_detail_container1;

public class MovieDetailRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<MovieDetailInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;
    private int mItemWidth;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, int itemPosition);
        void onMoreItemClick(View view, int position);
    }

    public MovieDetailRecyclerAdapter(Context context, List<MovieDetailInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int margin = mContext.getResources().getDimensionPixelOffset(R.dimen.item_movie_detail_ll_marginRight);
        mItemWidth = (width - margin) / 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MovieDetailViewHolder(mLayoutInflater.inflate(R.layout.item_movie_detail_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MovieDetailViewHolder viewHolder = (MovieDetailViewHolder) holder;
        viewHolder.mImageViewTop.setVisibility(View.GONE);
        viewHolder.mMoreTipView.setVisibility(View.VISIBLE);
        MovieDetailInfo info = mDatas.get(position);
        List<MovieItemInfo> infoList = info.getMovieInfoList();
        if (position == 0) {
            viewHolder.mImageViewTop.setVisibility(View.VISIBLE);
            viewHolder.mMoreTipView.setVisibility(View.GONE);
            viewHolder.mImageViewTop.setImageResource(R.drawable.movie_top);
        }
        viewHolder.mBlockTip.setText(info.getMovieBlockName());
        int i = 0;
        for (MovieItemInfo itemInfo : infoList) {
            ImageView imageView = viewHolder.mImgViewList.get(i);
            loadImageResource(itemInfo.getImageUrl(), imageView);
            TextView indexView = viewHolder.mIndexList.get(i);
            indexView.setText(itemInfo.getImageIndex());
            TextView nameView = viewHolder.mNameList.get(i);
            nameView.setText(itemInfo.getMovieName());
            TextView timeView = viewHolder.mTimeList.get(i);
            timeView.setText(itemInfo.getAddTime());
            i++;
        }
    }

    private void loadImageResource(String url, ImageView imageView) {
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(imageView);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.main_load_bg);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void resetData(List<MovieDetailInfo> listDatas) {
        mDatas = listDatas;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class MovieDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private List<ImageView> mImgViewList = new ArrayList<>();
        private List<TextView> mIndexList = new ArrayList<>();
        private List<TextView> mNameList = new ArrayList<>();
        private List<TextView> mTimeList = new ArrayList<>();
        private TextView mBlockTip;
        private ImageView mImageViewTop;
        private TextView mMoreTipView;

        public MovieDetailViewHolder(View itemView) {
            super(itemView);
            mImageViewTop = (ImageView) itemView.findViewById(R.id.movie_detail_iv_top);
            mBlockTip = (TextView) itemView.findViewById(R.id.movie_detail_block_tip);
            mMoreTipView = (TextView) itemView.findViewById(R.id.movie_detail_more);

            ImageView mImageView1 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView1);
            TextView mIndex1 = (TextView) itemView.findViewById(R.id.movie_detail_tip1);
            TextView mNameView1 = (TextView) itemView.findViewById(R.id.movie_detail_name1);
            TextView mTimeView1 = (TextView) itemView.findViewById(R.id.movie_detail_addTime1);
            mImgViewList.add(mImageView1);
            mIndexList.add(mIndex1);
            mNameList.add(mNameView1);
            mTimeList.add(mTimeView1);

            ImageView mImageView2 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView2);
            TextView mIndex2 = (TextView) itemView.findViewById(R.id.movie_detail_tip2);
            TextView mNameView2 = (TextView) itemView.findViewById(R.id.movie_detail_name2);
            TextView mTimeView2 = (TextView) itemView.findViewById(R.id.movie_detail_addTime2);
            mImgViewList.add(mImageView2);
            mIndexList.add(mIndex2);
            mNameList.add(mNameView2);
            mTimeList.add(mTimeView2);

            ImageView mImageView3 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView3);
            TextView mIndex3 = (TextView) itemView.findViewById(R.id.movie_detail_tip3);
            TextView mNameView3 = (TextView) itemView.findViewById(R.id.movie_detail_name3);
            TextView mTimeView3 = (TextView) itemView.findViewById(R.id.movie_detail_addTime3);
            mImgViewList.add(mImageView3);
            mIndexList.add(mIndex3);
            mNameList.add(mNameView3);
            mTimeList.add(mTimeView3);

            ImageView mImageView4 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView4);
            TextView mIndex4 = (TextView) itemView.findViewById(R.id.movie_detail_tip4);
            TextView mNameView4 = (TextView) itemView.findViewById(R.id.movie_detail_name4);
            TextView mTimeView4 = (TextView) itemView.findViewById(R.id.movie_detail_addTime4);
            mImgViewList.add(mImageView4);
            mIndexList.add(mIndex4);
            mNameList.add(mNameView4);
            mTimeList.add(mTimeView4);

            ImageView mImageView5 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView5);
            TextView mIndex5 = (TextView) itemView.findViewById(R.id.movie_detail_tip5);
            TextView mNameView5 = (TextView) itemView.findViewById(R.id.movie_detail_name5);
            TextView mTimeView5 = (TextView) itemView.findViewById(R.id.movie_detail_addTime5);
            mImgViewList.add(mImageView5);
            mIndexList.add(mIndex5);
            mNameList.add(mNameView5);
            mTimeList.add(mTimeView5);

            ImageView mImageView6 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView6);
            TextView mIndex6 = (TextView) itemView.findViewById(R.id.movie_detail_tip6);
            TextView mNameView6 = (TextView) itemView.findViewById(R.id.movie_detail_name6);
            TextView mTimeView6 = (TextView) itemView.findViewById(R.id.movie_detail_addTime6);
            mImgViewList.add(mImageView6);
            mIndexList.add(mIndex6);
            mNameList.add(mNameView6);
            mTimeList.add(mTimeView6);

            RelativeLayout mContainer1 =  (RelativeLayout) itemView.findViewById(item_movie_detail_container1);
            ViewGroup.LayoutParams params = mContainer1.getLayoutParams();
            params.width = mItemWidth;
            mContainer1.setLayoutParams(params);

            RelativeLayout mContainer2 = (RelativeLayout) itemView.findViewById(R.id.item_movie_detail_container2);
            params = mContainer2.getLayoutParams();
            params.width = mItemWidth;
            mContainer2.setLayoutParams(params);

            RelativeLayout mContainer3 = (RelativeLayout) itemView.findViewById(R.id.item_movie_detail_container3);
            params = mContainer3.getLayoutParams();
            params.width = mItemWidth;
            mContainer3.setLayoutParams(params);

            RelativeLayout mContainer4 = (RelativeLayout) itemView.findViewById(R.id.item_movie_detail_container4);
            params = mContainer4.getLayoutParams();
            params.width = mItemWidth;
            mContainer4.setLayoutParams(params);

            RelativeLayout mContainer5 = (RelativeLayout) itemView.findViewById(R.id.item_movie_detail_container5);
            params = mContainer5.getLayoutParams();
            params.width = mItemWidth;
            mContainer5.setLayoutParams(params);

            RelativeLayout mContainer6 = (RelativeLayout) itemView.findViewById(R.id.item_movie_detail_container6);
            params = mContainer6.getLayoutParams();
            params.width = mItemWidth;
            mContainer6.setLayoutParams(params);

            RelativeLayout moreTop = (RelativeLayout) itemView.findViewById(R.id.movie_detail_top);

            moreTop.setOnClickListener(this);
            mContainer1.setOnClickListener(this);
            mContainer2.setOnClickListener(this);
            mContainer3.setOnClickListener(this);
            mContainer4.setOnClickListener(this);
            mContainer5.setOnClickListener(this);
            mContainer6.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int itemPos = -2;
            switch (v.getId()) {
                case R.id.item_movie_detail_container1:
                    itemPos = 0;
                    break;
                case R.id.item_movie_detail_container2:
                    itemPos = 1;
                    break;
                case R.id.item_movie_detail_container3:
                    itemPos = 2;
                    break;
                case R.id.item_movie_detail_container4:
                    itemPos = 3;
                    break;
                case R.id.item_movie_detail_container5:
                    itemPos = 4;
                    break;
                case R.id.item_movie_detail_container6:
                    itemPos = 5;
                    break;
                case R.id.movie_detail_top:
                    itemPos = -1;
                    break;
                default:
                    break;
            }
            if (mListener != null && itemPos >= 0) {
                mListener.onItemClick(v, getLayoutPosition() - 1, itemPos);
            } else if (mListener != null && itemPos == -1) {
                mListener.onMoreItemClick(v, getLayoutPosition() - 1);
            }
        }
    }
}
