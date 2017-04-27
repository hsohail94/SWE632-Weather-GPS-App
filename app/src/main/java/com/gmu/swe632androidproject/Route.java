package com.gmu.swe632androidproject;

/**
 * Created by haaris on 4/15/17.
 */

/**
 * This class represents an individual route. Will be used in our RecyclerView when displaying routes as cards.
 */
public class Route
{
    private String distance;

    private String totalTime;

    private String averageWeather;

    public Route (String distance, String totalTime, String averageWeather)
    {
        this.distance = distance;
        this.totalTime = totalTime;
        this.averageWeather = averageWeather;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getAverageWeather() {
        return averageWeather;
    }

    public void setAverageWeather(String averageWeather) {
        this.averageWeather = averageWeather;
    }


}
