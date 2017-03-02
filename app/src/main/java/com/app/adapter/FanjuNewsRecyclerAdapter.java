package com.app.adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.bean.fanju.FanjuNewInfo;
import com.app.teacup.MainActivity;
import com.app.teacup.R;
import com.app.teacup.ShowPhotoActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FanjuNewsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<FanjuNewInfo> mDatas;
    private OnItemClickListener mListener;
    private final LayoutInflater mLayoutInflater;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public FanjuNewsRecyclerAdapter(Context context, List<FanjuNewInfo> datas) {
        mContext = context;
        mDatas = datas;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NewsViewHolder(mLayoutInflater.inflate(R.layout.item_fanju_news_view, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NewsViewHolder viewHolder = (NewsViewHolder) holder;
        FanjuNewInfo info = mDatas.get(position);
        viewHolder.mTimeView.setText(info.getPublishTime());
        viewHolder.mUserName.setText(info.getUserName());
        loadImageResource(info.getUserImgUrl(), viewHolder.mUserImageView);
        loadUserCommit(viewHolder.mContainer, info.getDatas());
    }

    private void loadUserCommit(LinearLayout container, List<String> datas) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.item_fanju_video_cv_margin);
        params.setMargins(margin, margin, margin, margin);
        int textSize = mContext.getResources()
                .getDimensionPixelSize(R.dimen.item_fanju_news_item_textSize);
        for (String data : datas) {
            if (data.contains("jpg")) {
                //image url
                ImageView imageView = new ImageView(mContext);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                loadImageResource(data, imageView);
                final String finalTag = data;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ShowPhotoActivity.class);
                        intent.putExtra("ImageUrl", finalTag);
                        mContext.startActivity(intent);
                    }
                });
                container.addView(imageView);
            } else {
                // text
                TextView textView = new TextView(mContext);
                textView.setLayoutParams(params);
                textView.setText(data);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                textView.setLineSpacing(0, 1.45f);
                if (data.contains("detail/1?channel=share")) {
                    textView.setTextIsSelectable(true);
                }
                container.addView(textView);
            }
        }
    }

    private void loadImageResource(String url, ImageView imageView) {
        if (!MainActivity.mIsLoadPhoto) {
            Glide.with(mContext).load(url).asBitmap()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.main_load_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(imageView);
        } else {
            if (MainActivity.mIsWIFIState) {
                Glide.with(mContext).load(url).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.main_load_bg)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.main_load_bg);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final CircleImageView mUserImageView;
        private final TextView mUserName;
        private final TextView mTimeView;
        private final LinearLayout mContainer;

        public NewsViewHolder(View itemView) {
            super(itemView);
            mUserImageView = (CircleImageView) itemView.findViewById(R.id.fanju_news_author_img);
            mUserName = (TextView) itemView.findViewById(R.id.fanju_news_author_name);
            mTimeView = (TextView) itemView.findViewById(R.id.fanju_news_publish_time);
            mContainer = (LinearLayout) itemView.findViewById(R.id.fanju_news_container);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition() - 1);
            }
        }
    }
}
