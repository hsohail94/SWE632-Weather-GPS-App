package com.gmu.swe632androidproject;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DisplayRouteCardsActivity extends AppCompatActivity
{
    private RecyclerView mCardsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routes_recyclerview);

        //Retrieve user data from MainActivity
        Bundle extras = getIntent().getExtras();
        String userSource = extras.getString("source location");
        String userDestination = extras.getString("destination address");

        //Build our Maps API URL
        URL mapsJSONUrl = NetworkMethods.buildCardsJSONURL(userSource, userDestination);

        //Set our RecyclerView object
        mCardsRV = (RecyclerView) findViewById(R.id.routes_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCardsRV.setLayoutManager(layoutManager);
        mCardsRV.setHasFixedSize(true);
        mCardsRV.setVisibility(View.VISIBLE);

        AsyncRouteCards asyncTask = new AsyncRouteCards(mapsJSONUrl, this, mCardsRV);
        asyncTask.execute();
    }
}
