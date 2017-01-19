package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.bean.Music.MusicInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import hb.xvideoplayer.MxVideoPlayerWidget;

public class MusicRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<MusicInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;
    private int mType;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public MusicRecyclerAdapter(Context context, List<MusicInfo> datas, int type) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        mType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (0 == mType) {
            return new MusicViewHolder(mLayoutInflater.inflate(R.layout.item_music_view, parent, false));
        } else {
            return new TingViewHolder(mLayoutInflater.inflate(R.layout.item_ting_view, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MusicViewHolder) {
            onBindSingleItemViewHolder(holder, position);
        } else if (holder instanceof TingViewHolder) {
            onBindTingItemViewHolder(holder, position);
        }
    }

    private void onBindTingItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        MusicInfo info = mDatas.get(position);
        String url = info.getImgUrl();
        final TingViewHolder myHolder = (TingViewHolder) holder;
        myHolder.mLayout.setVisibility(View.GONE);
        myHolder.mMxVideoPlayer.setVisibility(View.GONE);
        myHolder.mMxVideoPlayer.release();
        String strType = info.getHappyNum();
        if (strType.startsWith("http://")) {
            myHolder.mMxVideoPlayer.setVisibility(View.VISIBLE);
            myHolder.mMxVideoPlayer.startPlay(strType
                    , MxVideoPlayerWidget.SCREEN_LAYOUT_LIST, info.getTitle());
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(myHolder.mMxVideoPlayer.mThumbImageView);
            return;
        } else {
            myHolder.mLayout.setVisibility(View.VISIBLE);
        }
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(myHolder.mPhotoImg);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(myHolder.mPhotoImg);
            } else {
                myHolder.mPhotoImg.setImageResource(R.drawable.main_load_bg);
            }
        }

        myHolder.mTitle.setText(info.getTitle());
        myHolder.mDetail.setText(info.getInfoNum());
        myHolder.mContent.setText(strType);

        if (mListener != null) {
            myHolder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myHolder.getLayoutPosition() - 1;
                    mListener.onItemClick(myHolder.itemView, pos);
                }
            });
        }
    }

    private void onBindSingleItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        MusicInfo info = mDatas.get(position);
        String url = info.getImgUrl();
        final MusicViewHolder myHolder = (MusicViewHolder) holder;
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(myHolder.mPhotoImg);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(myHolder.mPhotoImg);
            } else {
                myHolder.mPhotoImg.setImageResource(R.drawable.main_load_bg);
            }
        }
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

    private class TingViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLayout;
        private TextView mTitle;
        private ImageView mPhotoImg;
        private TextView mContent;
        private TextView mDetail;
        private MxVideoPlayerWidget mMxVideoPlayer;

        public TingViewHolder(View itemView) {
            super(itemView);
            mLayout = (LinearLayout) itemView.findViewById(R.id.ll_radio_layout);
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_ting_img);
            mTitle = (TextView) itemView.findViewById(R.id.tv_ting_title);
            mContent = (TextView) itemView.findViewById(R.id.tv_ting_content);
            mDetail = (TextView) itemView.findViewById(R.id.tv_ting_detail);
            mMxVideoPlayer = (MxVideoPlayerWidget) itemView.findViewById(R.id.mx_video_player);
        }

    }
}
