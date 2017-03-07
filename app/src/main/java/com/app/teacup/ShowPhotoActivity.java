package com.app.teacup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.app.util.ToolUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import uk.co.senab.photoview.PhotoView;


public class ShowPhotoActivity extends Activity {

    private String mImgUrl;
    private PopupWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_show_photo);

        PhotoView showPhoto = (PhotoView) findViewById(R.id.iv_show_photo);

        mImgUrl = getIntent().getStringExtra("ImageUrl");
        if (mImgUrl.contains(".gif")) {
            Glide.with(this).load(mImgUrl)
                    .asGif()
                    .error(R.drawable.photo_loaderror)
                    .placeholder(R.drawable.loading_photo)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(showPhoto);
        } else {
            if (!MainActivity.mIsLoadPhoto) {
                Glide.with(this).load(mImgUrl).asBitmap()
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.loading_photo)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(showPhoto);
            } else {
                if (MainActivity.mIsWIFIState) {
                    Glide.with(this).load(mImgUrl).asBitmap()
                            .error(R.drawable.photo_loaderror)
                            .placeholder(R.drawable.loading_photo)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
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
                showPopupWindow();
                return false;
            }
        });
    }

    private void showPopupWindow() {
        View view = View.inflate(ShowPhotoActivity.this, R.layout.photo_popupwindow_layout, null);
        TextView shareView = (TextView) view.findViewById(R.id.share_photo);
        TextView saveView = (TextView) view.findViewById(R.id.save_photo);

        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImageTask imageTask = new SaveImageTask(ShowPhotoActivity.this, 0);
                imageTask.execute(mImgUrl);
            }
        });

        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImageTask imageTask = new SaveImageTask(ShowPhotoActivity.this, 1);
                imageTask.execute(mImgUrl);
            }
        });


        mPopupWindow = new PopupWindow(view,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        mPopupWindow.setTouchable(true);
        ColorDrawable cd = new ColorDrawable(Color.LTGRAY);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.showAtLocation(view, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void savePhoto(String imgPath) {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        String imageName = ToolUtils.SHA256Encrypt(mImgUrl);
        String newPath = Environment.getExternalStorageDirectory() + "/tea_img/" + imageName + ".jpg";
        File file = new File(newPath);
        if (!file.exists()) {
            File tmpFile = new File(Environment.getExternalStorageDirectory(), "tea_img");
            if (!tmpFile.exists()) {
                tmpFile.mkdir();
            }
            ToolUtils.copyFile(imgPath, newPath);

            //发送更新广播, 让图片显示在相册中
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            sendBroadcast(intent);
        }

        Toast toast = Toast.makeText(ShowPhotoActivity.this, getString(R.string.file_save_tip) + newPath,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void sharePhoto(String imgPath) {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        String imageName = ToolUtils.SHA256Encrypt(mImgUrl);
        String newPath = Environment.getExternalStorageDirectory() + "/tea_img/" + imageName + ".jpg";
        File tmpFile = new File(Environment.getExternalStorageDirectory(), "tea_img");
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }

        ToolUtils.copyFile(imgPath, newPath);

        File file = new File(newPath);
        ToolUtils.changeFilePermission(file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_header)));
    }

    private class SaveImageTask extends AsyncTask<String, Void, File> {
        private final Context context;
        private final int mFlag;

        public SaveImageTask(Context context, int flag) {
            this.context = context;
            mFlag = flag;
        }

        @Override
        protected File doInBackground(String... params) {
            String imgUrl =  params[0];
            try {
                return Glide.with(context)
                        .load(imgUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                return;
            }
            String path = result.getPath();
            if (mFlag == 0) {
                sharePhoto(path);
            } else {
                savePhoto(path);
            }
        }
    }
}
