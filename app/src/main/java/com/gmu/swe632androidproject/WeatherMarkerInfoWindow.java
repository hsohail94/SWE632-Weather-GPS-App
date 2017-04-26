package com.gmu.swe632androidproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

/**
 * Created by haaris on 4/25/17.
 */


public class WeatherMarkerInfoWindow implements GoogleMap.InfoWindowAdapter
{
    private HashMap<Marker,JSONObject> markersToWeatherMap;
    private HashMap<Marker,String> markersPlaceNameMap;
    private View view;
    private SingleRouteMapActivity parent;

    public WeatherMarkerInfoWindow (HashMap<Marker,JSONObject> markersToWeatherMap, HashMap<Marker,String> markersPlaceNameMap, SingleRouteMapActivity parent)
    {
        this.markersToWeatherMap = markersToWeatherMap;
        this.markersPlaceNameMap = markersPlaceNameMap;
        this.parent = parent;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker)
    {
        JSONObject weatherObjAtMarker = markersToWeatherMap.get(marker);
        String placeName = markersPlaceNameMap.get(marker);
        String tempUnit = ""; String windSpeedUnits = "";
        if (NetworkMethods.imperialOrMetric == 0) {
            tempUnit = "°F"; windSpeedUnits = " mi/h";
        }
        else if (NetworkMethods.imperialOrMetric == 1) {
            tempUnit = "°C"; windSpeedUnits = " km/h";
        }

        if (weatherObjAtMarker != null && placeName != null)
        {
            try
            {
                LayoutInflater inflater = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.custom_maps_weather_window, null);

                TextView infoWindowLocation = (TextView) view.findViewById(R.id.infowindow_location);
                infoWindowLocation.setText("Location: " + placeName);

                TextView infoWindowTemperature = (TextView) view.findViewById(R.id.infowindow_temperature);
                infoWindowTemperature.setText("Temperature: " + Double.toString(weatherObjAtMarker.getJSONObject("main").getDouble("temp")) + " " + tempUnit);

                TextView infoWindowWeatherSummary = (TextView) view.findViewById(R.id.infowindow_weather_summary);
                infoWindowWeatherSummary.setText("Conditions: " + weatherObjAtMarker.getJSONArray("weather").getJSONObject(0).getString("main"));

                TextView infoWindowWindSpeed = (TextView) view.findViewById(R.id.wind_speed);
                infoWindowWindSpeed.setText("Wind Speed: " + weatherObjAtMarker.getJSONObject("wind").getDouble("speed") + windSpeedUnits);

                TextView infoWindowAirPressure = (TextView) view.findViewById(R.id.air_pressure);
                infoWindowAirPressure.setText("Air Pressure: " + weatherObjAtMarker.getJSONObject("main").getDouble("pressure") + " kPa");

                TextView infoWindowHumidity = (TextView) view.findViewById(R.id.humidity);
                infoWindowHumidity.setText("Humidity: " + weatherObjAtMarker.getJSONObject("main").getInt("humidity"));
            }
            catch (JSONException e)
            {
                Log.e("InfoWindow JSON error", e.getMessage());
            }
        }

        return view;
    }

}