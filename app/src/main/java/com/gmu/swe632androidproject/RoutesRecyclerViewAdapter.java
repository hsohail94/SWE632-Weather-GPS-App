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
public class RoutesRecyclerViewAdapter extends RecyclerView.Adapter<RoutesRecyclerViewAdapter.RouteViewHolder>
{
    //Variable to be used for handling click events
    private final RoutesRecyclerViewAdapterOnClickHander onClickHandler;

    //ViewHolder extension; to be used for inflating adapter with route cards
    public class RouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private CardView routeCardView;
        private TextView distanceAndTime;

        RouteViewHolder(View routeView)
        {
            super(routeView);
            routeCardView = (CardView) itemView.findViewById(R.id.routes_cardview);
            distanceAndTime = (TextView) itemView.findViewById(R.id.distance_and_time);
            routeView.setOnClickListener(this);
        }

        public void onClick (View view)
        {
            onClickHandler.onClickCardItem(getAdapterPosition());
            //onClickHandler.onClickCardItem();
        }

    }

    private ArrayList<Route> routes; //the list of routes that will be used in the inflated cardview

    public RoutesRecyclerViewAdapter(ArrayList<Route> routes, RoutesRecyclerViewAdapterOnClickHander onClickHandler)
    {
        this.routes = routes;
        this.onClickHandler = onClickHandler;
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
        int actualPos = position + 1;
        holder.distanceAndTime.setText("Route " + actualPos + ": " + routes.get(position).getDistance() + ", " + routes.get(position).getTotalTime());
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
