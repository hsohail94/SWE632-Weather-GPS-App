package com.gmu.swe632androidproject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SingleRouteMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList mapMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_route_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        Log.v("MAP ACTIVITY: ", "setting map now");
        mMap = googleMap;

        //*Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        Bundle extras = getIntent().getExtras();
        String userSource = extras.getString("source location");
        String userDestination = extras.getString("destination address");
        //String routesJsonString = extras.getString("routes JSON array");
        int routeNumber = extras.getInt("route number");
        try
        {
            //Setting the markers for our origin and destination on the map
            LatLng origin = NetworkMethods.getLatitudeLongitudeFromUserString(this, userSource);
            LatLng destination = NetworkMethods.getLatitudeLongitudeFromUserString(this, userDestination);
            mMap.addMarker(new MarkerOptions().position(origin).title("Origin"));
            mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
            //mMap.setOnInfoWindowClickListener(this);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));

            URL buildMapsRouteURL = NetworkMethods.buildMapJSONURL(origin.latitude, origin.longitude,
                                    destination.latitude, destination.longitude);
            AsyncDrawMapTask asyncDrawMapTask = new AsyncDrawMapTask(routeNumber, buildMapsRouteURL, this, mMap);
            asyncDrawMapTask.execute();
        }
        catch (Exception e)
        {
            Log.e("JSON Exception: ", e.getMessage());
        }
    }

    /*
    @Override
    public void onInfoWindowClick(Marker marker)
    {

    }*/
}
