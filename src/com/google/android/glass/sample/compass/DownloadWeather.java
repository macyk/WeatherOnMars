package com.google.android.glass.sample.compass;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


/**
 * Created by damien on 4/12/2014.
 */
public class DownloadWeather {



    private static final String BASE_URL = "http://marsweather.ingenology.com/v1/latest";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(  AsyncHttpResponseHandler responseHandler) {
      Log.d("weather", " download2 weather");

      client.get(BASE_URL, null, responseHandler);
      Log.d("weather", " download3 weather");

    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl;
    }



}
