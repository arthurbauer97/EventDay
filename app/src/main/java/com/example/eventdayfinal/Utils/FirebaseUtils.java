package com.example.eventdayfinal.Utils;


import android.location.Location;
import android.util.Log;

import com.example.eventdayfinal.Models.Event;
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

    public static boolean savePlace(Event place, List<Integer> paymentMethods) {

//        place.setAcceptedSmartcoins(filterPayments(paymentMethods));

        try {
            placesReference.child(place.getId()).setValue(place);
            return true;
        } catch (Exception e) {
            Log.d("FirebaseUtils-savePlace", e.getMessage());
            return false;
        }
    }

    private static Map<String, Boolean> filterPayments(List<Integer> paymentMethodsToFilter) {
        Map<String, Boolean> acceptedPayments = new HashMap<>(3);

        if (paymentMethodsToFilter.contains(0)) {
            acceptedPayments.put("Cripto", true);
        } else acceptedPayments.put("Cripto", false);

        if (paymentMethodsToFilter.contains(1)) {
            acceptedPayments.put("SmartPay - SmartCard", true);
        } else acceptedPayments.put("SmartPay - SmartCard", false);

        if (paymentMethodsToFilter.contains(2)) {
            acceptedPayments.put("AtarBand", true);
        } else acceptedPayments.put("AtarBand", false);

        return acceptedPayments;
    }

    public static double distanceFromDatabasePlace(Event currentPlace, Event placeFromDatabase) {
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
