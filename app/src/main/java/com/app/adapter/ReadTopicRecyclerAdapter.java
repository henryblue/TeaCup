package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.Read.ReadTopicInfo;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class ReadTopicRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ReadTopicInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;
    private int mType;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public ReadTopicRecyclerAdapter(Context context, List<ReadTopicInfo> datas, int type) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        mType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mType == 0) {
            return new ReadTopicViewHolder(mLayoutInflater.inflate(R.layout.item_read_topic, parent, false));
        } else {
            return new ListReadTopicViewHolder(mLayoutInflater.inflate(R.layout.list_item_read_topic, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReadTopicViewHolder) {
            onBindSingleItemViewHolder(holder, position);
        } else if (holder instanceof ListReadTopicViewHolder) {
            onBindListItemViewHolder(holder, position);
        }
    }

    private void onBindListItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ReadTopicInfo info = mDatas.get(position);
        String url = info.getImgUrl();
        final ListReadTopicViewHolder myHolder = (ListReadTopicViewHolder) holder;
        myHolder.mPhotoImg.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(url)) {
            myHolder.mPhotoImg.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(myHolder.mPhotoImg);
        }
        myHolder.mTitle.setText(info.getTitle());
        myHolder.mAuthor.setText(info.getDetail());

        if (mListener != null) {
            myHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myHolder.getLayoutPosition() - 1;
                    mListener.onItemClick(myHolder.itemView, pos);
                }
            });
        }
    }

    private void onBindSingleItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ReadTopicInfo info = mDatas.get(position);
        String url = info.getImgUrl();
        final ReadTopicViewHolder myHolder = (ReadTopicViewHolder) holder;
        myHolder.mPhotoImg.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(url)) {
            myHolder.mPhotoImg.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(myHolder.mPhotoImg);
        }
        myHolder.mTitle.setText(info.getTitle());
        myHolder.mAuthor.setText(info.getDetail());
        myHolder.mContent.setText(info.getContent());

        if (mListener != null) {
            myHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myHolder.getLayoutPosition() - 1;
                    mListener.onItemClick(myHolder.itemView, pos);
                }
            });
        }
    }

    public void reSetData(List<ReadTopicInfo> list) {
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

    private class ReadTopicViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private TextView mAuthor;
        private ImageView mPhotoImg;
        private TextView mContent;
        private View mView;

        public ReadTopicViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.item_read_img);
            mTitle = (TextView) itemView.findViewById(R.id.item_read_title);
            mAuthor = (TextView) itemView.findViewById(R.id.item_read_author);
            mContent = (TextView) itemView.findViewById(R.id.item_read_content);
        }

    }

    private class ListReadTopicViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private TextView mAuthor;
        private ImageView mPhotoImg;
        private View mView;

        public ListReadTopicViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.list_read_img);
            mTitle = (TextView) itemView.findViewById(R.id.list_read_title);
            mAuthor = (TextView) itemView.findViewById(R.id.list_read_detail);
        }

    }
}
