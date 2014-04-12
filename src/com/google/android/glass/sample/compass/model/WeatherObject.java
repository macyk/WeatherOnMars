package com.google.android.glass.sample.compass.model;

/**
 * Created by damien on 4/12/2014.
 */
public class WeatherObject {
  public double maxTemp ;
  public double minTemp ;
  public double windSpeed ;
  public String season ;
  public String sol ;

  public WeatherObject(double maxTemp, double minTemp, double windSpeed, String season) {
    this.maxTemp = maxTemp;
    this.minTemp = minTemp;
    this.windSpeed = windSpeed;
    this.season = season;
    this.season = season;
  }
}
