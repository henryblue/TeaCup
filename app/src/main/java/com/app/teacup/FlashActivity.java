package com.app.teacup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.app.bean.UpdateInfo;
import com.app.util.HttpUtils;
import com.app.util.JsonUtils;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;

import java.io.File;

import me.drakeet.materialdialog.MaterialDialog;


public class FlashActivity extends Activity {

    private static final int UPDATE_APP = 0;
    private static final int ENTER_HOME = 1;
    private UpdateInfo mUpdateInfo;

    private Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_APP:
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    enterMainPage();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private TextView mUpdateText;

    private void showUpdateDialog() {
        final MaterialDialog mMaterialDialog = new MaterialDialog(this);
        mMaterialDialog.setTitle(getString(R.string.update_dialog))
                .setMessage(mUpdateInfo.getDec())
                .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downLoadUpdateApk();
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMaterialDialog.dismiss();
                        sendMessage(ENTER_HOME);
                    }
                });
        mMaterialDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_flash_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUpdate();
    }

    private void downLoadUpdateApk() {
        if (Environment.getExternalStorageDirectory().equals(Environment.MEDIA_MOUNTED)) {
            MaterialDialog materialDialog = new MaterialDialog(this);
            materialDialog.setTitle("软件版本跟新");
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.progressbar_item,
                            null);
            mUpdateText = (TextView) view.findViewById(R.id.update_text);
            materialDialog.setCanceledOnTouchOutside(true);
            materialDialog.setView(view).show();

            FinalHttp finalHttp = new FinalHttp();
            finalHttp.download(mUpdateInfo.getUrl(),
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mUpdateInfo.getApkName(),
                    new AjaxCallBack<File>() {
                        @Override
                        public void onLoading(long count, long current) {
                            super.onLoading(count, current);
                            int progress = (int) (current * 100 / count);
                            mUpdateText.setText(String.format("%d%", progress));
                        }

                        @Override
                        public void onSuccess(File file) {
                            super.onSuccess(file);
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            super.onFailure(t, errorNo, strMsg);
                        }
                    });
        }
    }

    private void checkUpdate() {
        new Thread() {
            @Override
            public void run() {
                HttpUtils.sendHttpRequest(getString(R.string.serverUrl), new HttpUtils.HttpCallBackListener() {
                    @Override
                    public void onFinish(String response) {
                        try {
                            mUpdateInfo = JsonUtils.parseUpdateJsonData(response);
                        } catch (JSONException e) {
                            sendMessage(ENTER_HOME);
                        }

                        if (getVersionName().equals(mUpdateInfo.getVersion())) {
                            sendMessage(ENTER_HOME);
                        } else {
                            sendMessage(UPDATE_APP);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        sendMessage(ENTER_HOME);
                    }
                });

            }
        }.start();
    }

    private void sendMessage(int what) {
        if (mHander != null) {
            Message msg = Message.obtain();
            msg.what = what;
            mHander.sendMessage(msg);
        }
    }

    private String getVersionName() {
        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi.versionName;
    }

    private void enterMainPage() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        boolean isFirst = sp.getBoolean("com.app.teacup.GuideActivity", true);
        Intent intent = new Intent();
        if (isFirst) {
            intent.setClass(FlashActivity.this, GuideActivity.class);
            SharedPreferences.Editor edit = sp.edit();
            edit.putBoolean("com.app.teacup.GuideActivity", false);
            edit.apply();
        } else {
            intent.setClass(FlashActivity.this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
