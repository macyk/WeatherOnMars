package com.google.android.glass.sample.compass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.glass.sample.compass.model.WeatherObject;

/**
 * Created by Macy on 2014-04-12.
 */
public class WeatherRender implements SurfaceHolder.Callback {
    private final LinearLayout mLayout;
    TextView mMaxTemp;

    /**
     * Creates a new instance of the {@code CompassRenderer} with the specified context,
     * orientation manager, and landmark collection.
     */
    public WeatherRender(Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);
        mLayout = (LinearLayout) inflater.inflate(R.layout.weather, null);
        mLayout.setWillNotDraw(false);
        mMaxTemp = (TextView) mLayout.findViewById(R.id.max_temp);
        mMaxTemp.setText("Loading...");
    }

    public void  WeatherLoaded(WeatherObject weather){
        mMaxTemp.setText(weather.season);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
