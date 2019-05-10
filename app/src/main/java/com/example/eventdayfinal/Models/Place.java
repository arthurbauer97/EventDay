package com.example.eventdayfinal.Models;

import java.io.Serializable;

public class Place implements Serializable {

    private String idPlace;
    private String placeName;
    private double latitude;
    private double logintude;
    private String adress;
    private String urlPhoto;


    public Place() {

    }
    public Place(String idPlace, String eventName, double latitude, double logintude, String adress) {
        this.idPlace = idPlace;
        this.placeName = eventName;
        this.latitude = latitude;
        this.logintude = logintude;
        this.adress = adress;
    }

    public Place(String idPlace, String eventName, double latitude, double logintude) {
        this.idPlace = idPlace;
        this.placeName = eventName;
        this.latitude = latitude;
        this.logintude = logintude;

    }

    public Place(double latitude, double logintude) {
        this.latitude = latitude;
        this.logintude = logintude;

    }

    public String getIdPlace() {
        return idPlace;
    }

    public void setIdPlace(String id) {
        this.idPlace = idPlace;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
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

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }
}
