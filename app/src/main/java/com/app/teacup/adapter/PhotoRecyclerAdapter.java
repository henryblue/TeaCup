package com.app.teacup.adapter;


import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.bumptech.glide.Glide;

import java.util.List;


public class PhotoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<String> mDatas;
    private OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public PhotoRecyclerAdapter(Context context, List<String> datas) {
        mContext = context;
        mDatas = datas;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder(View.inflate(mContext, R.layout.item_photo_view, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindItemViewHolder(holder, position);
    }

    public void reSetData(List<String> list) {
        int start = mDatas.size();
        int end = list.size();
        mDatas = list;
        notifyItemRangeInserted(start, end);
    }

    public void refreshData(List<String> list) {
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
        loadImage(url, myHolder.mPhotoImg);

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

    private void loadImage(String url, ImageView imgView) {
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.photo_default)
                    .dontAnimate()
                    .into(imgView);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.photo_default)
                        .dontAnimate()
                        .into(imgView);
            } else {
                imgView.setImageResource(R.drawable.photo_default);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class PhotoViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mPhotoImg;

        PhotoViewHolder(View itemView) {
            super(itemView);
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_photo);
        }

    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            int itemPosition = parent.getChildAdapterPosition(view);
            if (itemPosition % 2 != 0) {
                outRect.right = space;
            }
            outRect.bottom = space;
        }
    }
}
