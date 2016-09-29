package com.app.teacup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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


public class WeatherActivity extends AppCompatActivity {

    private ImageView mWeatherIcon;
    private TextView mWeatherLocal;
    private TextView mWeatherTemp;
    private TextView mWeatherWind;
    private TextView mWeatherContent;
    private LinearLayout mLayoutWeatherStatus;
    private Toolbar mToolbar;
    private LocationClient locationClient = null;
    public BDLocationListener myListener = new MyBdlocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(myListener);
        locationClient.start();
        initLocation();
        initView();
        initToolBar();
        startLoadData();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setOpenGps(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        locationClient.setLocOption(option);
    }

    private void startLoadData() {
        String url = urlUtils.WEATHER_URL + getString(R.string.default_city);
        OkHttpUtils.getAsyn(url, new OkHttpUtils.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                parseWeatherData(response);
            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        super.onDestroy();
    }

    private class MyBdlocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            String city = bdLocation.getCity();
            Log.i("WeatherActivity", "current===city==" + city);
        }
    }
}
