package com.gmu.swe632androidproject;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by haaris on 4/15/17.
 */

//Adapter class for the RecyclerView that shows our routes in cards
public class RoutesRecylerViewAdapter extends RecyclerView.Adapter<RoutesRecylerViewAdapter.RouteViewHolder>
{
    //ViewHolder extension; to be used for inflating adapter with route cards
    public static class RouteViewHolder extends RecyclerView.ViewHolder
    {
        private CardView routeCardView;
        private TextView distanceAndTime;

        RouteViewHolder(View routeView)
        {
            super(routeView);
            routeCardView = (CardView) itemView.findViewById(R.id.routes_cardview);
            distanceAndTime = (TextView) itemView.findViewById(R.id.distance_and_time);
        }
    }

    private ArrayList<Route> routes; //the list of routes that will be used in the inflated cardview

    public RoutesRecylerViewAdapter (ArrayList<Route> routes)
    {
        this.routes = routes;
    }

    //The actual inflate method for our cardview
    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_display_route_cards, parent, false);
        RouteViewHolder rvh = new RouteViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position)
    {
        holder.distanceAndTime.setText(routes.get(position).getDistance());
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView rv)
    {
        super.onAttachedToRecyclerView(rv);
    }


}
