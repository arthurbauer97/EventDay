package com.example.eventdayfinal.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.R;

import java.util.ArrayList;

public class PlacesAdapter extends BaseAdapter {

    private ArrayList<Event> listOfPlaces;
    private LayoutInflater inflater;

    public PlacesAdapter(Context context, ArrayList<Event> places) {
        listOfPlaces = places;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listOfPlaces.size();
    }

    @Override
    public Object getItem(int position) {
        return listOfPlaces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.listview_event_layout, parent, false);

        TextView placeName = view.findViewById(R.id.place_name_listview);
        TextView placeAdress = view.findViewById(R.id.place_adress_listview);

        placeName.setText(listOfPlaces.get(position).getEventName());
        placeAdress.setText(listOfPlaces.get(position).getAdress());

        return view;


    }
}
