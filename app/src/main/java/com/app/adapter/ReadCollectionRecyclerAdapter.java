package com.app.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.Read.ReadCollectInfo;
import com.app.teacup.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class ReadCollectionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ReadCollectInfo> mDatas;
    private OnItemClickListener mListener;
    private LayoutInflater mLayoutInflater;
    private int mType;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public ReadCollectionRecyclerAdapter(Context context, List<ReadCollectInfo> datas, int type) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
        mType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (0 == mType) {
            return new ReadCollectionViewHolder(mLayoutInflater.inflate(R.layout.item_read_collection, parent, false));
        } else {
            return new ListReadCollectionViewHolder(mLayoutInflater.inflate(R.layout.list_item_read_collection, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ReadCollectionViewHolder) {
            onBindSingleItemViewHolder(holder, position);
        } else if (holder instanceof ListReadCollectionViewHolder) {
            onBindListItemViewHolder(holder, position);
        }
    }

    private void onBindListItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ReadCollectInfo info = mDatas.get(position);
        String url = info.getImgUrl();
        final ListReadCollectionViewHolder myHolder = (ListReadCollectionViewHolder) holder;
        if (!TextUtils.isEmpty(url)) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(myHolder.mPhotoImg);
        }

        myHolder.mContent.setText(info.getText());
        myHolder.mDetail.setText(info.getDetail());
        myHolder.mTitle.setText(info.getTitle());
        myHolder.mAuthor.setText(info.getCome());
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
        ReadCollectInfo info = mDatas.get(position);
        String url = info.getImgUrl();
        final ReadCollectionViewHolder myHolder = (ReadCollectionViewHolder) holder;
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

        myHolder.mCome.setText(info.getCome());
        myHolder.mContent.setText(info.getText());
        myHolder.mDetail.setText(info.getDetail());
        myHolder.mTitle.setText(info.getTitle());
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

    public void reSetData(List<ReadCollectInfo> list) {
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

    private class ReadCollectionViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private TextView mCome;
        private TextView mDetail;
        private ImageView mPhotoImg;
        private TextView mContent;
        private View mView;

        public ReadCollectionViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.item_collect_img);
            mTitle = (TextView) itemView.findViewById(R.id.item_collect_title);
            mCome = (TextView) itemView.findViewById(R.id.item_collect_come);
            mDetail = (TextView) itemView.findViewById(R.id.item_collect_detail);
            mContent = (TextView) itemView.findViewById(R.id.item_collect_content);
        }

    }

    private class ListReadCollectionViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private TextView mAuthor;
        private TextView mDetail;
        private ImageView mPhotoImg;
        private TextView mContent;
        private View mView;

        public ListReadCollectionViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.list_item_collect_img);
            mTitle = (TextView) itemView.findViewById(R.id.list_item_collect_title);
            mAuthor = (TextView) itemView.findViewById(R.id.list_item_collect_author);
            mDetail = (TextView) itemView.findViewById(R.id.list_item_collect_detail);
            mContent = (TextView) itemView.findViewById(R.id.list_item_collect_content);
        }

    }
}
