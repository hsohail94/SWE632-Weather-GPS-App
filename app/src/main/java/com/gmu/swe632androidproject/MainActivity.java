package com.gmu.swe632androidproject;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    //The Views of the data the user will be entering
    private EditText sourceText;
    private EditText destinationText;

    //Variables we will need to setup our navigation drawer for user preferences
    private ListView preferencesListView;
    private ArrayAdapter<String> preferencesListAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout preferencesDrawerLayout;
    private String activityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup our hamburger navigation menu
        preferencesDrawerLayout = (DrawerLayout) findViewById(R.id.main_navigation_drawer);
        activityTitle = getTitle().toString();
        preferencesListView = (ListView) findViewById (R.id.preferences_list);
        addUserPreferenceItems();
        setPreferencesDrawerToggle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sourceText = (EditText) findViewById(R.id.source_text);
        destinationText = (EditText) findViewById(R.id.destination_text);
        /*
        sourceAndDestSearch = (android.widget.SearchView) findViewById(R.id.source_and_destination_search);

        sourceAndDestSearch.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                String[] userSourceAndDestination = s.split("and");
                String source = userSourceAndDestination[0];
                String destination = userSourceAndDestination[1];

                //Handling input incase either source or destination fields are empty or null
                if (source.replaceAll("\\s+","").equals("") || destination.replaceAll("\\s+","").equals(""))
                {
                    Toast.makeText(MainActivity.this, "Please enter a source, destination, or both!", Toast.LENGTH_SHORT).show();
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

    }

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

        //sourceText = (android.widget.SearchView) findViewById(R.id.source_text);
       // destinationText = (android.widget.SearchView) findViewById(R.id.destination_text);

        String source = sourceText.getText().toString();
        String destination = destinationText.getText().toString();

        //String source = sourceText.getQuery().toString();
        //String destination = destinationText.getQuery().toString();

        //Handling input incase either source or destination fields are empty or null
        if (source.replaceAll("\\s+","").equals("") || destination.replaceAll("\\s+","").equals(""))
        {
            Toast.makeText(this, "Please enter a source, destination, or both!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Pass data to new class, and start it when button is clicked
            Intent i = new Intent(MainActivity.this, DisplayRouteCardsActivity.class);
            i.setAction(Intent.ACTION_PICK_ACTIVITY);
            i.putExtra("source location", source);
            i.putExtra("destination address", destination);
            startActivity(i);
        }
    }

    /**
     * This method will set the options for our navigation drawer. Things like changing units from imperial to metric,
     * and vice versa. Will also set onclick behavior for options in our drawer
     */
    private void addUserPreferenceItems()
    {
        final String[] userOptions = {"Select Units", "View License"};
        preferencesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userOptions);
        preferencesListView.setAdapter(preferencesListAdapter);
        //setting onclick functionality for options in navigation drawer
        preferencesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //this is what happens when you choose the select units option
                if (userOptions[position].equals("Select Units"))
                {
                    AlertDialog.Builder unitsDialogBuilder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                    unitsDialogBuilder.setMessage("Select Units of Choice").setCancelable(false)
                            .setPositiveButton("Imperial (miles and Farenheit)", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NetworkMethods.imperialOrMetric("imperial");
                                    Toast.makeText(MainActivity.this, "Units set to Imperial (miles and Farenheit).", Toast.LENGTH_SHORT).show();
                                    dialogInterface.dismiss();
                                }
                            }).setNegativeButton("Metric (km and Celsius)", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NetworkMethods.imperialOrMetric("metric");
                            Toast.makeText(MainActivity.this, "Units set to Metric (km and Celsius).", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog userUnitsDialog = unitsDialogBuilder.create();
                    userUnitsDialog.show();
                }
            }
        });
    }

    /**
     * This method will essentially create a toggle for the hamburger button that opens the navigation drawer.
     * When we open and close the navigation drawer, we also want to have additional behavior alongside it,
     * such as changing the current activity title
     */
    private void setPreferencesDrawerToggle()
    {
        mDrawerToggle = new ActionBarDrawerToggle(this, preferencesDrawerLayout, R.string.open_preferences_drawer,
                                                    R.string.close_preferences_drawer) {
            public void onDrawerOpened (View drawer)
            {
                super.onDrawerOpened(drawer);
                getSupportActionBar().setTitle("User Options");
                //invalidateOptionsMenu();
            }

            public void onDrawerClosed (View drawer)
            {
                super.onDrawerClosed(drawer);
                getSupportActionBar().setTitle(activityTitle);
                //invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        preferencesDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.user_options)
            return true;

        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

}
