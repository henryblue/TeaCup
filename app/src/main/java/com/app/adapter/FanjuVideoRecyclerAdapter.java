package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.fanju.FanjuVideoInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class FanjuVideoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<FanjuVideoInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;
    private final int mScreenWidth;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public FanjuVideoRecyclerAdapter(Context context, List<FanjuVideoInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VideoViewHolder(mLayoutInflater.inflate(R.layout.item_fanju_video_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        ViewGroup.LayoutParams params = viewHolder.itemView.getLayoutParams();
        int margin = mContext.getResources().getDimensionPixelOffset(R.dimen.item_fanju_video_cv_margin);
        params.width = (mScreenWidth - 3 * margin) / 2;
        viewHolder.itemView.setLayoutParams(params);

        FanjuVideoInfo info = mDatas.get(position);
        viewHolder.mVideoName.setText(info.getVideoName());
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(info.getImgeUrl()).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(viewHolder.mVideoImg);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(info.getImgeUrl()).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(viewHolder.mVideoImg);
            } else {
                viewHolder.mVideoImg.setImageResource(R.drawable.main_load_bg);
            }
        }
    }

    public void reSetData(List<FanjuVideoInfo> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mVideoName;
        private ImageView mVideoImg;

        public VideoViewHolder(View itemView) {
            super(itemView);
            mVideoName = (TextView) itemView.findViewById(R.id.fanju_video_name);
            mVideoImg = (ImageView) itemView.findViewById(R.id.fanju_video_img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition() - 1);
            }
        }
    }
}
