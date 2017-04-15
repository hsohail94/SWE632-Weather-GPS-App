package com.gmu.swe632androidproject;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by haaris on 4/12/17.
 */

public final class NetworkMethods
{
    private static final String TAG = NetworkMethods.class.getSimpleName(); //will be used for logcat print statements

    //This is the Google API KEY for things like Maps and whatnot
    private static final String API_KEY = System.getenv("GOOGLE_API_KEY");

    //URL for getting only JSON data for a particular route
    private static final String JSON_ROUTE_URL = "https://maps.googleapis.com/maps/api/directions/json";

    //Parameter for units; choice between metric and imperial in user preferences
    private static final String UNITS_PARAM = "units";

    //Paramater for source location
    private static final String SOURCE_PARAM = "origin";

    //Parameter for destination
    private static final String DEST_PARAM = "destination";

    //By default, units are imperial, since we're building this in America. But, they can be changed to metric too.
    //A method will be written for this
    private static String units = "imperial";

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
                            .appendQueryParameter(DEST_PARAM, destinationString).appendQueryParameter("key", API_KEY).build();

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

}
