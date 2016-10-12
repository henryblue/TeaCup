package com.app.teacup;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import uk.co.senab.photoview.PhotoView;


public class ShowPhotoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_show_photo);

        PhotoView showPhoto = (PhotoView) findViewById(R.id.iv_show_photo);

        String imgUrl = getIntent().getStringExtra("ImageUrl");
        if (imgUrl.contains(".gif")) {
            Glide.with(this).load(imgUrl)
                    .asGif()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.loading_photo)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(showPhoto);
        } else {
            if (!MainActivity.mIsLoadPhoto) {
                Glide.with(this).load(imgUrl).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.loading_photo)
                        .dontAnimate()
                        .into(showPhoto);
            } else {
                if (MainActivity.mIsWIFIState) {
                    Glide.with(this).load(imgUrl).asBitmap()
                            .error(R.drawable.photo_loaderror)
                            .placeholder(R.drawable.loading_photo)
                            .dontAnimate()
                            .into(showPhoto);
                } else {
                    showPhoto.setImageResource(R.drawable.photo_default);
                }
            }
        }

        showPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                
                return false;
            }
        });
    }
}
