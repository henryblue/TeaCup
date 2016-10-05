package com.app.teacup;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.bean.WeatherInfo;
import com.app.util.OkHttpUtils;
import com.app.util.ToolUtils;
import com.app.util.urlUtils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.squareup.okhttp.Request;

import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class WeatherActivity extends BaseActivity {

    private String mFlieName = "WeatherCacheInfo.json";
    private ImageView mWeatherIcon;
    private TextView mWeatherLocal;
    private TextView mWeatherTemp;
    private TextView mWeatherWind;
    private TextView mWeatherContent;
    private LinearLayout mLayoutWeatherStatus;
    private Toolbar mToolbar;
    private String mCurrCity;
    private LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyBdlocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        initLocation();
        mLocationClient.start();
        initView();
        initToolBar();
    }

    @Override
    protected void onLoadDataError() {
        Toast.makeText(WeatherActivity.this,
                getString(R.string.location_error), Toast.LENGTH_SHORT).show();
        startLoadData();
    }

    @Override
    protected void onLoadDataFinish() {
        startLoadData();
    }

    @Override
    protected void onRefreshError() {

    }

    @Override
    protected void onRefreshFinish() {

    }

    @Override
    protected void onRefreshStart() {

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.setLocOption(option);
    }

    private void startLoadData() {
        if (TextUtils.isEmpty(mCurrCity)) {
            readDataFromFile();
            return;
        }
        String url = urlUtils.WEATHER_URL + mCurrCity;
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                readDataFromFile();
                Toast.makeText(WeatherActivity.this,
                        getString(R.string.refresh_net_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                writeDataToFile(response);
                parseWeatherData(response);
            }
        });
    }

    private void writeDataToFile(String data) {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(mFlieName, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readDataFromFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fis;
                    fis = openFileInput(mFlieName);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    fis.close();
                    final String fileContent = EncodingUtils.getString(buffer, "UTF-8");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parseWeatherData(fileContent);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseWeatherData(String response) {
        Gson gson = new Gson();
        WeatherInfo weatherInfo = gson.fromJson(response, WeatherInfo.class);
        WeatherInfo.Data data = weatherInfo.getData();
        String date = null;
        WeatherInfo.DayWeather dayWeather = null;
        if (data.getForecast().size() > 0) {
            dayWeather = data.getForecast().get(0);
            date = dayWeather.getDate();
        }
        String city = data.getCity();
        mWeatherLocal.setText(String.format("%s   %s", city, date));
        if (dayWeather != null) {
            mWeatherIcon.setImageResource(ToolUtils.getWeatherImage(dayWeather.getType()));
            mWeatherTemp.setText(String.format("%s %s", dayWeather.getHigh(), dayWeather.getLow()));
            mWeatherWind.setText(dayWeather.getFengxiang());
            mWeatherContent.setText(dayWeather.getType());
        }

        for (int i = 1; i < data.getForecast().size(); i++) {
            View view = View.inflate(WeatherActivity.this, R.layout.item_weather, null);
            TextView tv_date = (TextView) view.findViewById(R.id.date);
            ImageView iv_icon = (ImageView) view.findViewById(R.id.weatherImage);
            TextView tv_temp = (TextView) view.findViewById(R.id.weatherTemp);
            TextView tv_wind = (TextView) view.findViewById(R.id.wind);
            TextView tv_weather = (TextView) view.findViewById(R.id.weather);

            WeatherInfo.DayWeather weather = data.getForecast().get(i);
            tv_date.setText(weather.getDate());
            if (iv_icon != null) {
                iv_icon.setImageResource(ToolUtils.getWeatherImage(weather.getType()));
            }
            tv_temp.setText(String.format("%s / %s", weather.getHigh(), weather.getLow()));
            tv_wind.setText(weather.getFengxiang());
            tv_weather.setText(weather.getType());
            mLayoutWeatherStatus.addView(view);
        }
    }

    private void initView() {
        mWeatherLocal = (TextView) findViewById(R.id.tv_weather_local);
        mWeatherIcon = (ImageView) findViewById(R.id.iv_weather_icon);
        mWeatherTemp = (TextView) findViewById(R.id.tv_temperature);
        mWeatherWind = (TextView) findViewById(R.id.tv_wind);
        mWeatherContent = (TextView) findViewById(R.id.tv_weather_content);
        mLayoutWeatherStatus = (LinearLayout) findViewById(R.id.ll_weather_status);
    }


    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.activity_navigation_toolbar);
        mToolbar.setTitle(getString(R.string.item_weather));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        super.onDestroy();
    }

    private class MyBdlocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && !TextUtils.isEmpty(bdLocation.getCity())) {
                mCurrCity = bdLocation.getCity();
                mCurrCity = mCurrCity.substring(0, mCurrCity.length() - 1);
                sendParseDataMessage(LOAD_DATA_FINISH);
            } else {
                sendParseDataMessage(LOAD_DATA_ERROR);
            }

        }
    }
}
