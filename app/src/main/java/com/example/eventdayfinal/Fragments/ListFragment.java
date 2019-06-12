package com.example.eventdayfinal.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import com.example.eventdayfinal.Adapters.EventsAdapter;
import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.R;
import com.example.eventdayfinal.Utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class ListFragment extends Fragment{

    private ListView eventsListView;
    private TextView noResultsTextView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    private ArrayList<Event> eventsArray = new ArrayList<>();
    private static final double DEFAULT_PLACES_DISTANCE = 20.0;

    //create event dialog
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private View view;

    // text event
    private TextView nameEvent;
    private TextView dateEvent;
    private TextView ticketEvent;
    private TextView descriptionEvent;
    private TextView hourEvent;
    private ImageView newButtonAddPhoto;
    private ImageView photoEvent;
    private ImageView deleteEvent;
    private Button viewInMap;
    private int num;

    private Event event = new Event();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_events, container, false);

        eventsListView = view.findViewById(R.id.events_listview);
        noResultsTextView = view.findViewById(R.id.no_result_textview);

        if (MapsFragment.currentLocation != null && FirebaseAuth.getInstance() != null) {
            fetchEvents();
        } else {
            noResultsTextView.setVisibility(View.VISIBLE);
        }

        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                event = eventsArray.get(position);
                loadEvent(event);
                num = position;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //tras o eventos e monta a lista de eventos proximos
    private void fetchEvents() {
        db.collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc : docs) {
                            Event event = doc.toObject(Event.class);

                            if (event != null) {
                                if (FirebaseUtils.distanceFromDatabasePlace(MapsFragment.currentLocation, event) < DEFAULT_PLACES_DISTANCE) {
                                    eventsArray.add(event);
                                    noResultsTextView.setVisibility(View.GONE);
                                }
                            }
                            eventsListView.setAdapter(new EventsAdapter(getContext(), eventsArray));
                        }
                    }
                });
    }

    //create event
    private void loadEvent(Event event){
        builder = new AlertDialog.Builder(getContext());
        view = getLayoutInflater().inflate(R.layout.dialog_all_event,null);

        viewInMap = view.findViewById(R.id.viewInMap);

//        photoEvent = view.findViewById(R.id.PhotoEvent);
        dateEvent = view.findViewById(R.id.DateEvent);
        nameEvent = view.findViewById(R.id.NameEvent);
        hourEvent = view.findViewById(R.id.HourEvent);
        ticketEvent = view.findViewById(R.id.TicketEvent);
        descriptionEvent = view.findViewById(R.id.DescriptionEvent);

        nameEvent.setText(event.getNameEvent());
        dateEvent.setText(event.getDateEvent());
        hourEvent.setText(event.getHourEvent());
        ticketEvent.setText(event.getTicketEvent());
        descriptionEvent.setText(event.getDescriptionEvent());

        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        viewInMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserAuth()) {
                    if (!isEmpty()) {
                        goToEventLocationInMaps(eventsArray.get(num));
                        dialog.hide();
                    }
                }
            }
        });
        dialog.show();
    }

    public boolean isUserAuth() {
        return firebaseUser != null;
    }

    private boolean isEmpty() {
        if (nameEvent.getText().toString().isEmpty() ||
                dateEvent.getText().toString().isEmpty() ||
                hourEvent.getText().toString().isEmpty() ||
                ticketEvent.getText().toString().isEmpty() ||
                descriptionEvent.getText().toString().isEmpty()) {
            return true;

        }else return false;
    }

    private void goToEventLocationInMaps(Event event) {
        MapsFragment mapsFragment = new MapsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        mapsFragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, mapsFragment);
        fragmentTransaction.commit();
    }
}
