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
import java.util.List;

import com.example.eventdayfinal.Adapters.PlacesAdapter;
import com.example.eventdayfinal.Models.Place;
import com.example.eventdayfinal.R;
import com.example.eventdayfinal.Utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class ListFragment extends Fragment implements Serializable {

    private ListView eventsListView;
    private TextView noResultsTextView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Place> eventsArray = new ArrayList<>();
    private static final double DEFAULT_PLACES_DISTANCE = 20.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        eventsListView = view.findViewById(R.id.places_listview);
        noResultsTextView = view.findViewById(R.id.no_result_textview);

        if (MapsFragment.currentLocation != null && FirebaseAuth.getInstance() != null) {
            fetchEvents();
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

    //tras o eventos do banco e coloca todos no mapa
    private void fetchEvents() {
        db.collection("places")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc : docs) {
                            Place place = doc.toObject(Place.class);

                            if (place != null) {
                                if (FirebaseUtils.distanceFromDatabasePlace(MapsFragment.currentLocation, place) < DEFAULT_PLACES_DISTANCE) {
                                    eventsArray.add(place);
                                    noResultsTextView.setVisibility(View.GONE);
                                }
                            }
                            eventsListView.setAdapter(new PlacesAdapter(getContext(), eventsArray));

                        }
                    }
                });
    }

    //caso o usuario selecione na lista um evento pergunta se ...
    private void showConfirmGoToPlaceDialog(final Place clickedPlace) {
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


    private void goToEventLocationInMaps(Place event) {
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
