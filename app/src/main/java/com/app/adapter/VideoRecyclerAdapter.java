package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.video.VideoInfo;
import com.app.teacup.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<VideoInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public VideoRecyclerAdapter(Context context, List<VideoInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MusicViewHolder(mLayoutInflater.inflate(R.layout.item_video_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            onBindSingleItemViewHolder(holder, position);
    }

    private void onBindSingleItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
    }

    public void reSetData(List<VideoInfo> list) {
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

    private class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView mAuthorImg;
        private TextView mAuthorName;
        private TextView mPublishTime;
        private TextView mVideoName;
        private TextView mVideoContent;
        private ImageView mVideoImg;
        private ImageView mVideoIndex;

        public MusicViewHolder(View itemView) {
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
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }
}
