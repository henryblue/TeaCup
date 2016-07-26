package com.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.bean.FindBookInfo;
import com.app.teacup.R;
import com.app.util.ToolUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class FindRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LoadStatus mLoadStatus = LoadStatus.LOAD_OK;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<FindBookInfo> mDatas;
    private OnItemClickListener mListener;
    private boolean animateItems = true;
    private int lastAnimatedPosition = -1;

    private static final int VIEW_TYPE_FOOTER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    public interface OnItemClickListener {
        void onItemClick(View view,int position);
        void onItemLongClick(View view , int position);
    }

    public enum LoadStatus {
        LOAD_OK,
        LOADING_MORE
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

    public void setLoadStatus(LoadStatus loadStatus) {
        mLoadStatus = loadStatus;
    }

    public void reSetData(ArrayList<FindBookInfo> list) {
        mDatas = list;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_FOOTER) {
            return onCreateFooterViewHolder(parent, viewType);
        } else if(viewType == VIEW_TYPE_ITEM) {
            return onCreateItemViewHolder(parent, viewType);
        }
        return null;

    }

    private RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.item_find_fragment, parent, false));
    }

    private RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return new FooterViewHolder(mLayoutInflater.inflate(R.layout.layout_foot_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case VIEW_TYPE_ITEM:
                onBindItemViewHolder(holder, position);
                break;
            case VIEW_TYPE_FOOTER:
                onBindFooterViewHolder(holder, position);
                break;
            default:
                break;
        }
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

    private void onBindFooterViewHolder(RecyclerView.ViewHolder holder, int position) {
        FooterViewHolder viewHolder = (FooterViewHolder) holder;

        switch (mLoadStatus) {
            case LOAD_OK:
                viewHolder.mLoadingLayout.setVisibility(View.GONE);
                break;
            case LOADING_MORE:
                viewHolder.mLoadingLayout.setVisibility(View.VISIBLE);
                break;
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
        Picasso.with(mContext).load(info.getmImgUrl()).into(myHolder.mBookImg);
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position + 1 == getItemCount()) { //the last data to show FooterView
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_ITEM;
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

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mLoadingLayout;

        public FooterViewHolder(View itemView) {
            super(itemView);
            mLoadingLayout = (RelativeLayout) itemView.findViewById(R.id.load_layout);
        }
    }
}
