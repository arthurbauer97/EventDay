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

public class EventsAdapter extends BaseAdapter {

    private ArrayList<Event> listOfEvents;
    private LayoutInflater inflater;

    public EventsAdapter(Context context, ArrayList<Event> events) {
        listOfEvents = events;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listOfEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return listOfEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.listview_event, parent, false);

        TextView eventName = view.findViewById(R.id.place_name_listview);
        TextView eventAdress = view.findViewById(R.id.place_adress_listview);
        TextView eventHour = view.findViewById(R.id.place_hour_listview);

        eventName.setText(listOfEvents.get(position).getNameEvent());
        eventAdress.setText(listOfEvents.get(position).getAdress());
        eventHour.setText(listOfEvents.get(position).getHourEvent());

        return view;
    }
}
