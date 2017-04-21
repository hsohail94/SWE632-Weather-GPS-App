package com.gmu.swe632androidproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.pwittchen.weathericonview.WeatherIconView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by haaris on 4/13/17.
 */

public class AsyncRouteCards extends AsyncTask <String, String, JSONArray>
{
    //private ArrayList<NameValuePair> elementPairs;
    private String sourceAddress;
    private String destAddress;
    private URL jsonURL;
    private ProgressDialog progressDialog;
    private Activity context;
    private RecyclerView rv;
    private RoutesRecyclerViewAdapterOnClickHander onClickHander;

    public AsyncRouteCards (URL jsonURL, Activity context, RecyclerView rv, String sourceAddress, String destAddress, RoutesRecyclerViewAdapterOnClickHander onClickHander)
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

        //Try getting a JSON data response from our Maps API Call
        try
        {
            //Get our JSON data from Maps, and transform it into a JSONArray for parsing purposes
            String jsonDataResponse = NetworkMethods.getResponseFromHttpUrl(jsonURL);
            jsonDataObject = new JSONObject(jsonDataResponse);

            Log.d("JSON Data returned: ", jsonDataResponse);

            if (jsonDataObject.has("routes")) {
                routesJsonArray = jsonDataObject.getJSONArray("routes"); //store routes in this object
                Log.d("Json routes returned: ", routesJsonArray.toString());
            }
        }
        catch (IOException e)
        {
            Log.e("Error: ", e.getMessage());
        }
        catch (JSONException e)
        {
            Log.e("Error: ", e.getMessage());
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

        //iterate through JSONArray and build our ArrayList of mappings
        for (int i = 0; i < routeResults.length(); i++)
        {
            JSONObject resultObj = (JSONObject) routeResults.getJSONObject(i);
            HashMap<String,String> map = new HashMap<String,String>();
            //we only need our route legs, overall distance, and overall duration
            map.put("legs", resultObj.getJSONArray("legs").toString());
            map.put("totalDistance", resultObj.getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text"));
            map.put("totalDuration", resultObj.getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text"));

            //add data to hashmap, then log it
            jsonResultsList.add(i, map);
            Log.v("JSON data: legs", resultObj.getJSONArray("legs").toString());
            Log.v("JSON data: distance", resultObj.getJSONArray("legs").getJSONObject(0).getString("distance"));
            Log.v("JSON data: duration", resultObj.getJSONArray("legs").getJSONObject(0).getString("duration"));

        }

        //call a method for initializing our routes to be displayed
        //then use our new arraylist to create our RecyclerView's adapter
        ArrayList<Route> allRoutes = initializeRoutes(jsonResultsList);
        RoutesRecyclerViewAdapter adapter = new RoutesRecyclerViewAdapter(allRoutes, this.onClickHander);
        rv.setAdapter(adapter);
    }

    /**
     * Creates an ArrayList of Routes that will be passed to the adapter for our RecyclerView
     *
     * @param routesMapList
     * @return ArrayList<Route>
     */
    private ArrayList<Route> initializeRoutes(ArrayList<HashMap<String,String>> routesMapList)
    {
        ArrayList<Route> cardRoutes = new ArrayList<Route>();

        for (HashMap<String,String> jsonRouteMap: routesMapList)
        {
            Route route = new Route(jsonRouteMap.get("totalDistance"),jsonRouteMap.get("totalDuration"));
            cardRoutes.add(route);
        }

        return cardRoutes;
    }
}
