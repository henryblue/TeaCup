package com.app.teacup.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.app.teacup.bean.News.NewsInfo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NORMAL = 1;

    private View mHeaderView;
    private final Context mContext;
    private List<NewsInfo> mDatas;
    private OnItemClickListener mListener;
    private final LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public NewsRecyclerAdapter(Context context, List<NewsInfo> dataList) {
        mContext = context;
        mDatas = dataList;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER) {
            return new NewsViewHolder(mHeaderView);
        }
        return new NewsViewHolder(mLayoutInflater.inflate(R.layout.item_news_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER || position == 0) {
            return;
        }
        onBindItemViewHolder(holder, position);
    }

    private void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        NewsInfo info = mDatas.get(position - 1);
        String url = info.getImgUrl();

        final NewsViewHolder myHolder = (NewsViewHolder) holder;
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
        myHolder.mLabel.setText(info.getLabel());

        if (mListener != null) {
            myHolder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getRealPosition(myHolder);
                    mListener.onItemClick(myHolder.itemView, pos);
                }
            });
        }
    }

     private int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return position - 2;
    }

    public void reSetData(List<NewsInfo> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null) {
            return TYPE_NORMAL;
        }
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }


    private class NewsViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private ImageView mPhotoImg;
        private TextView mLabel;
        private View mainView;

        NewsViewHolder(View itemView) {
            super(itemView);
            if (itemView == mHeaderView) {
                return;
            }
            mainView = itemView;
            mPhotoImg = (ImageView) itemView.findViewById(R.id.iv_news_img);
            mTitle = (TextView) itemView.findViewById(R.id.tv_news_title);
            mLabel = (TextView) itemView.findViewById(R.id.tv_news_label);
        }
    }
}
