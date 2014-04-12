package com.google.android.glass.sample.compass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Macy on 2014-04-12.
 */
public class WeatherRender implements SurfaceHolder.Callback {
    private final FrameLayout mLayout;


    /**
     * Creates a new instance of the {@code CompassRenderer} with the specified context,
     * orientation manager, and landmark collection.
     */
    public WeatherRender(Context context) {
        TextView mMaxTemp;
        LayoutInflater inflater = LayoutInflater.from(context);
        mLayout = (FrameLayout) inflater.inflate(R.layout.compass, null);
        mLayout.setWillNotDraw(false);
        mMaxTemp = (TextView) mLayout.findViewById(R.id.max_temp);
        mMaxTemp.setText("Hi");
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
