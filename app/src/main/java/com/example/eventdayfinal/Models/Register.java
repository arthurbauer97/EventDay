package com.example.eventdayfinal.Models;

import java.io.Serializable;

public class Register implements Serializable {

    private User userData;
    private String registeredDateTime;

    public Register(User user, String dateTime) {
        this.userData = user;
        this.registeredDateTime = dateTime;
    }

    public Register() {

    }

    public User getUserData() {
        return userData;
    }

    public void setUserData(User userData) {
        this.userData = userData;
    }

    public String getRegisteredDateTime() {
        return registeredDateTime;
    }

    public void setRegisteredDateTime(String registeredDateTime) {
        this.registeredDateTime = registeredDateTime;
    }
}
