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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.app.util.ToolUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;


public class ShowPhotoListActivity extends Activity {

    private ArrayList<String> mPhotoList;
    private LinearLayout.LayoutParams mLps;
    private PopupWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_show_photo_list);
        Intent intent = getIntent();
        mPhotoList = intent.getStringArrayListExtra("photoList");
        int mPhotoPos = intent.getIntExtra("photoPos", 0);
        mLps = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mLps.gravity = Gravity.CENTER;

        ViewPager mViewPager = (ViewPager) findViewById(R.id.photo_viewPager);
        mViewPager.setAdapter(new MyViewPagerAdapter());
        mViewPager.setCurrentItem(mPhotoPos);
    }

    private void showPopupWindow(final int pos) {
        View view = View.inflate(ShowPhotoListActivity.this, R.layout.photo_popupwindow_layout, null);
        TextView shareView = (TextView) view.findViewById(R.id.share_photo);
        TextView saveView = (TextView) view.findViewById(R.id.save_photo);

        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImageTask imageTask = new SaveImageTask(ShowPhotoListActivity.this, 0);
                imageTask.execute(mPhotoList.get(pos));
            }
        });

        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImageTask imageTask = new SaveImageTask(ShowPhotoListActivity.this, 1);
                imageTask.execute(mPhotoList.get(pos));
            }
        });


        mPopupWindow = new PopupWindow(view,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        mPopupWindow.setTouchable(true);
        ColorDrawable cd = new ColorDrawable(Color.LTGRAY);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.showAtLocation(view, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void savePhoto(String imgPath, String imgUrl) {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        String imageName = ToolUtils.SHA256Encrypt(imgUrl);
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

        Toast toast = Toast.makeText(ShowPhotoListActivity.this, getString(R.string.file_save_tip) + newPath,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void sharePhoto(String imgPath, String imgUrl) {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        String imageName = ToolUtils.SHA256Encrypt(imgUrl);
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
        private String mImgUrl;
        private final int mFlag;

        public SaveImageTask(Context context, int flag) {
            this.context = context;
            mFlag = flag;
        }

        @Override
        protected File doInBackground(String... params) {
            mImgUrl =  params[0];
            try {
                return Glide.with(context)
                        .load(mImgUrl)
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
                sharePhoto(path, mImgUrl);
            } else {
                savePhoto(path, mImgUrl);
            }
        }
    }

    private class MyViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPhotoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            PhotoView photoView = new PhotoView(ShowPhotoListActivity.this);
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (!MainActivity.mIsLoadPhoto) {
                Glide.with(ShowPhotoListActivity.this).load(mPhotoList.get(position))
                        .error(R.drawable.photo_loaderror)
                        .placeholder(R.drawable.loading_photo)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(photoView);
            } else {
                if (MainActivity.mIsWIFIState) {
                    Glide.with(ShowPhotoListActivity.this).load(mPhotoList.get(position))
                            .error(R.drawable.photo_loaderror)
                            .placeholder(R.drawable.loading_photo)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(photoView);
                } else {
                    photoView.setImageResource(R.drawable.photo_default);
                }
            }
            container.addView(photoView, mLps);
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    showPopupWindow(position);
                    return false;
                }
            });
            return photoView;
        }
    }
}
