package com.example.eventdayfinal.Models;

import java.io.Serializable;

public class Event implements Serializable {

    private String id;
    private String eventName;
    private double latitude;
    private double logintude;
    private String adress;
    private Register registerData;


    public Event() {

    }
    public Event(String id, String eventName, double latitude, double logintude,String adress, Register registerData) {
        this.id = id;
        this.eventName = eventName;
        this.latitude = latitude;
        this.logintude = logintude;
        this.registerData = registerData;
        this.adress = adress;
    }

    public Event(String id, String eventName, double latitude, double logintude) {
        this.id = id;
        this.eventName = eventName;
        this.latitude = latitude;
        this.logintude = logintude;

    }

    public Event(double latitude, double logintude) {
        this.latitude = latitude;
        this.logintude = logintude;

    }

    public Register getRegisterData() {
        return registerData;
    }

    public void setRegisterData(Register registerData) {
        this.registerData = registerData;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLogintude() {
        return logintude;
    }

    public void setLogintude(double logintude) {
        this.logintude = logintude;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

}
