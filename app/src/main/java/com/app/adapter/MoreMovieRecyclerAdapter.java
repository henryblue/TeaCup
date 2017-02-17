package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.movie.MovieItemInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class MoreMovieRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int mItemWidth;
    private Context mContext;
    private List<MovieItemInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public MoreMovieRecyclerAdapter(Context context, List<MovieItemInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int margin = mContext.getResources().getDimensionPixelOffset(R.dimen.item_movie_more_rl_marginRight);
        mItemWidth = (width - margin * 4) / 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MoreViewHolder(mLayoutInflater.inflate(R.layout.item_movie_more_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindItemViewHolder(holder, position);
    }

    public void reSetData(List<MovieItemInfo> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    private void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        MoreViewHolder viewHolder = (MoreViewHolder) holder;
        MovieItemInfo itemInfo = mDatas.get(position);
        viewHolder.mIndex.setText(itemInfo.getImageIndex());
        viewHolder.mNameView.setText(itemInfo.getMovieName());
        loadImageResource(itemInfo.getImageUrl(), viewHolder.mImageView);
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

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class MoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private final ImageView mImageView;
        private final TextView mIndex;
        private final TextView mNameView;

        public MoreViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.movie_more_imageView);
            mIndex = (TextView) itemView.findViewById(R.id.movie_more_tip);
            mNameView = (TextView) itemView.findViewById(R.id.movie_more_name);
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.width = mItemWidth;
            itemView.setLayoutParams(params);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition() - 1);
            }
        }
    }
}
