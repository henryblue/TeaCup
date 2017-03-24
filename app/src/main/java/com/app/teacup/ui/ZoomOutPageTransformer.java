package com.app.teacup.ui;

import android.view.View;

public class ZoomOutPageTransformer implements CenterViewPager.PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();
        if (position <= 1) { // [-1,1]
            float MIN_SCALE = 0.9f;
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            float verMargin = pageHeight * (1 - scaleFactor) / 2;
            float horMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horMargin - verMargin / 2);
            } else {
                view.setTranslationX(-horMargin + verMargin / 2);
            }
            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        }
    }
}