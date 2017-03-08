package com.app.teacup.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.teacup.bean.PhotoInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.app.teacup.ui.MoreTextView;
import com.bumptech.glide.Glide;

import java.util.List;

public class PhotoGaoxiaoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<PhotoInfo> mDatas;
    private OnItemClickListener mListener;
    private final LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public PhotoGaoxiaoRecyclerAdapter(Context context, List<PhotoInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PhotoViewHolder(mLayoutInflater.inflate(R.layout.item_photo_gaoxiao_view, parent, false));
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

        myHolder.mTitle.setText(mDatas.get(position).getTitle());
        myHolder.mContent.setContent(mDatas.get(position).getContent());

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

        private final TextView mTitle;
        private final MoreTextView mContent;
        private final ImageView mPhotoImg;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_photo);
            mContent = (MoreTextView) itemView.findViewById(R.id.tv_content);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);
        }

    }
}
