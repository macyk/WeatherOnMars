package com.google.android.glass.sample.compass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Macy on 2014-04-12.
 */
public class WeatherRender implements SurfaceHolder.Callback {
    private final FrameLayout mLayout;
  private SurfaceHolder mHolder;
  private boolean mTooSteep;
  private boolean mInterference;
  private int mSurfaceWidth;
  private int mSurfaceHeight;
  private final OrientationManager mOrientationManager;

    /**
     * Creates a new instance of the {@code CompassRenderer} with the specified context,
     * orientation manager, and landmark collection.
     */
    public WeatherRender(Context context, OrientationManager orientationManager) {
        TextView mMaxTemp;
        LayoutInflater inflater = LayoutInflater.from(context);
        mLayout = (FrameLayout) inflater.inflate(R.layout.compass, null);
        mLayout.setWillNotDraw(false);
        mMaxTemp = (TextView) mLayout.findViewById(R.id.tips_view);
        mMaxTemp.setText("Hi");
      mOrientationManager = orientationManager;
    }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    mSurfaceWidth = width;
    mSurfaceHeight = height;
    doLayout();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    mHolder = holder;

    mOrientationManager.start();




  }

  private void doLayout() {
    // Measure and update the layout so that it will take up the entire surface space
    // when it is drawn.
    int measuredWidth = View.MeasureSpec.makeMeasureSpec(mSurfaceWidth,
      View.MeasureSpec.EXACTLY);
    int measuredHeight = View.MeasureSpec.makeMeasureSpec(mSurfaceHeight,
      View.MeasureSpec.EXACTLY);

    mLayout.measure(measuredWidth, measuredHeight);
    mLayout.layout(0, 0, mLayout.getMeasuredWidth(), mLayout.getMeasuredHeight());
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {

    mOrientationManager.stop();
  }
}
