package com.google.android.glass.sample.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Created by Macy on 2014-04-13.
 */
public class HealthRender implements SurfaceHolder.Callback {

    private static final String TAG = HealthRender.class.getSimpleName();

    /**
     * The (absolute) pitch angle beyond which the compass will display a message telling the user
     * that his or her head is at too steep an angle to be reliable.
     */
    private static final float TOO_STEEP_PITCH_DEGREES = 70.0f;

    /** The refresh rate, in frames per second, of the compass. */
    private static final int REFRESH_RATE_FPS = 45;

    /** The duration, in milliseconds, of one frame. */
    private static final long FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;

    private SurfaceHolder mHolder;
    private boolean mTooSteep;
    private boolean mInterference;
    private RenderThread mRenderThread;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private final FrameLayout mLayout;
//    private final RelativeLayout mTipsContainer;
//    private final TextView mTipsView;
    TextView tv1, tv2, tv3, tv4;


  private final OrientationManager.OnChangedListener mCompassListener =
            new OrientationManager.OnChangedListener() {

                @Override
                public void onOrientationChanged(OrientationManager orientationManager) {

                    boolean oldTooSteep = mTooSteep;
                    mTooSteep = (Math.abs(orientationManager.getPitch()) > TOO_STEEP_PITCH_DEGREES);
                    if (mTooSteep != oldTooSteep) {
                        updateTipsView();
                    }
                }

                @Override
                public void onLocationChanged(OrientationManager orientationManager) {
                }

                @Override
                public void onAccuracyChanged(OrientationManager orientationManager) {
                    mInterference = orientationManager.hasInterference();
                    updateTipsView();
                }
            };

    /**
     * Creates a new instance of the {@code CompassRenderer} with the specified context,
     * orientation manager, and landmark collection.
     */
    public HealthRender(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mLayout = (FrameLayout) inflater.inflate(R.layout.health, null);
        mLayout.setWillNotDraw(false);

//        mTipsContainer = (RelativeLayout) mLayout.findViewById(R.id.tips_container);
//        mTipsView = (TextView) mLayout.findViewById(R.id.tips_view);


      ImageView iv1 = (ImageView) mLayout.findViewById(R.id.iv1);
//      iv1.setImageDrawable();
       tv1 = (TextView) mLayout.findViewById(R.id.tv1);
      tv1.setText("pulse : "+HealthServices.pulse);


      ImageView iv2 = (ImageView) mLayout.findViewById(R.id.iv2);
//      iv2.setImageDrawable();
       tv2 = (TextView) mLayout.findViewById(R.id.tv2);
      tv2.setText("gsr : "+HealthServices.gsr);


      ImageView iv3 = (ImageView) mLayout.findViewById(R.id.iv3);
//      iv3.setImageDrawable();
      TextView tv3 = (TextView) mLayout.findViewById(R.id.tv3);
//      tv3.setText();


      ImageView iv4 = (ImageView) mLayout.findViewById(R.id.iv4);
//      iv4.setImageDrawable();
      TextView tv4 = (TextView) mLayout.findViewById(R.id.tv4);
//      tv4.setText();
      
      
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

        mRenderThread = new RenderThread();
        mRenderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mRenderThread.quit();
    }

    /**
     * Requests that the views redo their layout. This must be called manually every time the
     * tips view's text is updated because this layout doesn't exist in a GUI thread where those
     * requests will be enqueued automatically.
     */
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

    /**
     * Repaints the compass.
     */
    private synchronized void repaint() {
        Canvas canvas = null;

        try {
            canvas = mHolder.lockCanvas();
        } catch (RuntimeException e) {
            Log.d(TAG, "lockCanvas failed", e);
        }

        if (canvas != null) {
            mLayout.draw(canvas);

          tv2.setText("gsr : "+HealthServices.gsr);
          tv1.setText("pulse : "+HealthServices.pulse);



          try {
                mHolder.unlockCanvasAndPost(canvas);
            } catch (RuntimeException e) {
                Log.d(TAG, "unlockCanvasAndPost failed", e);
            }
        }
    }

    /**
     * Shows or hides the tip view with an appropriate message based on the current accuracy of the
     * compass.
     */
    private void updateTipsView() {
        int stringId = 0;

        // Only one message (with magnetic interference being higher priority than pitch too steep)
        // will be displayed in the tip.
        if (mInterference) {
            stringId = R.string.magnetic_interference;
        } else if (mTooSteep) {
            stringId = R.string.pitch_too_steep;
        }

        boolean show = (stringId != 0);

        if (show) {
//            mTipsView.setText(stringId);
            doLayout();
        }

//        if (mTipsContainer.getAnimation() == null) {
//            float newAlpha = (show ? 1.0f : 0.0f);
//            mTipsContainer.animate().alpha(newAlpha).start();
//        }
    }

    /**
     * Redraws the compass in the background.
     */
    private class RenderThread extends Thread {
        private boolean mShouldRun;

        /**
         * Initializes the background rendering thread.
         */
        public RenderThread() {
            mShouldRun = true;
        }

        /**
         * Returns true if the rendering thread should continue to run.
         *
         * @return true if the rendering thread should continue to run
         */
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        /**
         * Requests that the rendering thread exit at the next opportunity.
         */
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                long frameStart = SystemClock.elapsedRealtime();
                repaint();
                long frameLength = SystemClock.elapsedRealtime() - frameStart;

                long sleepTime = FRAME_TIME_MILLIS - frameLength;
                if (sleepTime > 0) {
                    SystemClock.sleep(sleepTime);
                }
            }
        }
    }
}
