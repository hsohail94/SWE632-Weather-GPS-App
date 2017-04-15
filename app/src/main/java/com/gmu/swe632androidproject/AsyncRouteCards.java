package com.gmu.swe632androidproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by haaris on 4/13/17.
 */

public class AsyncRouteCards extends AsyncTask <String, String, JSONArray>
{
    private ArrayList<NameValuePair> elementPairs;
    private URL jsonURL;
    private ProgressDialog progressDialog;
    private Activity context;
    private RecyclerView rv;

    public AsyncRouteCards (ArrayList<NameValuePair> elementPairs, URL jsonURL, Activity context, RecyclerView rv)
    {
        this.elementPairs = elementPairs;
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
        //If there is no data passed, return null
        if (params.length == 0)
        {
            return null;
        }

        //prepare request
        String sourceLocation = params[0];
        String destinationLocation = params[1];
        URL jsonDataURL = NetworkMethods.buildCardsJSONURL(sourceLocation, destinationLocation);

        //Try getting a JSON data response from our Maps API Call
        try
        {
            //Get our JSON data from Maps, and transform it into a JSONArray for parsing purposes
            String jsonDataResponse = NetworkMethods.getResponseFromHttpUrl(jsonDataURL);
            JSONObject jsonDataObject = new JSONObject(jsonDataResponse);

        }
        catch (IOException e)
        {

        }
        catch (JSONException e)
        {

        }

        return null;
    }
}
