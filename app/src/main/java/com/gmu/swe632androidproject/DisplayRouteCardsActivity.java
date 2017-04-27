package com.gmu.swe632androidproject;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

//import com.github.pwittchen.weathericonview.WeatherIconView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisplayRouteCardsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, RoutesRecyclerViewAdapterOnClickHander
{
    private RecyclerView mCardsRV;
    private String userSource;
    private String userDestination;
    private android.widget.SearchView sourceAndDestSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routes_recyclerview);

        sourceAndDestSearch = (android.widget.SearchView) findViewById(R.id.source_and_destination_search);
        setupSearchViewForVoice();

        //Retrieve user data from MainActivity
        Bundle extras = getIntent().getExtras();
        userSource = extras.getString("source location");
        userDestination = extras.getString("destination address");

        //onNewIntent(getIntent()); //this will only happen if the intent is a search one, and not one with manual input from the user
        if (!isIntentASearchIntent(getIntent()))
            displayRouteCards(userSource, userDestination);
        else
            onNewIntent(getIntent());
    }

    private void setupSearchViewForVoice()
    {
        sourceAndDestSearch = (SearchView) findViewById(R.id.source_and_destination_search);

        /*
        sourceAndDestSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                String[] userSourceAndDestination = s.split("and");
                String source = userSourceAndDestination[0];
                String destination = userSourceAndDestination[1];

                //Handling input incase either source or destination fields are empty or null
                if (source.replaceAll("\\s+","").equals("") || destination.replaceAll("\\s+","").equals("") || source == null || destination == null)
                {
                    Toast.makeText(DisplayRouteCardsActivity.this, "Please enter a source, destination, or both!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Pass data to new class, and start it when button is clicked
                    Intent i = new Intent(sourceAndDestSearch.getContext(), DisplayRouteCardsActivity.class);
                    i.setAction(Intent.ACTION_SEARCH);
                    i.putExtra(SearchManager.QUERY, s);
                    startActivity(i);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });*/

        sourceAndDestSearch.setIconifiedByDefault(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            sourceAndDestSearch.setSearchableInfo(info);
        }

        sourceAndDestSearch.setOnQueryTextListener(this);
        sourceAndDestSearch.setFocusable(false);
        sourceAndDestSearch.setFocusableInTouchMode(false);
    }

    protected void onNewIntent (Intent intent)
    {
        String sourceAndDestination = intent.getStringExtra(SearchManager.QUERY);
        Log.v("Search Intent", sourceAndDestination);
        String[] sourceDestArray = sourceAndDestination.split("and");
        String source = sourceDestArray[0];
        userSource = source;
        Log.v("Search Intent Source", source);
        String destination = sourceDestArray[1];
        userDestination = destination;
        Log.v("Search Intent Dest", destination);
        if (source != null && destination != null)
            displayRouteCards(source, destination);
    }

    private boolean isIntentASearchIntent (Intent intent) {
        String action = intent.getAction();
        return (action.equals(Intent.ACTION_SEARCH) || action.equals("com.google.android.gms.actions.SEARCH_ACTION"));
    }

    private void displayRouteCards(String userSource, String userDestination)
    {
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

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }
}
