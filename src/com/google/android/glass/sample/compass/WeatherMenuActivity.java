package com.google.android.glass.sample.compass;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Macy on 2014-04-12.
 */
public class WeatherMenuActivity extends Activity {
    private WeatherServices.WeatherBinder mWeatherService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof WeatherServices.WeatherBinder) {
                mWeatherService = (WeatherServices.WeatherBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Do nothing.
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, CompassService.class), mConnection, 0);
        Log.d("weather", "downloading weather");



    }
}