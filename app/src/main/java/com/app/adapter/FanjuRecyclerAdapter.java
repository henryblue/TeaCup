package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.fanju.FanjuInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FanjuRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<FanjuInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public FanjuRecyclerAdapter(Context context, List<FanjuInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VideoViewHolder(mLayoutInflater.inflate(R.layout.item_fanju_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoViewHolder viewHolder = (VideoViewHolder) holder;
        FanjuInfo info = mDatas.get(position);
        viewHolder.mAuthorName.setText(info.getAuthorName());
        viewHolder.mPublishTime.setText(info.getPublishTime());
        viewHolder.mVideoContent.setText(info.getVideoContent());
        viewHolder.mVideoName.setText(info.getVideoName());
        viewHolder.mVideoIndex.setVisibility(View.GONE);
        if (!MainActivity.mIsLoadPhoto) {
            loadImageResource(info, viewHolder);
        } else {
            if (MainActivity.mIsWIFIState) {
                loadImageResource(info, viewHolder);
            } else {
                viewHolder.mAuthorImg.setImageResource(R.drawable.main_load_bg);
                viewHolder.mVideoImg.setImageResource(R.drawable.main_load_bg);
            }
        }
    }

    private void loadImageResource(FanjuInfo info, VideoViewHolder viewHolder) {
        Glide.with(mContext).load(info.getAuthorImgUrl()).asBitmap()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.main_load_bg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(viewHolder.mAuthorImg);
        Glide.with(mContext).load(info.getVideoImgUrl()).asBitmap()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.main_load_bg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(viewHolder.mVideoImg);
        Log.i("itemitem", "loadImageResource: ===============load====");
        if (!TextUtils.isEmpty(info.getVideoIndexUrl())) {
            Log.i("itemitem", "loadImageResource: ===================" + info.getVideoIndexUrl());
            viewHolder.mVideoIndex.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(info.getVideoIndexUrl()).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(viewHolder.mVideoIndex);
        }
    }

    public void reSetData(List<FanjuInfo> list) {
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

        private CircleImageView mAuthorImg;
        private TextView mAuthorName;
        private TextView mPublishTime;
        private TextView mVideoName;
        private TextView mVideoContent;
        private ImageView mVideoImg;
        private ImageView mVideoIndex;

        public VideoViewHolder(View itemView) {
            super(itemView);
            mAuthorImg = (CircleImageView) itemView.findViewById(R.id.video_author_img);
            mAuthorName = (TextView) itemView.findViewById(R.id.video_author_name);
            mPublishTime = (TextView) itemView.findViewById(R.id.video_publish_time);
            mVideoName = (TextView) itemView.findViewById(R.id.video_name);
            mVideoContent = (TextView) itemView.findViewById(R.id.video_content);
            mVideoImg = (ImageView) itemView.findViewById(R.id.video_img);
            mVideoIndex = (ImageView) itemView.findViewById(R.id.video_index);
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
