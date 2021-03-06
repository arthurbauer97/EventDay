package com.example.eventdayfinal.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.common.eventbus.Subscribe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class InfoAdapter implements GoogleMap.InfoWindowAdapter {
    private View view;

    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public double lat;
    public double lon;

    @Override
    public View getInfoWindow(Marker marker) {
        view = LayoutInflater.from(context).inflate(R.layout.info_event, null);
        renderInfo(marker);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderInfo(marker);
        return view;
    }


    public InfoAdapter(Context mContext) {
        context = mContext;
    }

    private void renderInfo(Marker marker) {
        lat = marker.getPosition().latitude;
        lon = marker.getPosition().longitude;

       TextView nameEvent = view.findViewById(R.id.infoNameEvent);
       TextView descriptionEvent = view.findViewById(R.id.infoDescriptionEvent);

       nameEvent.setText(marker.getTitle());
       descriptionEvent.setText(marker.getSnippet());

    }

}
