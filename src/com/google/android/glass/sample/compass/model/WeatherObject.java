package com.google.android.glass.sample.compass.model;

/**
 * Created by damien on 4/12/2014.
 */
public class WeatherObject {
  public double maxTemp ;
  public double minTemp ;
  public double windSpeed ;
  public String season ;
  public String atmoOpacity ;
  public String sol ;

  public WeatherObject(double maxTemp, double minTemp, double windSpeed, String season, String atmoOpacity, String sol) {
    this.maxTemp = maxTemp;
    this.minTemp = minTemp;
    this.windSpeed = windSpeed;
    this.season = season;
    this.atmoOpacity = atmoOpacity;
    this.sol = sol;
  }
}
