package com.gmu.swe632androidproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;

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
        return null;
    }
}
