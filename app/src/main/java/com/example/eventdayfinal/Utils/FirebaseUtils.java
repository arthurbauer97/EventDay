package com.example.eventdayfinal.Utils;


import android.location.Location;
import android.util.Log;

import com.example.eventdayfinal.Models.Event;
import com.example.eventdayfinal.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FirebaseUtils {

    public static double distanceFromDatabasePlace(Event currentEvent, Event eventFromDatabase) {
        if(eventFromDatabase == null) {
            return -1;
        }

        Location startPoint = new Location("startPoint");
        startPoint.setLatitude(currentEvent.getLatitude());
        startPoint.setLongitude(currentEvent.getLogintude());

        Location endPoint = new Location("endPoint");
        endPoint.setLatitude(eventFromDatabase.getLatitude());
        endPoint.setLongitude(eventFromDatabase.getLogintude());

        double distance = startPoint.distanceTo(endPoint) * 0.001d;
        return distance;
    }

}
