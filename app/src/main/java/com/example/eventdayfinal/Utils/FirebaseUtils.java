package com.example.eventdayfinal.Utils;


import android.location.Location;
import android.util.Log;

import com.example.eventdayfinal.Models.Place;
import com.example.eventdayfinal.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUtils {

    private static DatabaseReference placesReference
            = FirebaseDatabase.getInstance().getReference("Places");

    private static DatabaseReference usersReference
            = FirebaseDatabase.getInstance().getReference("Users");

    private static DatabaseReference eventsReference
            = FirebaseDatabase.getInstance().getReference("Events");

    public static FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public static boolean isUserAuth = currentUser != null;


    private static boolean saveUser(User user) {
        try {
            usersReference.child(user.getUserID()).setValue(user);
            return true;
        } catch (Exception e) {
            Log.d("FirebaseUtils-saveUser", e.getMessage());
            return false;
        }
    }

    public static boolean savePlace(Place place, List<Integer> paymentMethods) {
        try {
            placesReference.child(place.getIdPlace()).setValue(place);
            return true;
        } catch (Exception e) {
            Log.d("FirebaseUtils-savePlace", e.getMessage());
            return false;
        }
    }

    public static double distanceFromDatabasePlace(Place currentPlace, Place placeFromDatabase) {
        if(placeFromDatabase == null) {
            return -1;
        }

        Location startPoint = new Location("startPoint");
        startPoint.setLatitude(currentPlace.getLatitude());
        startPoint.setLongitude(currentPlace.getLogintude());

        Location endPoint = new Location("endPoint");
        endPoint.setLatitude(placeFromDatabase.getLatitude());
        endPoint.setLongitude(placeFromDatabase.getLogintude());

        double distance = startPoint.distanceTo(endPoint) * 0.001d;
        return distance;
    }

}
