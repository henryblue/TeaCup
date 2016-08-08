package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.teacup.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<String> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view,int position);
    }

    public PhotoRecyclerAdapter(Context context, List<String> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder(mLayoutInflater.inflate(R.layout.item_photo_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindItemViewHolder(holder, position);
    }

    public void reSetData(List<String> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    private void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        String url = mDatas.get(position);
        if (url == null) {
            return;
        }

        final PhotoViewHolder myHolder = (PhotoViewHolder) holder;
        Glide.with(mContext).load(url).asBitmap()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.photo_default)
                .dontAnimate()
                .into(myHolder.mPhotoImg);
        if (url.contains(".gif")) {
            myHolder.mGif.setVisibility(View.VISIBLE);
        } else {
            myHolder.mGif.setVisibility(View.GONE);
        }

        if (mListener != null) {
            myHolder.mPhotoImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myHolder.getLayoutPosition();
                    mListener.onItemClick(holder.itemView, pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class PhotoViewHolder extends RecyclerView.ViewHolder {

        private ImageView mPhotoImg;
        private ImageView mGif;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_photo);
            mGif = (ImageView) itemView.findViewById(R.id.iv_gif);
        }

    }
}
