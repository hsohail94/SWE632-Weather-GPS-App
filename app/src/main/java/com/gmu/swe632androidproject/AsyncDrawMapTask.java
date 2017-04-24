package com.gmu.swe632androidproject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by haaris on 4/22/17.
 */

public class AsyncDrawMapTask extends AsyncTask<Void, Void, JSONArray>
{
    private URL weatherAPIUrl;
    private List<LatLng> routeWeatherMarkerList;
    private ProgressDialog progressDialog;
    private int routeNumber;
    private URL mapsRouteUrl;
    private GoogleMap mMap;
    private SingleRouteMapActivity context; //only the map activity is calling this method, so context will be SingleRouteMapActivity instead of Activity

    public AsyncDrawMapTask (int routeNumber, URL mapsRouteUrl, SingleRouteMapActivity context, GoogleMap mMap)
    {
        this.routeNumber = routeNumber;
        this.mapsRouteUrl = mapsRouteUrl;
        this.context = context;
        this.mMap = mMap;
    }

    protected void onPreExecute()
    {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("Drawing selected route...");
        progressDialog.show();

    }


    @Override
    protected JSONArray doInBackground(Void... voids)
    {
    //protected List<JSONArray> doInBackground (Void... voids){
        JSONObject jsonDataObject = null;
        JSONArray routesJsonArray = null;
        JSONObject weatherJsonDataObj = null;
        JSONArray weatherJsonArray = null;

        int runningDurationSum = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        LatLng objectToSend = null;
        routeWeatherMarkerList = new ArrayList<LatLng>();

        //Try getting a JSON data response from our Maps API Call
        try
        {
            //Get our JSON data from Maps, and transform it into a JSONArray for parsing purposes
            String jsonDataResponse = NetworkMethods.getResponseFromHttpUrl(this.mapsRouteUrl);
            jsonDataObject = new JSONObject(jsonDataResponse);

            Log.d("JSON Data returned: ", jsonDataResponse);

            if (jsonDataObject.has("routes"))
            {
                routesJsonArray = jsonDataObject.getJSONArray("routes"); //store routes in this object
                Log.d("Json routes returned: ", routesJsonArray.toString());
                int overallTime = 0;
                String[] timeStringSplit;

                //This will be for building our weather API request
                JSONObject legsOfSpecificRoute = routesJsonArray.getJSONObject(routeNumber).getJSONArray("legs").getJSONObject(0);
                JSONArray stepsOfLegsOfRoute = legsOfSpecificRoute.getJSONArray("steps");
                for (int i = 0; i < stepsOfLegsOfRoute.length(); i++)
                {
                    JSONObject individualStep = stepsOfLegsOfRoute.getJSONObject(i);
                    String timeOfStep = individualStep.getJSONObject("duration").getString("text");
                    Log.v("Step duration: ", timeOfStep);

                    if (timeOfStep.contains("hours") && timeOfStep.contains("min"))
                    {
                        timeStringSplit = timeOfStep.split("\\s+"); //split along whitespace
                        overallTime = (Integer.parseInt(timeStringSplit[0]) * 60) + Integer.parseInt(timeStringSplit[2]);

                    }
                    else if (timeOfStep.contains("min"))
                    {
                        timeStringSplit = timeOfStep.split("\\s+"); //same split, but only contains mins
                        overallTime = Integer.parseInt(timeStringSplit[0]);
                    }

                    runningDurationSum += overallTime;
                    Log.v("Minutes total for step", Integer.toString(overallTime));
                    Calendar timeAtStep = NetworkMethods.getCalendarDateTimeAfterMinutesAdd(runningDurationSum);
                    Log.v("Time at step", sdf.format(timeAtStep.getTime()));
                    Log.v("Time since start", Integer.toString(runningDurationSum));

                    //If we've passed a certain interval (between 2.5 and 4 hours), we'll draw a marker at that location
                    //So for that location, we'll send a weather API call to fetch the weather at that location at that time.
                    //The weather data itself isn't very flexible for time, so we'll just need to do some interval comparisons
                    //there too.
                    if (runningDurationSum >= 200 && runningDurationSum <= 400)
                    {
                        JSONObject currentLatLngObj = individualStep.getJSONObject("end_location");
                        objectToSend = new LatLng(currentLatLngObj.getDouble("lat"),
                                                    currentLatLngObj.getDouble("lng"));
                        routeWeatherMarkerList.add(objectToSend);
                        weatherAPIUrl = NetworkMethods.buildWeatherRequestURL(objectToSend.latitude, objectToSend.longitude);
                        String jsonWeatherDataResponse = NetworkMethods.getResponseFromHttpUrl(weatherAPIUrl);
                        Log.v("JSON Weather Data", jsonWeatherDataResponse);
                        weatherJsonDataObj = new JSONObject(jsonWeatherDataResponse);
                        weatherJsonArray = weatherJsonDataObj.getJSONArray("list");
                        Log.v("Weather List at step", weatherJsonArray.toString());
                    }
                }

            }


        }
        catch (IOException e)
        {
            Log.e("Error: ", e.getMessage());
        }
        catch (JSONException e)
        {
            Log.e("Error: ", e.getMessage());
            e.printStackTrace();
        }

        return routesJsonArray;


    }

    protected void onPostExecute (JSONArray jsonArray)
    {
        progressDialog.hide();
        if (jsonArray != null)
            this.drawRouteBetweenOriginAndDest(jsonArray, this.routeNumber);
    }

    /**
     * This method will be responsible for actually drawing the route between an origin and a
     * destination.
     *
     * @param routesArray: the JSON array containing all routes by Google Maps.
     * @param routeNumber: the route that the user has chosen
     */
    private void drawRouteBetweenOriginAndDest (JSONArray routesArray, int routeNumber)
    {
        try
        {
            JSONObject routesObj = routesArray.getJSONObject(routeNumber);
            JSONObject routePolylines = routesObj.getJSONObject("overview_polyline"); //essentially a JSONObject containing all the polyline points
            String encodedString = routePolylines.getString("points");
            List<LatLng> pointsList = decodePoly(encodedString); //get a list of latitude-longitude pairs from the encoded polylines in our JSON object, which will form a line of sorts
            //use that list of lat-long pairs to create a single, new polyline. This polyline is our path
            Polyline routeLine = this.mMap.addPolyline(new PolylineOptions().addAll(pointsList).width(15).color(Color.parseColor("#05b1fb")).geodesic(true));
        }
        catch (JSONException e)
        {
            Log.e("JSONException drawroute", e.getMessage());
        }
    }

    /**
     * The Google Maps Directions API uses this thing called polyline encoding in order to store
     * coordinates as a single string in a JSON response. From here, I need to take those encoded points,
     * and turn them into LatLng objects to represent them as latitude-longitude pairs on my map, the
     * combination of which will make up my line between my two markers. This implentation itself is based
     * on an algorithm for decoding polylines by Jeffrey Sambells (http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java)
     * as well as similar algorithms for encoding and decoding polylines by Mark McClur at UNC Asheville
     * (http://web.archive.org/web/20130705171527/http://facstaff.unca.edu:80/mcmcclur/googlemaps/encodepolyline/)
     *
     * Polyline Documentation: https://developers.google.com/android/reference/com/google/android/gms/maps/model/Polyline
     *
     * @param encoded: a polyline-encoded string with point coordinates
     * @return: a list of polylines decoded into lat-long pairs
     */
    private List<LatLng> decodePoly(String encoded)
    {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }


}
