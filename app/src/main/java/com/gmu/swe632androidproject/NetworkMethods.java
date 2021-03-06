package com.gmu.swe632androidproject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * Created by haaris on 4/12/17.
 */

public final class NetworkMethods
{
    private static final String TAG = NetworkMethods.class.getSimpleName(); //will be used for logcat print statements

    //This is the Google API KEY for things like Maps and whatnot
    //private static final String API_KEY = System.getenv("GOOGLE_API_KEY");
    private static final String API_KEY = "AIzaSyBVGhILjQgCPzE4R_zelzcZCiGrOJ__SFM";

    //This is our API KEY for the OpenWeatherMaps API
    private static final String OWM_API_KEY = "728457692c1769f25424b3fe8f71b018";

    //URL for getting only JSON data for a particular route
    private static final String JSON_ROUTE_URL = "https://maps.googleapis.com/maps/api/directions/json";

    //URL for getting JSON data from the OpenWeatherMaps API
    private static final String JSON_WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast";

    //Parameter for units; choice between metric and imperial in user preferences
    private static final String UNITS_PARAM = "units";

    //Paramater for source location
    private static final String SOURCE_PARAM = "origin";

    //Parameter for destination
    private static final String DEST_PARAM = "destination";

    //If you don't set this parameter in the HTTP request, you will not get multiple routes
    private static final String ALTERNATIVE_ROUTES = "alternatives";
    private static final String ALTERNATIVE_CHOICES = "true"; //set this to true, by default

    //By default, units are imperial, since we're building this in America. But, they can be changed to metric too.
    //A method will be written for this
    private static String units = "imperial";
    public static int imperialOrMetric;

    /**
     * Builder method used to build the URL that will request JSON data via the Google Maps API.
     * This API call only returns JSON data, which will be parsed and displayed in the CardView/RecyclerView
     * combo that is DisplayRouteCardsActivity.
     *
     * @param sourceString: string representing source location
     * @param destinationString: string representing destination location
     * @return a URL object that represents the API call we will make a request to
     */
    public static URL buildCardsJSONURL (String sourceString, String destinationString)
    {
        Uri builtJsonUri = Uri.parse(JSON_ROUTE_URL).buildUpon().appendQueryParameter(UNITS_PARAM, units).appendQueryParameter(SOURCE_PARAM, sourceString)
                            .appendQueryParameter(DEST_PARAM, destinationString).appendQueryParameter(ALTERNATIVE_ROUTES, ALTERNATIVE_CHOICES).appendQueryParameter("key", API_KEY).build();

        URL jsonURL = null;
        try
        {
            jsonURL = new URL(builtJsonUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "Built JSON URL = " + jsonURL);

        return jsonURL;
    }

    /**
     * Another URL builder method for the Maps Directions API, except this one is geared towards displays for our MapAcitivity.
     * Hence, our use of lat-long pairs instead of user-provided addresses.
     *
     * @param originLatitude: latitude of the origin address
     * @param originLongitude: longitude of the origin address
     * @param destLatitude: latitude of the destination address
     * @param destLongitude: longitude of the destination address
     * @return a URL to request Directions from the Maps API
     */
    public static URL buildMapJSONURL (Double originLatitude, Double originLongitude, Double destLatitude, Double destLongitude)
    {
        Uri builtMapsUri = Uri.parse(JSON_ROUTE_URL).buildUpon().appendQueryParameter(UNITS_PARAM, units).appendQueryParameter(SOURCE_PARAM, originLatitude + "," + originLongitude)
                                .appendQueryParameter(DEST_PARAM, destLatitude + "," + destLongitude).appendQueryParameter(ALTERNATIVE_ROUTES, ALTERNATIVE_CHOICES)
                                .appendQueryParameter("sensor", "false").appendQueryParameter("key", API_KEY).build();

        URL jsonURL = null;
        try
        {
            jsonURL = new URL(builtMapsUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "Built MAPS URL = " + jsonURL);

        return jsonURL;
    }

    public static URL buildWeatherRequestURL (Double latitude, Double longitude)
    {
        Uri builtWeatherUri = Uri.parse(JSON_WEATHER_URL).buildUpon().appendQueryParameter("lat", Double.toString(latitude))
                                .appendQueryParameter("lon",Double.toString(longitude)).appendQueryParameter(UNITS_PARAM, units)
                                .appendQueryParameter("mode", "json").appendQueryParameter("APPID", OWM_API_KEY).build();
        URL jsonWeatherURL = null;
        try
        {
            jsonWeatherURL = new URL (builtWeatherUri.toString());
        }
        catch (MalformedURLException e)
        {
            Log.v("JSON Weather URL Issue", e.getMessage());
        }
        Log.v(TAG, "Built OWM API URL = " + jsonWeatherURL);

        return jsonWeatherURL;
    }

    /**
     * This method is used to capture the JSON response from an HTTP request.
     * This is a generic method that will be used frequently by this application
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException
    {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try
        {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput)
            {
                return scanner.next();
            }
            else
            {
                return null;
            }
        }
        finally
        {
            urlConnection.disconnect();
        }
    }

    /**
     * Method for converting a user provided address, in String form, into latitude and longitude coordinates.
     * Will need this for drawing routes in Google Maps.
     *
     * @param context
     * @param userAddress
     * @return
     */
    public static LatLng getLatitudeLongitudeFromUserString (Context context, String userAddress)
    {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses;
        LatLng point1 = null;

        try
        {
            addresses = geocoder.getFromLocationName(userAddress, 5); //running this could throw an IOException
            if (addresses == null)
                return null;
            Address location = addresses.get(0);
            point1 = new LatLng(location.getLatitude(), location.getLongitude());
        }
        catch (IOException e)
        {
            Log.e("LatLong error: ", e.getMessage());
        }
        return point1;
    }

    /**
     * Method for setting units of choice: imperial or metric (mi & F vs km & C)
     *
     * @param userChoice - will be set in a Navigation Menu
     */
    public static void imperialOrMetric (String userChoice)
    {
        if (userChoice.equalsIgnoreCase("metric")) {
            units = "metric";
            imperialOrMetric=1;
        }
        else if (userChoice.equalsIgnoreCase("imperial")) {
            units = "imperial";
            imperialOrMetric = 0;
        }
    }

    /**
     * A method for adding time to a calendar object. Basically: convert the additional time you want in minutes,
     * then pass that along with your Calendar object, and return the resulting calendar object.
     *
     * @return
     */
    public static Calendar getCalendarDateTimeAfterMinutesAdd(Calendar c, int minutes)
    {
        //Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //calendar.add(Calendar.MINUTE, minutes);
        c.add(Calendar.MINUTE,minutes);
        return c;
    }

    /**
     * Create a calendar object from a String parameter according to the format parameter.
     *
     * @param dateString
     * @param format
     * @return A new calendar object based on the dateString parameter
     */
    public static Calendar getCalendarObjFromString(String dateString, String format) throws ParseException
    {
        Calendar cal = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = sdf.parse(dateString);
        cal = sdf.getCalendar();

        return cal;
    }

    /**
     * This method will be used by the AsyncRouteCards class to determine the most common type of weather
     * for a given route, using a latlng->jsonobject argument.
     * @return
     */
    public static String getMostCommonTypeOfWeather(HashMap<LatLng,String> latLngStringHashMap)
    {
        HashMap<String,Integer> weatherDescriptionFrequencyMap = new HashMap<String, Integer>();
        int i = 0;
        for (LatLng latLng:  latLngStringHashMap.keySet())
        {

                String weatherMainDescription = latLngStringHashMap.get(latLng);
                if (!weatherDescriptionFrequencyMap.containsKey(weatherMainDescription))
                {
                    weatherDescriptionFrequencyMap.put(weatherMainDescription, new Integer(0));
                }
                else
                {
                    Integer freq = weatherDescriptionFrequencyMap.get(weatherMainDescription);
                    Integer newFreq = new Integer(freq.intValue()+1);
                    weatherDescriptionFrequencyMap.put(weatherMainDescription,newFreq);
                }

        }

        Integer integer = null;
        for (String s: weatherDescriptionFrequencyMap.keySet())
        {
            Integer freq = weatherDescriptionFrequencyMap.get(s);
            if (integer == null || freq.compareTo(integer) > 0)
            {
                integer = freq;
            }
        }
        String mostCommon = getKeyByValue(weatherDescriptionFrequencyMap, integer);
        return mostCommon;

    }

    /**
     * A generic method for finding a key in a map given the value it's mapped to.
     *
     * @param map
     * @param value
     * @param <T>
     * @param <E>
     * @return
     */
    public static <T, E> T getKeyByValue(Map<T, E> map, E value)
    {
        for (Map.Entry<T, E> entry : map.entrySet())
        {
            if (value.equals(entry.getValue()))
            {
                return entry.getKey();
            }
        }
        return null;
    }

}
