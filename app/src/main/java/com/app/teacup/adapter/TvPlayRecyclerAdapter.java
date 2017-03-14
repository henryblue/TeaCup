package com.app.teacup.adapter;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.teacup.MovieTestPlayActivity;
import com.app.teacup.bean.movie.TvItemInfo;
import com.app.teacup.MoviePlayActivity;
import com.app.teacup.R;

import java.util.List;

public class TvPlayRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<TvItemInfo> mDatas;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public TvPlayRecyclerAdapter(Context context, List<TvItemInfo> datas) {
        mContext = context;
        mDatas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = new TextView(mContext);
        return new TvViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TvViewHolder viewHolder = (TvViewHolder) holder;
        viewHolder.mTextView.setTextColor(Color.BLACK);
        TvItemInfo info = mDatas.get(position);
        viewHolder.mTextView.setText(info.getName());
        int playIndex = 0;
        if (mContext instanceof MoviePlayActivity) {
            playIndex = ((MoviePlayActivity) mContext).mPlayIndex;
        } else if (mContext instanceof MovieTestPlayActivity) {
            playIndex = ((MovieTestPlayActivity) mContext).mPlayIndex;
        }
        if (position == playIndex) {
            viewHolder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.deepYellow));
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class TvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTextView;
        private final int margin = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.tv_series_textView_item_margin);
        private final int width = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.tv_series_textView_item_width);
        private final int maxWidth = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.tv_series_textView_item_max_width);
        private final int height = mContext.getResources()
                .getDimensionPixelOffset(R.dimen.tv_series_textView_item_height);
        private final int textSize = mContext.getResources().getDimensionPixelSize(R.dimen.tv_series_textView_item_textSize);

        public TvViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
            itemView.setOnClickListener(this);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(margin, margin, margin, margin);
            mTextView.setMinHeight(height);
            mTextView.setMinWidth(width);
            mTextView.setMaxWidth(maxWidth);
            mTextView.setLayoutParams(params);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setTextColor(Color.BLACK);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mTextView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.alpha_white));
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
}
