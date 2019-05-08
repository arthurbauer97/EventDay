package com.example.eventdayfinal.Models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class User implements Serializable {

    private String userID;
    private String userName;
    private String userEmail;
    private LatLng userLastLocation;
    private double notificationRange;

    public User(String userID, String userName, String userEmail,
                LatLng userLastLocation,double notificationRange) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userLastLocation = userLastLocation;
        this.notificationRange = notificationRange;
    }

    public User(String userID, String userName, String userEmail, LatLng userLastLocation) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userLastLocation = userLastLocation;
    }

    public User(String userID, String userName, String userEmail) {
        this.userID = userID;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public User() {
    }

    public double getNotificationRange() {
        return notificationRange;
    }

    public void setNotificationRange(double notificationRange) {
        this.notificationRange = notificationRange;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LatLng getUserLastLocation() {
        return userLastLocation;
    }

    public void setUserLastLocation(LatLng userLastLocation) {
        this.userLastLocation = userLastLocation;
    }
}
