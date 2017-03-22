package com.app.teacup.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.teacup.bean.movie.MovieDetailInfo;
import com.app.teacup.bean.movie.MovieItemInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import static com.app.teacup.R.id.item_movie_detail_container1;

public class MovieDetailRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int ROW_NUM = 2;

    private final Context mContext;
    private List<MovieDetailInfo> mDataList;
    private OnItemClickListener mListener;
    private final LayoutInflater mLayoutInflater;
    private final int mItemWidth;
    private View mHeaderView;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NORMAL = 1;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, int itemPosition);
        void onMoreItemClick(View view, int position);
    }

    public MovieDetailRecyclerAdapter(Context context, List<MovieDetailInfo> dataList) {
        mContext = context;
        mDataList = dataList;
        mLayoutInflater = LayoutInflater.from(context);
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int margin = mContext.getResources().getDimensionPixelOffset(R.dimen.item_movie_detail_ll_marginRight);
        mItemWidth = (width - margin * 2) / 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER) {
            return new MovieDetailViewHolder(mHeaderView);
        }
        return new MovieDetailViewHolder(mLayoutInflater.inflate(R.layout.item_movie_detail_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER || position == 0) {
            return;
        }
        MovieDetailViewHolder viewHolder = (MovieDetailViewHolder) holder;
        viewHolder.mMovieTop.setVisibility(View.GONE);
        MovieDetailInfo info = mDataList.get((position - 1) / ROW_NUM);
        List<MovieItemInfo> infoList = info.getMovieInfoList();

        if ((position - 1) % ROW_NUM == 0) {
            viewHolder.mMovieTop.setVisibility(View.VISIBLE);
            String blockName = info.getMovieBlockName();
            viewHolder.mBlockTip.setText(blockName);
        }
        int i = (position - 1) % ROW_NUM * 3;
        for (int j = 0; j < 3; i++, j++) {
            MovieItemInfo itemInfo = infoList.get(i);
            ImageView imageView = viewHolder.mImgViewList.get(j);
            loadImageResource(itemInfo.getImageUrl(), imageView);
            TextView indexView = viewHolder.mIndexList.get(j);
            indexView.setText(itemInfo.getImageIndex());
            TextView nameView = viewHolder.mNameList.get(j);
            nameView.setText(itemInfo.getMovieName());
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

    public void resetData(List<MovieDetailInfo> listData) {
        mDataList = listData;
        notifyDataSetChanged();
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    @Override
    public int getItemCount() {
        if (mHeaderView == null) {
            return mDataList.size() * ROW_NUM;
        } else {
            return mDataList.size() * ROW_NUM + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) {
            return TYPE_NORMAL;
        }
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    private class MovieDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final List<ImageView> mImgViewList = new ArrayList<>();
        private final List<TextView> mIndexList = new ArrayList<>();
        private final List<TextView> mNameList = new ArrayList<>();
        private TextView mBlockTip;
        private RelativeLayout mMovieTop;

        MovieDetailViewHolder(View itemView) {
            super(itemView);
            if (itemView == mHeaderView) {
                return;
            }
            mBlockTip = (TextView) itemView.findViewById(R.id.movie_detail_block_tip);
            mMovieTop = (RelativeLayout) itemView.findViewById(R.id.movie_detail_top);

            ImageView mImageView1 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView1);
            TextView mIndex1 = (TextView) itemView.findViewById(R.id.movie_detail_tip1);
            TextView mNameView1 = (TextView) itemView.findViewById(R.id.movie_detail_name1);
            mImgViewList.add(mImageView1);
            mIndexList.add(mIndex1);
            mNameList.add(mNameView1);

            ImageView mImageView2 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView2);
            TextView mIndex2 = (TextView) itemView.findViewById(R.id.movie_detail_tip2);
            TextView mNameView2 = (TextView) itemView.findViewById(R.id.movie_detail_name2);
            mImgViewList.add(mImageView2);
            mIndexList.add(mIndex2);
            mNameList.add(mNameView2);

            ImageView mImageView3 = (ImageView) itemView.findViewById(R.id.movie_detail_imageView3);
            TextView mIndex3 = (TextView) itemView.findViewById(R.id.movie_detail_tip3);
            TextView mNameView3 = (TextView) itemView.findViewById(R.id.movie_detail_name3);
            mImgViewList.add(mImageView3);
            mIndexList.add(mIndex3);
            mNameList.add(mNameView3);

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

            mMovieTop.setOnClickListener(this);
            mContainer1.setOnClickListener(this);
            mContainer2.setOnClickListener(this);
            mContainer3.setOnClickListener(this);
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
                case R.id.movie_detail_top:
                    itemPos = -1;
                    break;
                default:
                    break;
            }
            int pos = getLayoutPosition() - 2;
            if (mListener != null && itemPos >= 0) {
                mListener.onItemClick(v, pos / ROW_NUM, pos % ROW_NUM * 3 + itemPos);
            } else if (mListener != null && itemPos == -1) {
                mListener.onMoreItemClick(v, pos / ROW_NUM);
            }
        }
    }
}
