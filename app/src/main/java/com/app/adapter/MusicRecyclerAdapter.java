package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.Music.MusicInfo;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class MusicRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<MusicInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public MusicRecyclerAdapter(Context context, List<MusicInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MusicViewHolder(mLayoutInflater.inflate(R.layout.item_music_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            onBindSingleItemViewHolder(holder, position);
    }

    private void onBindSingleItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        MusicInfo info = mDatas.get(position);
        String url = info.getImgUrl();
        final MusicViewHolder myHolder = (MusicViewHolder) holder;
        Glide.with(mContext).load(url).asBitmap()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.photo_default)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(myHolder.mPhotoImg);
        myHolder.mTitle.setText(info.getTitle());
        myHolder.mHappy.setText(info.getHappyNum());
        myHolder.mInfos.setText(info.getInfoNum());

        if (mListener != null) {
            myHolder.mPhotoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myHolder.getLayoutPosition() - 1;
                    mListener.onItemClick(myHolder.itemView, pos);
                }
            });
        }
    }

    public void reSetData(List<MusicInfo> list) {
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

    private class MusicViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private ImageView mPhotoImg;
        private TextView mHappy;
        private TextView mInfos;

        public MusicViewHolder(View itemView) {
            super(itemView);
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_music);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mHappy = (TextView) itemView.findViewById(R.id.tv_happy);
            mInfos = (TextView) itemView.findViewById(R.id.tv_info);
        }

    }
}
