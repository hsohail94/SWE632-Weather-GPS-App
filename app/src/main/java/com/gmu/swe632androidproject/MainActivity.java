package com.gmu.swe632androidproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity
{
    private EditText sourceText;
    private EditText destinationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //TODO #1: Need to setup a menu with user preferences (add menu/preferences.xml to res, and see how to go from there)
    //TODO #2: Override onCreateOptionsMenu and onOptionsItemSelected in this class for handling the Menu

    /**
     * This method is responsible for passing along the user-provided data to any
     * subsequent activities in order to retrieve the necessary data from the Google Maps API.
     *
     * @param view: representing the current activity
     */
    public void submitSrcDest (View view)
    {
        sourceText = (EditText) findViewById(R.id.source_text);
        destinationText = (EditText) findViewById(R.id.destination_text);

        //Pass data to new class, and start it when button is clicked
        Intent i = new Intent(MainActivity.this, DisplayRouteCardsActivity.class);
        i.putExtra("source location", sourceText.getText().toString());
        i.putExtra("destination address", destinationText.getText().toString());
        startActivity(i);
    }
}
