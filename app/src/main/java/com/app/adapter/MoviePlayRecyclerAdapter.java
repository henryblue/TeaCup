package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.movie.MoviePlayInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class MoviePlayRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<MoviePlayInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public MoviePlayRecyclerAdapter(Context context, List<MoviePlayInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MovieViewHolder(mLayoutInflater.inflate(R.layout.item_movie_play_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MovieViewHolder viewHolder = (MovieViewHolder) holder;
        MoviePlayInfo info = mDatas.get(position);
        viewHolder.mTimeView.setText(info.getAddTime());
        viewHolder.mNameView.setText(info.getMovieName());
        loadImageResource(info.getImgUrl(), viewHolder.mImageView);
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


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mImageView;
        private TextView mNameView;
        private TextView mTimeView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.moive_play_img);
            mNameView = (TextView) itemView.findViewById(R.id.movie_play_name);
            mTimeView = (TextView) itemView.findViewById(R.id.movie_play_addTime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
}
