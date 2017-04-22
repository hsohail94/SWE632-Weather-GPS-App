package com.gmu.swe632androidproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

//import com.github.pwittchen.weathericonview.WeatherIconView;

import org.json.JSONArray;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DisplayRouteCardsActivity extends AppCompatActivity implements RoutesRecyclerViewAdapterOnClickHander
{
    private RecyclerView mCardsRV;
    private String userSource;
    private String userDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routes_recyclerview);

        //Retrieve user data from MainActivity
        Bundle extras = getIntent().getExtras();
        userSource = extras.getString("source location");
        userDestination = extras.getString("destination address");

        //Build our Maps API URL
        URL mapsJSONUrl = NetworkMethods.buildCardsJSONURL(userSource, userDestination);

        //Set our RecyclerView object
        mCardsRV = (RecyclerView) findViewById(R.id.routes_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCardsRV.setLayoutManager(layoutManager);
        mCardsRV.setHasFixedSize(true);
        mCardsRV.setVisibility(View.VISIBLE);

        RoutesRecyclerViewAdapterOnClickHander onClickHander = this;
        AsyncRouteCards asyncTask = new AsyncRouteCards(mapsJSONUrl, this, mCardsRV, userSource, userDestination, onClickHander);
        asyncTask.execute();
    }

    @Override
    public void onClickCardItem(int position)
    //public void onClickCardItem()
    {
        Intent i = new Intent(DisplayRouteCardsActivity.this, SingleRouteMapActivity.class);
        //int focusPosition = mCardsRV.getChildAdapterPosition(mCardsRV.getFocusedChild());
        //int focusPosition = 1;
        i.putExtra("source location", userSource);
        i.putExtra("destination address", userDestination);
        //i.putExtra("routes JSON array", routesJSONArray.toString());
        i.putExtra("route number", position);
        try {
            startActivity(i);
        }
        catch (Exception e)
        {
            Log.v("Something went wrong", e.getMessage());
        }
    }

}
