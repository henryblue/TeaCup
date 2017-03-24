package com.app.teacup.ui;


import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.lang.reflect.Field;

public class ReactViewPager extends CenterViewPager {

    public ReactViewPager(Context context) {
        this(context, null);
    }

    public ReactViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Field mFirstLayout = ViewPager.class.getDeclaredField("mFirstLayout");
            mFirstLayout.setAccessible(true);
            mFirstLayout.set(this, false);
            if (getAdapter() != null) {
                getAdapter().notifyDataSetChanged();
            }
            setCurrentItem(getCurrentItem(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (((Activity) getContext()).isFinishing()) {
            super.onDetachedFromWindow();
        }
    }
}
