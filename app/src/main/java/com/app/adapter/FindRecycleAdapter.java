package com.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.Utils;
import com.app.bean.FindBookInfo;
import com.app.teacup.R;

import java.util.ArrayList;


public class FindRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<FindBookInfo> mDatas;
    private OnItemClickListener mListener;
    private boolean animateItems = true;
    private int lastAnimatedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(View view,int position);
        void onItemLongClick(View view , int position);
    }

    public FindRecycleAdapter(Context context, ArrayList<FindBookInfo> datas) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDatas = datas;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void updateItems(ArrayList<FindBookInfo> books, boolean animated) {
        animateItems = animated;
        lastAnimatedPosition = -1;
        mDatas.addAll(books);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.item_find_fragment, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        runEnterAnimation(holder.itemView, position);
        FindBookInfo info = mDatas.get(position);
        if (info == null) {
            return;
        }

        final MyViewHolder myHolder = (MyViewHolder) holder;
        bindViewHolderItem(info, myHolder);

        if (mListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myHolder.getLayoutPosition();
                    mListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = myHolder.getLayoutPosition();
                    mListener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    private void runEnterAnimation(View view, int position) {
        if (!animateItems) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;

            view.setTranslationY(Utils.getScreenHeight(mContext));
            view.animate()
                    .translationY(0)
                    .setStartDelay(400)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(500)
                    .start();
        }
    }

    private void bindViewHolderItem(FindBookInfo info, MyViewHolder myHolder) {
        myHolder.mBookTitle.setText(info.getmBookTitle());
        myHolder.mBookContent.setText(info.getmBookContent());
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView mBookImg;
        private TextView mBookTitle;
        private TextView mBookContent;

        public MyViewHolder(View itemView) {
            super(itemView);
            mBookImg = (ImageView) itemView.findViewById(R.id.iv_book_img);
            mBookTitle = (TextView) itemView.findViewById(R.id.tv_book_title);
            mBookContent = (TextView) itemView.findViewById(R.id.tv_book_content);
        }

    }
}
