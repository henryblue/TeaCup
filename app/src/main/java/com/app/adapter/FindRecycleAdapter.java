package com.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.bean.book.FindBookInfo;
import com.app.teacup.R;
import com.app.util.ToolUtils;
import com.bumptech.glide.Glide;

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

    public void reSetData(ArrayList<FindBookInfo> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return onCreateItemViewHolder(parent, viewType);
    }

    private RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.item_find_fragment, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
                onBindItemViewHolder(holder, position);
    }

    private void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
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
                    int pos = myHolder.getLayoutPosition() - 1;
                    mListener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = myHolder.getLayoutPosition() - 1;
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
            view.setTranslationY(ToolUtils.getScreenHeight(mContext));
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
        Glide.with(mContext).load(info.getmImgUrl())
                .centerCrop()
                .error(R.drawable.photo_loaderror)
                .placeholder(R.drawable.main_load_bg)
                .crossFade()
                .into(myHolder.mBookImg);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
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
