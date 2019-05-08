package com.example.eventdayfinal.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;

import com.example.eventdayfinal.Adapters.PlacesAdapter;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.R;
import com.example.eventdayfinal.Utils.FirebaseUtils;

public class ListFragment extends Fragment implements Serializable {

    private ListView eventsListView;
    private TextView noResultsTextView;
    private FirebaseUser currentUser;
    private ArrayList<Event> eventsArray = new ArrayList<>();
    private static final double DEFAULT_PLACES_DISTANCE = 20.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        eventsListView = view.findViewById(R.id.places_listview);
        noResultsTextView = view.findViewById(R.id.no_result_textview);

        if(MapsFragment.currentLocation != null && FirebaseAuth.getInstance() != null) {
            getNearbyEvents();
        } else {
            noResultsTextView.setVisibility(View.VISIBLE);
        }

        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showConfirmGoToPlaceDialog(eventsArray.get(position));
            }
        });


        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    private void getNearbyEvents() {
        final ArrayList<Event> eventsList = new ArrayList<>();
        DatabaseReference events = FirebaseDatabase.getInstance().getReference("Eventos");

        events.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Event databaseEvent = dataSnapshot.getValue(Event.class);

                if(databaseEvent != null) {
                    if(FirebaseUtils.distanceFromDatabasePlace(MapsFragment.currentLocation, databaseEvent) < DEFAULT_PLACES_DISTANCE) {
                        eventsArray.add(databaseEvent);
                        noResultsTextView.setVisibility(View.GONE);
                    }
                }

                eventsListView.setAdapter(new PlacesAdapter(getContext(), eventsArray));
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Event eventData = dataSnapshot.getValue(Event.class);


                if(eventData != null) {
                    if(FirebaseUtils.distanceFromDatabasePlace(MapsFragment.currentLocation, eventData) < DEFAULT_PLACES_DISTANCE) {
                        eventsArray.remove(eventData);
                        noResultsTextView.setVisibility(View.GONE);
                    }
                }

                eventsListView.setAdapter(new PlacesAdapter(getContext(), eventsArray));
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {
                noResultsTextView.setVisibility(View.VISIBLE);
            }
        });

    }

    private void showConfirmGoToPlaceDialog(final Event clickedPlace) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.listview_options_message)
                .setPositiveButton(R.string.listview_options_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        goToEventLocationInMaps(clickedPlace);
                    }
                })
                .setNegativeButton(R.string.listview_options_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false);
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void goToEventLocationInMaps(Event event) {
        MapsFragment mapsFragment = new MapsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("PLACE_OBJECT", event);
        mapsFragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, mapsFragment);
        fragmentTransaction.commit();
    }
}
