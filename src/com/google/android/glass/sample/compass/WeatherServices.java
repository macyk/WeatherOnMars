package com.google.android.glass.sample.compass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.sample.compass.model.WeatherObject;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


// ...
public class WeatherServices extends Service
{
  // "Life cycle" constants

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
  private LiveCard liveCard;

  public WeatherObject weatherObject;


  // No need for IPC...
  public class LocalBinder extends Binder {
    public WeatherServices getService() {
      return WeatherServices.this;
    }
  }
  private final IBinder mBinder = new LocalBinder();


  public WeatherServices()
  {
    super();
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    currentState = STATE_NORMAL;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    Log.i("weather"," + startId +  + intent");
    onServiceStart();
    return START_STICKY;
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

    new RequestTask().execute("http://marsweather.ingenology.com/v1/archive/?sol=155");

    currentState = STATE_NORMAL;
    return true;
  }

  private boolean onServicePause()
  {
    Log.d("weather","onServicePause() called.");
    return true;
  }
  private boolean onServiceResume()
  {
    Log.d("weather","onServiceResume() called.");
    return true;
  }

  private boolean onServiceStop()
  {
    Log.d("weather","onServiceStop() called.");

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
    if (liveCard == null) {
      String cardId = "livecarddemo_card";
      TimelineManager tm = TimelineManager.from(this);
      liveCard = tm.createLiveCard(cardId);


      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
      R.layout.livecard_livecarddemo);
      remoteViews.setTextViewText(R.id.tvTempMin, String.valueOf(weatherObject.minTemp) + "°C");
      remoteViews.setTextViewText(R.id.tvTempMax, String.valueOf(weatherObject.maxTemp) + "°C");

      if(weatherObject.atmoOpacity.equals("Sunny")){
        remoteViews.setImageViewResource(R.id.atmoOpacity, R.drawable.sun);
      }
      else {
        remoteViews.setImageViewResource(R.id.atmoOpacity, R.drawable.cloud);

      }
      liveCard.setViews(remoteViews);

      Intent intent = new Intent(context, CompassMenuActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      liveCard.setAction(PendingIntent.getActivity(this, 0, intent, 0));
      liveCard.publish(LiveCard.PublishMode.REVEAL);
    } else {
      // Card is already published.
      return;
    }
  }

  private void unpublishCard(Context context)
  {
    Log.d("weather","unpublishCard() called.");
    if (liveCard != null) {
      liveCard.unpublish();
      liveCard = null;
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

      try {
        JSONObject jsonObj = new JSONObject(result);
        String mResult = jsonObj.getString("results").replace("]", "").replace("[", "");
        Log.d("weather", mResult);

        JSONObject resul = new JSONObject(mResult);

        Log.d("weather", "season : "+ resul.getString("season"));
        Log.d("weather", "wind speed : "+ resul.getInt("wind_speed"));
        Log.d("weather", "max-temp : "+ resul.getDouble("max_temp"));
        Log.d("weather", "presure : "+ resul.getDouble("pressure"));


        String season = resul.getString("season");
        String atmoOpacity = resul.getString("season");
        String sol = resul.getString("season");
        double windSpeed = resul.getInt("wind_speed");
        double maxTemp = resul.getDouble("max_temp");
        double mainTemp = resul.getDouble("min_temp");

         weatherObject = new WeatherObject(maxTemp, mainTemp, windSpeed, season, atmoOpacity, sol);


        publishCard(getApplicationContext());


      } catch (JSONException e) {
        e.printStackTrace();
      }
      //Do anything with response..
    }
  }

}