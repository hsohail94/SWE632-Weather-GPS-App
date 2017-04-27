package com.gmu.swe632androidproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

//import com.github.pwittchen.weathericonview.WeatherIconView;

import com.google.android.gms.maps.model.LatLng;

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
 * Created by haaris on 4/13/17.
 */

public class AsyncRouteCards extends AsyncTask <String, String, JSONArray>
{
    //private ArrayList<NameValuePair> elementPairs;
    private String sourceAddress;
    private String destAddress;
    private URL jsonURL;
    private URL weatherAPIURL;

    private ProgressDialog progressDialog;
    private DisplayRouteCardsActivity context;
    private RecyclerView rv;
    private RoutesRecyclerViewAdapterOnClickHander onClickHander;

    private List<HashMap<LatLng,JSONObject>> routeMarkerToWeatherForecastMapList;
    private List<HashMap<LatLng,String>> weatherSummaryFrequencyMapList;
    private HashMap<JSONObject,JSONArray> oneRouteToWeatherArrayMap;
    private String weatherDateFormat = "yyyy-MM-dd HH:mm:ss";

    public AsyncRouteCards (URL jsonURL, DisplayRouteCardsActivity context, RecyclerView rv, String sourceAddress, String destAddress, RoutesRecyclerViewAdapterOnClickHander onClickHander)
    {
        //this.elementPairs = elementPairs;
        this.jsonURL = jsonURL;
        this.context = context;
        this.rv = rv;
        this.sourceAddress = sourceAddress;
        this.destAddress = destAddress;
        this.onClickHander = onClickHander;
    }

    protected void onPreExecute()
    {
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("Obtaining routes. Standby...");
        progressDialog.show();
    }

    protected JSONArray doInBackground (String... params)
    {
        JSONObject jsonDataObject = null;
        JSONArray routesJsonArray = null;

        JSONObject weatherJsonDataObj = null;
        JSONArray weatherJsonArray = null;

        routeMarkerToWeatherForecastMapList = new ArrayList<HashMap<LatLng, JSONObject>>();
        weatherSummaryFrequencyMapList = new ArrayList<HashMap<LatLng, String>>();
        oneRouteToWeatherArrayMap = new HashMap<JSONObject, JSONArray>();

        //We will not have the current time across the route, so we will initialize the calendar object,
        //then keep incrementing it by adding the durations below for every step.
        //This way, our time persists.
        Calendar timeAtStep = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        int overallRunningDurationSum = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        //Try getting a JSON data response from our Maps API Call
        try
        {
            //Get our JSON data from Maps, and transform it into a JSONArray for parsing purposes
            String jsonDataResponse = NetworkMethods.getResponseFromHttpUrl(jsonURL);
            jsonDataObject = new JSONObject(jsonDataResponse);

            Log.d("JSON Data returned: ", jsonDataResponse);

            if (jsonDataObject.has("routes"))
            {
                routesJsonArray = jsonDataObject.getJSONArray("routes"); //store routes in this object
                Log.d("Json routes returned: ", routesJsonArray.toString());

                for (int i = 0; i < routesJsonArray.length(); i++)
                {
                    HashMap<LatLng,JSONObject> latLngJSONObjectHashMap = new HashMap<LatLng, JSONObject>();
                    HashMap<LatLng,String> pointsToWeatherSummaryMap = new HashMap<LatLng, String>();

                    int runningDurationSum = 0;

                    JSONObject legsOfSpecificRoute = routesJsonArray.getJSONObject(i).getJSONArray("legs").getJSONObject(0);
                    JSONArray stepsOfLegsOfRoute = legsOfSpecificRoute.getJSONArray("steps");

                    int minutesAtStep = 0;
                    String[] timeStringSplit;

                    for (int j = 0; j < stepsOfLegsOfRoute.length(); j++)
                    {
                        JSONObject individualStep = stepsOfLegsOfRoute.getJSONObject(j);
                        String timeOfStep = individualStep.getJSONObject("duration").getString("text");
                        Log.v("AsyncRouteCardsStepTime", timeOfStep);

                        if (timeOfStep.contains("hour") && timeOfStep.contains("min"))
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

                        JSONObject currentLatLngObj = individualStep.getJSONObject("end_location");
                        LatLng objectToSend = new LatLng(currentLatLngObj.getDouble("lat"), currentLatLngObj.getDouble("lng"));
                        //routeWeatherMarkerList.add(objectToSend);

                        URL weatherAPIUrl = NetworkMethods.buildWeatherRequestURL(objectToSend.latitude, objectToSend.longitude);
                        String jsonWeatherDataResponse = NetworkMethods.getResponseFromHttpUrl(weatherAPIUrl);
                        Log.v("RouteCard Weather Data", jsonWeatherDataResponse);

                        weatherJsonDataObj = new JSONObject(jsonWeatherDataResponse);
                        weatherJsonArray = weatherJsonDataObj.getJSONArray("list");

                        if (weatherJsonArray != null)
                        {
                            //String placeInMarker = weatherJsonDataObj.getJSONObject("city").getString("name") + ", " +
                                    //weatherJsonDataObj.getJSONObject("city").getString("country");
                            Log.v("Weather List at step", weatherJsonArray.toString());
                            //Log.v("Current place", placeInMarker);
                            //latLngToPlaceNameMap.put(objectToSend, placeInMarker);

                            //When using the free version of the OpenWeatherMap API, we are unfortunately limited to 3 hour windows
                            //of weather forecasts over a span of 5 days, so we need to find the next interval where we will get our weather
                            //forecast from. This interval will be based on a difference comparison.
                            for (int n = 0; n < weatherJsonArray.length() - 1; n++)
                            {
                                JSONObject currentWeatherObj = weatherJsonArray.getJSONObject(n);
                                Log.v("Weather Object " + n, currentWeatherObj.toString());
                                int k = n+1;
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
                                        //weatherJsonObjForMarkersList.add(weatherThreeHoursFromNowObj);
                                        latLngJSONObjectHashMap.put(objectToSend, weatherThreeHoursFromNowObj);
                                        pointsToWeatherSummaryMap.put(objectToSend, weatherThreeHoursFromNowObj.getJSONArray("weather").getJSONObject(0).getString("main"));
                                    }
                                    else if (differenceOne < differenceTwo)
                                    {
                                        //weatherJsonObjForMarkersList.add(currentWeatherObj);
                                        latLngJSONObjectHashMap.put(objectToSend, currentWeatherObj);
                                        pointsToWeatherSummaryMap.put(objectToSend, currentWeatherObj.getJSONArray("weather").getJSONObject(0).getString("main"));
                                    }
                                    //If the difference between the current time and the UTC time given by the Weather API
                                    //is the same as the difference in the time in the next prediction, and the current UTC time,
                                    //then just add the current forecast object. It doesn't really matter which one, anyways.
                                    else
                                    {
                                        //weatherJsonObjForMarkersList.add(weatherThreeHoursFromNowObj);
                                        latLngJSONObjectHashMap.put(objectToSend, weatherThreeHoursFromNowObj);
                                        pointsToWeatherSummaryMap.put(objectToSend, weatherThreeHoursFromNowObj.getJSONArray("weather").getJSONObject(0).getString("main"));
                                    }
                                    break;
                                }
                                else if (differenceOne <= 0.00)
                                {
                                    //weatherJsonObjForMarkersList.add(currentWeatherObj);
                                    latLngJSONObjectHashMap.put(objectToSend, currentWeatherObj);
                                    pointsToWeatherSummaryMap.put(objectToSend, currentWeatherObj.getJSONArray("weather").getJSONObject(0).getString("main"));
                                }
                            }
                        }

                    }

                    //Build maps for each route. The data for each route will be displayed as an aggregate in each card.
                    routeMarkerToWeatherForecastMapList.add(i, latLngJSONObjectHashMap);
                    weatherSummaryFrequencyMapList.add(i, pointsToWeatherSummaryMap);
                }

            }
        }
        catch (IOException e)
        {
            Log.e("IOError: ", e.getMessage());
        }
        catch (JSONException e)
        {
            Log.e("JSONError: ", e.getMessage());
        }
        catch (ParseException e)
        {
            Log.e("ParseError: ", e.getMessage());
        }

        return routesJsonArray;
    }

    protected void onPostExecute (JSONArray jsonArray)
    {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        if (jsonArray != null)
        {
            try
            {
                this.showJSONRoutesInRecyclerView(jsonArray);
            }
            catch (JSONException e)
            {
                Log.e("JSON error: ", e.getMessage());
            }
        }
    }

    /**
     * A method called by onPostExecute. This is what will actually take care of displaying the returned Maps results
     * in our RecyclerView
     *
     * @param jsonArray
     * @throws JSONException
     */
    private void showJSONRoutesInRecyclerView (JSONArray jsonArray) throws JSONException
    {
        /*WeatherIconView weatherIconView;
        weatherIconView = (WeatherIconView) this.context.findViewById(R.id.average_weather_icon);
        weatherIconView.setIconResource(this.context.getString(R.string.wi_strong_wind));
        weatherIconView.setIconSize(100);
        weatherIconView.setIconColor(Color.BLACK);*/

        JSONArray routeResults = jsonArray; //defensive programming!
        final ArrayList<HashMap<String,String>> jsonResultsList = new ArrayList<HashMap<String, String>>(); //build a mapping of our JSON objects in List format
        final ArrayList<String> mostCommonWeatherDescriptionsList = new ArrayList<String>();

        //iterate through JSONArray and build our ArrayList of mappings
        for (int i = 0; i < routeResults.length(); i++)
        {
            JSONObject resultObj = (JSONObject) routeResults.getJSONObject(i);
            HashMap<String,String> map = new HashMap<String,String>();
            HashMap<LatLng,String> latLngStringMap = weatherSummaryFrequencyMapList.get(0);
            String mostCommonWeatherForRoute = NetworkMethods.getMostCommonTypeOfWeather(latLngStringMap);

            //we only need our route legs, overall distance, and overall duration
            map.put("legs", resultObj.getJSONArray("legs").toString());
            map.put("totalDistance", resultObj.getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text"));
            map.put("totalDuration", resultObj.getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text"));

            //add data to hashmap, then log it
            jsonResultsList.add(i, map);
            mostCommonWeatherDescriptionsList.add(i, mostCommonWeatherForRoute);
            Log.v("JSON data: legs", resultObj.getJSONArray("legs").toString());
            Log.v("JSON data: distance", resultObj.getJSONArray("legs").getJSONObject(0).getString("distance"));
            Log.v("JSON data: duration", resultObj.getJSONArray("legs").getJSONObject(0).getString("duration"));

        }

        //call a method for initializing our routes to be displayed
        //then use our new arraylist to create our RecyclerView's adapter
        ArrayList<Route> allRoutes = initializeRoutes(jsonResultsList, mostCommonWeatherDescriptionsList);
        RoutesRecyclerViewAdapter adapter = new RoutesRecyclerViewAdapter(allRoutes, this.onClickHander);
        rv.setAdapter(adapter);

        //pass routes JSON array back to the displayroutes activity; this will come in very handy
        //this.context.retainRoutesJSONArray(routeResults);
    }

    /**
     * Creates an ArrayList of Routes that will be passed to the adapter for our RecyclerView
     *
     * @param routesMapList
     * @return ArrayList<Route>
     */
    private ArrayList<Route> initializeRoutes(ArrayList<HashMap<String,String>> routesMapList, ArrayList<String> mostCommonWeatherList)
    {
        ArrayList<Route> cardRoutes = new ArrayList<Route>();

        int i = 0;

        for (HashMap<String,String> jsonRouteMap: routesMapList)
        {
            Route route = new Route(jsonRouteMap.get("totalDistance"),jsonRouteMap.get("totalDuration"),mostCommonWeatherList.get(i));
            cardRoutes.add(route);
            i++;
        }

        return cardRoutes;
    }
}
