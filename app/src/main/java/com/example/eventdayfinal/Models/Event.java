package com.example.eventdayfinal.Models;



import java.io.Serializable;


public class Event implements Serializable {

    private String idEvent;
    private String idUser;
    private String idMarker;
    private double latitude;
    private double logintude;
    private String adress;
    private String urlPhoto;
    private String nameEvent;
    private String hourEvent;
    private String dateEvent;
    private String ticketEvent;
    private String descriptionEvent;

    public Event() {}


    public Event(String idEvent, String idUser, double latitude, double logintude, String adress, String urlPhoto, String nameEvent, String hourEvent, String dateEvent, String ticketEvent, String descriptionEvent) {
        this.idEvent = idEvent;
        this.idUser = idUser;
        this.latitude = latitude;
        this.logintude = logintude;
        this.adress = adress;
        this.urlPhoto = urlPhoto;
        this.nameEvent = nameEvent;
        this.hourEvent = hourEvent;
        this.dateEvent = dateEvent;
        this.ticketEvent = ticketEvent;
        this.descriptionEvent = descriptionEvent;
    }



    public String getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(String idEvent) {
        this.idEvent = idEvent;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
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

    public String getNameEvent() {
        return nameEvent;
    }

    public void setNameEvent(String nameEvent) {
        this.nameEvent = nameEvent;
    }

    public String getHourEvent() {
        return hourEvent;
    }

    public void setHourEvent(String hourEvent) {
        this.hourEvent = hourEvent;
    }

    public String getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(String dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getTicketEvent() {
        return ticketEvent;
    }

    public void setTicketEvent(String ticketEvent) {
        this.ticketEvent = ticketEvent;
    }

    public String getDescriptionEvent() {
        return descriptionEvent;
    }

    public void setDescriptionEvent(String descriptionEvent) {
        this.descriptionEvent = descriptionEvent;
    }
}

