package com.gmu.swe632androidproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ScrollingTabContainerView;
import android.util.Log;

import org.apache.http.NameValuePair;
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
    private URL jsonURL;
    private ProgressDialog progressDialog;
    private Activity context;
    private RecyclerView rv;

    public AsyncRouteCards (URL jsonURL, Activity context, RecyclerView rv)
    {
        //this.elementPairs = elementPairs;
        this.jsonURL = jsonURL;
        this.context = context;
        this.rv = rv;
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
        //If there is no data passed, return null
        if (params.length == 0)
        {
            return null;
        }

        //Try getting a JSON data response from our Maps API Call
        try
        {
            //Get our JSON data from Maps, and transform it into a JSONArray for parsing purposes
            String jsonDataResponse = NetworkMethods.getResponseFromHttpUrl(jsonURL);
            jsonDataObject = new JSONObject(jsonDataResponse);

            Log.d("JSON Data returned: ", jsonDataResponse);

            if (jsonDataObject.has("routes"))
                routesJsonArray = jsonDataObject.getJSONArray("routes"); //store routes in this object
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

    private void showJSONRoutesInRecyclerView (JSONArray jsonArray) throws JSONException
    {
        JSONArray routeResults = jsonArray; //defensive programming!
        final ArrayList<HashMap<String,String>> jsonResultsList = new ArrayList<HashMap<String, String>>(); //build a mapping of our JSON objects in List format

        //iterate through JSONArray and build our ArrayList of mappings
        for (int i = 0; i < routeResults.length(); i++)
        {
            JSONObject resultObj = (JSONObject) routeResults.getJSONObject(i);
            HashMap<String,String> map = new HashMap<String,String>();

            //we only need our route legs, overall distance, and overall duration
            map.put("legs", resultObj.getJSONArray("legs").toString());
            map.put("totalDistance", resultObj.getJSONArray("legs").getJSONObject(0).getString("distance"));
            map.put("totalDuration", resultObj.getJSONArray("legs").getJSONObject(1).getString("duration"));

            //add data to hashmap, then log it
            jsonResultsList.add(i, map);
            Log.d("JSON data" + i + ": ", resultObj.getString("legs") + " " + resultObj.getJSONArray("legs").getJSONObject(0).getString("distance")
                    + ", " + resultObj.getJSONArray("legs").getJSONObject(1).getString("duration"));

        }

        //call a method for initializing our routes to be displayed
        //then use our new arraylist to create our RecyclerView's adapter
        ArrayList<Route> allRoutes = initializeRoutes(jsonResultsList);
        RoutesRecylerViewAdapter adapter = new RoutesRecylerViewAdapter(allRoutes);
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
