package com.google.android.glass.sample.compass;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Macy on 2014-04-12.
 */
public class WeatherServices extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

  @Override
  public void onCreate() {
    super.onCreate();
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


      //Do anything with response..
    }
  }
}
