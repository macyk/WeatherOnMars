package com.google.android.glass.sample.compass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.android.glass.sample.compass.model.Landmarks;
import com.google.android.glass.sample.compass.model.WeatherObject;
import com.google.android.glass.sample.compass.util.MathUtils;
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

/**
 * Created by Macy on 2014-04-12.
 */
public class WeatherServices extends Service {
    private static final String LIVE_CARD_ID = "weather";
    private LiveCard mLiveCard;
    private TimelineManager mTimelineManager;
    private WeatherRender mRenderer;
    private Landmarks mLandmarks;
    private OrientationManager mOrientationManager;
    private TextToSpeech mSpeech;
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * A binder that gives other components access to the speech capabilities provided by the
     * service.
     */
    public class WeatherBinder extends Binder {
        /**
         * Read the current heading aloud using the text-to-speech engine.
         */
        public void readHeadingAloud() {
            float heading = mOrientationManager.getHeading();

            Resources res = getResources();
            String[] spokenDirections = res.getStringArray(R.array.spoken_directions);
            String directionName = spokenDirections[MathUtils.getHalfWindIndex(heading)];

            int roundedHeading = Math.round(heading);
            int headingFormat;
            if (roundedHeading == 1) {
                headingFormat = R.string.spoken_heading_format_one;
            } else {
                headingFormat = R.string.spoken_heading_format;
            }

            String headingText = res.getString(headingFormat, roundedHeading, directionName);
            mSpeech.speak(headingText, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
            Log.d("weather", "started!");
            mRenderer = new WeatherRender(this);

            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mRenderer);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, WeatherMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }

        return START_STICKY;
    }

  @Override
  public void onCreate() {
    super.onCreate();
      SensorManager sensorManager =
              (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      LocationManager locationManager =
              (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      mOrientationManager = new OrientationManager(sensorManager, locationManager);
      mLandmarks = new Landmarks(this);
      mTimelineManager = TimelineManager.from(this);
    new RequestTask().execute("http://marsweather.ingenology.com/v1/archive/?sol=155");
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
        double windSpeed = resul.getInt("wind_speed");
        double maxTemp = resul.getDouble("max_temp");
        double mainTemp = resul.getDouble("min_temp");

        WeatherObject weatherObject = new WeatherObject(maxTemp, mainTemp, windSpeed, season);

        mRenderer.WeatherLoaded(weatherObject);

      } catch (JSONException e) {
        e.printStackTrace();
      }
      //Do anything with response..
    }
  }
}
