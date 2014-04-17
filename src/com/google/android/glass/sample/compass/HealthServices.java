package com.google.android.glass.sample.compass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Macy on 2014-04-12.
 */
public class HealthServices extends Service {// "Life cycle" constants

// [1] Starts from this..
private static final int STATE_NORMAL = 1;

// [2] When panic action has been triggered by the user.
private static final int STATE_PANIC_TRIGGERED = 2;

// [3] Note that cancel, or successful send, etc. change the state back to normal
// These are intermediate states...
private static final int STATE_CANCEL_REQUESTED = 4;
private static final int STATE_CANCEL_PROCESSED = 8;
private static final int STATE_PANIC_PROCESSED = 16;
        // ....

// Global "state" of the service.
// Currently not being used...
private int currentState;


// For live card
private LiveCard mLiveCard;
private  LiveCard mLiveCard2;
private String  mResult;
private Task mTask;
private HealthRender mRenderer, mRenderer2;

  public static String pulse;
  public static String gsr;
  public static String temp;
  public static String o2 ;
  public static String suitpresure ;
  public static String bloodpresure ;
  public static String radiation ;


private final HealthBinder mBinder = new HealthBinder();

private TextToSpeech mSpeech;

    public HealthServices()
    {
        super();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Do nothing.
            }
        });
        currentState = STATE_NORMAL;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i("weather", " + startId +  + intent");
        onServiceStart();
        if (mLiveCard == null && mLiveCard2 == null) {
            String cardId = "health";
            String cardId2 = "health2";
            TimelineManager tm = TimelineManager.from(this);
            mLiveCard = tm.createLiveCard(cardId);
            mLiveCard2 = tm.createLiveCard(cardId2);
            mRenderer = new HealthRender(this,0);
            mRenderer2 = new HealthRender(this,1);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mRenderer);
            mLiveCard2.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mRenderer2);
        }
        else {
            mLiveCard.unpublish();
            mLiveCard2.unpublish();
        }
        return START_STICKY;
    }

    private class Task extends AsyncTask<Void, String, Void> {

        private boolean running = true;
        @Override
        protected Void doInBackground(Void... params) {
            while( running ) {
                //fetch data from server;
                this.publishProgress("updated json");
                try {
                    Thread.sleep(5000); // removed try/catch for readability
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if( ! running ) {
                return;
            }
            String json = values[0];
            //update views directly, as this is run on the UI thread.
            //textView.setText(json);
        }

        public void stop() {
            running = false;
        }
    }

public class HealthBinder extends Binder {
    /**
     * Read the current heading aloud using the text-to-speech engine.
     */
    public void readHeadingAloud() {

        String headingText = "ha ha";
        mSpeech.speak(headingText, TextToSpeech.QUEUE_FLUSH, null);
    }
}

    @Override
    public IBinder onBind(Intent intent)
    {
        // ????
        onServiceStart();
        return mBinder;
    }

    @Override
    public void onDestroy()
    {
        // ???
        onServiceStop();
        mLiveCard.getSurfaceHolder().removeCallback(mRenderer);
        super.onDestroy();
    }


    // Service state handlers.
    // ....

    private boolean onServiceStart()
    {
        Log.d("weather","onServiceStart() called.");

        // TBD:
        // Publish live card...
        // ....
        // ....

        new RequestTask().execute("http://192.168.1.142:3000/getdata");
        currentState = STATE_NORMAL;
        return true;
    }

    private boolean onServicePause()
    {
        mTask.stop();
        mTask = null;
        Log.d("weather","onServicePause() called.");
        return true;
    }
    private boolean onServiceResume()
    {
        mTask = new Task();
        mTask.execute();
        Log.d("weather","onServiceResume() called.");
        return true;
    }

    private boolean onServiceStop()
    {
        Log.d("weather","onServiceStop() called.");
        mTask.stop();
        mTask = null;
        // TBD:
        // Unpublish livecard here
        // .....
        unpublishCard(this);
        // ...

        return true;
    }


    // For live cards...

    private void publishCard(Context context)
    {
        Log.d("weather","publishCard() called.");
        if (mLiveCard != null && mLiveCard2 != null) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.health);
            mLiveCard.setViews(remoteViews);
            mLiveCard2.setViews(remoteViews);
            Intent intent = new Intent(context, CompassMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
            mLiveCard2.setAction(PendingIntent.getActivity(context, 0, intent, 0));
            mLiveCard2.publish(LiveCard.PublishMode.REVEAL);
        } else {
            // Card is already published.
            mLiveCard.unpublish();
            mLiveCard2.unpublish();
            return;
        }
    }

    private void unpublishCard(Context context)
    {
        Log.d("weather","unpublishCard() called.");
        if (mLiveCard != null) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        if (mLiveCard2 != null) {
            mLiveCard2.unpublish();
            mLiveCard2 = null;
        }
    }


class RequestTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);


        Log.d("weather", result);

        mResult = result;
      JSONArray list = null;
      try {
         list = new JSONArray(result);


      } catch (JSONException e) {
        e.printStackTrace();
      }

      try {
          if(list!=null) {
              pulse = String.valueOf(list.getJSONObject(0).getInt("pulse"));
              gsr = list.getJSONObject(1).getString("gsr");
              temp = list.getJSONObject(2).getString("temp");
              o2 = list.getJSONObject(3).getString("o2");
              suitpresure = list.getJSONObject(4).getString("suitpressure");
              bloodpresure = list.getJSONObject(5).getString("bloodpressure");
              radiation = list.getJSONObject(6).getString("radiation");

              Log.d("weather", pulse);
          }

      } catch (JSONException e) {
        e.printStackTrace();
      }

      publishCard(getApplicationContext());
        //Do anything with response..
    }
}
}
