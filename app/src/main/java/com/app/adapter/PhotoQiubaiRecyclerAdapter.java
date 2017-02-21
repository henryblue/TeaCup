package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.PhotoInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class PhotoQiubaiRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<PhotoInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public PhotoQiubaiRecyclerAdapter(Context context, List<PhotoInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void startLoadImage(View view, String url) {
        if (url.contains(".gif")) {
            ImageView photoImg = (ImageView) view.findViewById(R.id.iv_photo);
            ImageView gifView = (ImageView) view.findViewById(R.id.iv_gif);
            if (!MainActivity.mIsLoadPhoto) {
                Glide.with(mContext).load(url)
                        .asGif()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.loading_photo)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(photoImg);
            } else {
                if (MainActivity.mIsWIFIState) {
                    Glide.with(mContext).load(url)
                            .asGif()
                            .error(R.drawable.photo_loaderror)
                            .placeholder(R.drawable.loading_photo)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(photoImg);
                } else {
                    photoImg.setImageResource(R.drawable.photo_default);
                }
            }
            gifView.setVisibility(View.GONE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PhotoViewHolder(mLayoutInflater.inflate(R.layout.item_photo_qiubai_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            onBindSingleItemViewHolder(holder, position);
    }

    private void onBindSingleItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        String url = mDatas.get(position).getImgUrl();
        if (url == null) {
            return;
        }

        final PhotoViewHolder myHolder = (PhotoViewHolder) holder;
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.photo_default)
                    .dontAnimate()
                    .into(myHolder.mPhotoImg);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.photo_default)
                        .dontAnimate()
                        .into(myHolder.mPhotoImg);
            } else {
                myHolder.mPhotoImg.setImageResource(R.drawable.photo_default);
            }
        }

        if (url.contains(".gif")) {
            myHolder.mGif.setVisibility(View.VISIBLE);
        } else {
            myHolder.mGif.setVisibility(View.GONE);
        }

        myHolder.mTitle.setText(mDatas.get(position).getTitle());

        if (mListener != null) {
            myHolder.mPhotoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myHolder.getLayoutPosition() - 1;
                    mListener.onItemClick(myHolder.itemView, pos);
                }
            });

            myHolder.mPhotoImg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = myHolder.getLayoutPosition() - 1;
                    mListener.onItemLongClick(myHolder.itemView, pos);
                    return false;
                }
            });
        }
    }

    public void reSetData(List<PhotoInfo> list) {
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

    private class PhotoViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private ImageView mPhotoImg;
        private ImageView mGif;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_photo);
            mGif = (ImageView) itemView.findViewById(R.id.iv_gif);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);
        }

    }
}
