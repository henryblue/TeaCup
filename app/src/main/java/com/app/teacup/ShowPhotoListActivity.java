package com.app.teacup;

import android.app.Activity;
import android.os.Bundle;

import uk.co.senab.photoview.PhotoView;


public class ShowPhotoListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_show_photo);

        PhotoView showPhoto = (PhotoView) findViewById(R.id.iv_show_photo);
    }
}
