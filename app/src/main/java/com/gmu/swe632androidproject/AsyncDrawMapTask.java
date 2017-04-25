package com.gmu.swe632androidproject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by haaris on 4/22/17.
 */

public class AsyncDrawMapTask extends AsyncTask<Void, Void, JSONArray>
{
    private URL weatherAPIUrl;
    private List<LatLng> routeWeatherMarkerList;
    private List<JSONObject> weatherJsonObjForMarkersList;
    private HashMap<LatLng, JSONObject> routeMarkerToWeatherForecastMap;
    private HashMap<LatLng, String> latLngToPlaceNameMap;

    private ProgressDialog progressDialog;
    private String weatherDateFormat = "yyyy-MM-dd HH:mm:ss";
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

        //We will not have the current time across the route, so we will initialize the calendar object,
        //then keep incrementing it by adding the durations below for every step.
        //This way, our time persists.
        Calendar timeAtStep = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        int runningDurationSum = 0;
        int overallRunningDurationSum = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        LatLng objectToSend = null;
        routeWeatherMarkerList = new ArrayList<LatLng>();
        routeMarkerToWeatherForecastMap = new HashMap<LatLng, JSONObject>();
        latLngToPlaceNameMap = new HashMap<LatLng, String>();

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

                int minutesAtStep = 0;
                String[] timeStringSplit;

                //This will be for building our weather API request
                JSONObject legsOfSpecificRoute = routesJsonArray.getJSONObject(routeNumber).getJSONArray("legs").getJSONObject(0);
                JSONArray stepsOfLegsOfRoute = legsOfSpecificRoute.getJSONArray("steps");
                for (int i = 0; i < stepsOfLegsOfRoute.length(); i++)
                {
                    weatherJsonObjForMarkersList = new ArrayList<JSONObject>();
                    JSONObject individualStep = stepsOfLegsOfRoute.getJSONObject(i);
                    String timeOfStep = individualStep.getJSONObject("duration").getString("text");
                    Log.v("Step duration: ", timeOfStep);

                    if (timeOfStep.contains("hours") && timeOfStep.contains("min"))
                    {
                        timeStringSplit = timeOfStep.split("\\s+"); //split along whitespace
                        minutesAtStep = (Integer.parseInt(timeStringSplit[0]) * 60) + Integer.parseInt(timeStringSplit[2]);

                    }
                    else if (timeOfStep.contains("min"))
                    {
                        timeStringSplit = timeOfStep.split("\\s+"); //same split, but only contains mins
                        minutesAtStep = Integer.parseInt(timeStringSplit[0]);
                    }

                    runningDurationSum += minutesAtStep;
                    Log.v("Minutes total for step", Integer.toString(minutesAtStep));
                    timeAtStep = NetworkMethods.getCalendarDateTimeAfterMinutesAdd(timeAtStep, runningDurationSum);
                    Log.v("Time at step", sdf.format(timeAtStep.getTime()));
                    Log.v("Minutes since start", Integer.toString(runningDurationSum));

                    //If we've passed a certain interval (between 3 and 4.5 hours), we'll draw a marker at that location
                    //So for that location, we'll send a weather API call to fetch the weather at that location at that time.
                    //The weather data itself isn't very flexible for time, so we'll just need to do some interval comparisons
                    //there too.
                    if (runningDurationSum >= 120 && runningDurationSum <= 240)
                    {
                        JSONObject currentLatLngObj = individualStep.getJSONObject("end_location");
                        objectToSend = new LatLng(currentLatLngObj.getDouble("lat"), currentLatLngObj.getDouble("lng"));
                        routeWeatherMarkerList.add(objectToSend);

                        weatherAPIUrl = NetworkMethods.buildWeatherRequestURL(objectToSend.latitude, objectToSend.longitude);
                        String jsonWeatherDataResponse = NetworkMethods.getResponseFromHttpUrl(weatherAPIUrl);
                        Log.v("JSON Weather Data", jsonWeatherDataResponse);

                        weatherJsonDataObj = new JSONObject(jsonWeatherDataResponse);
                        weatherJsonArray = weatherJsonDataObj.getJSONArray("list");
                        if (weatherJsonArray != null)
                        {
                            String placeInMarker = weatherJsonDataObj.getJSONObject("city").getString("name") + ", " +
                                    weatherJsonDataObj.getJSONObject("city").getString("country");
                            Log.v("Weather List at step", weatherJsonArray.toString());
                            Log.v("Current place", placeInMarker);
                            latLngToPlaceNameMap.put(objectToSend, placeInMarker);

                            //When using the free version of the OpenWeatherMap API, we are unfortunately limited to 3 hour windows
                            //of weather forecasts over a span of 5 days, so we need to find the next interval where we will get our weather
                            //forecast from. This interval will be based on a difference comparison.
                            for (int j = 0; j < weatherJsonArray.length() - 1; j++)
                            {
                                JSONObject currentWeatherObj = weatherJsonArray.getJSONObject(j);
                                Log.v("Weather Object " + j, currentWeatherObj.toString());
                                int k = j+1;
                                JSONObject weatherThreeHoursFromNowObj = weatherJsonArray.getJSONObject(k);
                                Log.v("Weather Object " + k, weatherThreeHoursFromNowObj.toString());
                                String currentDateTimeInForecast = currentWeatherObj.getString("dt_txt");
                                String nextDateTimeInForecast = weatherThreeHoursFromNowObj.getString("dt_txt");
                                Log.v("Current forecast", currentDateTimeInForecast);
                                Log.v("Next forecast", nextDateTimeInForecast);
                                Calendar currentForecastCal = NetworkMethods.getCalendarObjFromString(currentDateTimeInForecast, weatherDateFormat);
                                Calendar nextForecastCal = NetworkMethods.getCalendarObjFromString(nextDateTimeInForecast, weatherDateFormat);
                                long differenceOne = timeAtStep.getTimeInMillis() - currentForecastCal.getTimeInMillis();
                                long differenceTwo = nextForecastCal.getTimeInMillis() - timeAtStep.getTimeInMillis();
                                Log.v("DifferenceOne", Long.toString(differenceOne));
                                Log.v("DifferenceTwo", Long.toString(differenceTwo));
                                if (differenceOne > 0.00 && differenceTwo > 0.00)
                                {
                                    if (differenceOne > differenceTwo)
                                    {
                                        weatherJsonObjForMarkersList.add(weatherThreeHoursFromNowObj);
                                        routeMarkerToWeatherForecastMap.put(objectToSend, weatherThreeHoursFromNowObj);
                                    }
                                    else if (differenceOne < differenceTwo)
                                    {
                                        weatherJsonObjForMarkersList.add(currentWeatherObj);
                                        routeMarkerToWeatherForecastMap.put(objectToSend, currentWeatherObj);
                                    }
                                    //If the difference between the current time and the UTC time given by the Weather API
                                    //is the same as the difference in the time in the next prediction, and the current UTC time,
                                    //then just add the current forecast object. It doesn't really matter which one, anyways.
                                    else
                                    {
                                        weatherJsonObjForMarkersList.add(weatherThreeHoursFromNowObj);
                                        routeMarkerToWeatherForecastMap.put(objectToSend, weatherThreeHoursFromNowObj);
                                    }
                                    break;
                                }
                                else if (differenceOne <= 0.00)
                                {
                                    weatherJsonObjForMarkersList.add(currentWeatherObj);
                                    routeMarkerToWeatherForecastMap.put(objectToSend, currentWeatherObj);
                                }
                            }

                            //Once we have our latlng -> weatherobj mapping, we reset the duration and continue the loop
                            runningDurationSum = 0;
                            continue;
                        }
                    }
                }

            }


        }
        catch (IOException e)
        {
            Log.e("IO Error: ", e.getMessage());
        }
        catch (JSONException e)
        {
            Log.e("Route JSON Error", e.getMessage());
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            Log.e("Calendar error", e.getMessage());
        }

        return routesJsonArray;


    }

    protected void onPostExecute (JSONArray jsonArray)
    {
        progressDialog.hide();
        if (jsonArray != null)
        {
            try {
                this.drawRouteBetweenOriginAndDest(jsonArray, this.routeNumber);
                this.drawRouteMarkersWithWeather();
            }
            catch (JSONException e)
            {
                Log.e("JSON Error", e.getMessage());
            }
        }
    }
    /**
     * This method will draw markers on top of the route, and as such, will be called after the drawRouteBetweenOriginAndDest
     * method.
     */
    protected void drawRouteMarkersWithWeather() throws JSONException
    {
        int i = 0;
        HashMap<LatLng,String> latLngStringHashMap = latLngToPlaceNameMap;
        HashMap<LatLng,JSONObject> latLngJSONObjectHashMap = routeMarkerToWeatherForecastMap;
        for (LatLng coordinate: latLngStringHashMap.keySet())
        {
            i++;
            String placeName = latLngStringHashMap.get(coordinate);
            Log.v("Place from weather JSON", placeName);
            JSONObject weatherDataObj = latLngJSONObjectHashMap.get(coordinate);
            Log.v("Weather JSONObj", weatherDataObj.toString());
            String windowData = "Location: " + placeName + "\n" +
                                "Average Temperature: " +  weatherDataObj.getJSONObject("main").getDouble("temp") + "\n" +
                                "Conditions: " + weatherDataObj.getJSONArray("weather").getJSONObject(0).getString("description");

            MarkerOptions markerOption = new MarkerOptions().position(coordinate).title("Marker " + i)
                                                .snippet(windowData).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            this.mMap.addMarker(markerOption);
        }
    }

    /**
     * Step 1: convert the LatLng pairs we've obtained for our weather forecasts, place it in a map,
     * and convert them to Google Maps Marker representations.
     * @return

    private HashMap<LatLng,Marker> convertLatLngPairsToMarkersOnMap()
    {
        int i = 0;
        HashMap<LatLng, Marker> latLngMarkerHashMap = new HashMap<LatLng, Marker>();
        for (LatLng coordinate: routeMarkerToWeatherForecastMap.keySet())
        {
            i++;
            Marker m = this.mMap.addMarker(new MarkerOptions().title("Marker " + i)
                    .position(coordinate));
            latLngMarkerHashMap.put(coordinate,m);
        }
        return latLngMarkerHashMap;
    }*/

    /**
     * Step 2: each marker needs a string title to represent it. So we're getting the city and country name
     * matching the relevant latlng pair, and mapping that to the Maps Marker object.
     * @return

    private HashMap<Marker,String> buildLocationMarkerToPlaceNameAndCountryMap()
    {
        HashMap<LatLng,Marker> latLngMarkerHashMap = convertLatLngPairsToMarkersOnMap();
        HashMap<Marker,String> markerStringHashMap = new HashMap<Marker, String>();
        HashMap<LatLng,String> latLngStringHashMap = latLngToPlaceNameMap;

        for (LatLng latLng: latLngMarkerHashMap.keySet())
        {
            Marker m = latLngMarkerHashMap.get(latLng);
            String place = latLngStringHashMap.get(latLng);
            markerStringHashMap.put(m,place);
        }

        return markerStringHashMap;
    }*/


    /**
     * Step 3: We have our Markers, and we have a map for latlng->JSONObject. Now, based on what we did in step 1,
     * we need to take the results from that method, and create a marker->JSONObject map.
     * @return

    private HashMap<Marker,JSONObject> buildLocationMarkerToWeatherJsonObjMap()
    {
        HashMap<Marker,JSONObject> markerJSONObjectHashMap = new HashMap<Marker, JSONObject>();
        HashMap<LatLng,JSONObject> latLngJSONObjectHashMap = routeMarkerToWeatherForecastMap;
        HashMap<LatLng,Marker> latLngMarkerHashMap = convertLatLngPairsToMarkersOnMap();

        for (LatLng coordinate: latLngJSONObjectHashMap.keySet())
        {
            JSONObject jsonObject = latLngJSONObjectHashMap.get(coordinate);
            Marker m = latLngMarkerHashMap.get(coordinate);
            markerJSONObjectHashMap.put(m,jsonObject);
        }

        return  markerJSONObjectHashMap;
    }*/

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
