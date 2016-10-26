package com.app.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.teacup.R;

public class MoreTextView extends LinearLayout implements View.OnClickListener {

    private static final int DEFAULT_MAX_LINE_COUNT = 9;

    private static final int COLLAPSIBLE_STATE_SHRINKUP = 1;
    private static final int COLLAPSIBLE_STATE_SPREAD = 2;

    private TextView mTextContent;
    private TextView mTextDesc;
    private String mShrinkup;
    private String mSpread;
    private int mState;
    private int mDefaultSize = 15;
    private boolean mIsChanged;

    public MoreTextView(Context context) {
        this(context, null);
    }

    public MoreTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoreTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
        setAttributeSet(context, attrs);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.stretch_view_layout, this);
        mTextContent = (TextView) findViewById(R.id.svl_text_content);
        mTextDesc = (TextView) findViewById(R.id.svl_text_desc);
        mTextDesc.setOnClickListener(this);
        mShrinkup = context.getResources().getString(R.string.desc_shrinkup);
        mSpread = context.getResources().getString(R.string.desc_spread);
        mState = COLLAPSIBLE_STATE_SPREAD;
        mIsChanged = false;
        mTextContent.setMaxLines(DEFAULT_MAX_LINE_COUNT + 1);
    }

    private void setAttributeSet(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray typeArray = context.obtainStyledAttributes(attrs,
                R.styleable.MoreTextView);
        final int texSize = typeArray.getDimensionPixelOffset(R.styleable.MoreTextView_textSize, mDefaultSize);
        setTextSize(texSize);

        int color = typeArray.getColor(R.styleable.MoreTextView_textColor, Color.BLACK);
        mTextContent.setTextColor(color);

        int size = typeArray.getDimensionPixelOffset(R.styleable.MoreTextView_desc_textSize, mDefaultSize);
        mTextDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

        color = typeArray.getColor(R.styleable.MoreTextView_desc_textColor, Color.BLACK);
        mTextDesc.setTextColor(color);
        typeArray.recycle();
    }

    public void setTextSize(int texSize) {
        mTextContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, texSize);
    }

    @Override
    public void onClick(View v) {
        mIsChanged = false;
        String desc = mTextDesc.getText().toString();
        if (desc.equals(mShrinkup)) {
            mState = COLLAPSIBLE_STATE_SPREAD;
        } else {
            mState = COLLAPSIBLE_STATE_SHRINKUP;
        }
        requestLayout();
    }

    public final void setContent(CharSequence charSequence) {
        mTextContent.setText(charSequence, TextView.BufferType.NORMAL);
        mState = COLLAPSIBLE_STATE_SPREAD;
        mIsChanged = false;
        requestLayout();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mTextContent.getLineCount() <= DEFAULT_MAX_LINE_COUNT) {
            mTextContent.setMaxLines(DEFAULT_MAX_LINE_COUNT + 1);
            mTextDesc.setVisibility(View.GONE);
        } else {
            post(new InnerRunnable());
        }
    }

    class InnerRunnable implements Runnable {
        @Override
        public void run() {
            if (!mIsChanged) {
                changedLayout();
            }
        }
    }
    private void changedLayout() {
        if (mState == COLLAPSIBLE_STATE_SPREAD) {
            mTextContent.setMaxLines(DEFAULT_MAX_LINE_COUNT);
            mTextDesc.setVisibility(View.VISIBLE);
            mTextDesc.setText(mSpread);
            mIsChanged = true;
        } else if (mState == COLLAPSIBLE_STATE_SHRINKUP) {
            mIsChanged = true;
            mTextContent.setMaxLines(Integer.MAX_VALUE);
            mTextDesc.setVisibility(View.VISIBLE);
            mTextDesc.setText(mShrinkup);
        }
    }
}
