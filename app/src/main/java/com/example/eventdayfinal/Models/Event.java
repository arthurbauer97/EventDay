package com.example.eventdayfinal.Models;

import android.widget.EditText;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {

    private String idEvent;
    private String nameEvent;
    private String hourEvent;
    private String dateEvent;
    private String ticketEvent;
    private String descriptionEvent;

    public Event(){}

    public Event(String nomeEvento, String horarioEvento, EditText dateEvent, String ingressoEvento, String descricaEvento) {
    }

    public Event(String nameEvent, String hourEvent, String dateEvent, String ticketEvent,String descriptionEvent) {
        this.nameEvent = nameEvent;
        this.hourEvent = hourEvent;
        this.dateEvent = dateEvent;
        this.ticketEvent = ticketEvent;
        this.descriptionEvent = descriptionEvent;
    }

    public String getIdEvent() {
        return idEvent;
    }

    public String getTicketEvent() {
        return ticketEvent;
    }

    public void setTicketEvent(String ticketEvent) {
        this.ticketEvent = ticketEvent;
    }

    public void setIdEvent(String idEvent) {
        this.idEvent = idEvent;
    }

    public String getHourEvent() {
        return hourEvent;
    }

    public void setHourEvent(String hourEvent) {
        this.hourEvent = hourEvent;
    }

    public String getDescriptionEvent() {
        return descriptionEvent;
    }

    public void setDescriptionEvent(String descriptionEvent) {
        this.descriptionEvent = descriptionEvent;
    }

    public String getNameEvent() {
        return nameEvent;
    }

    public void setNameEvent(String nameEvent) {
        this.nameEvent = nameEvent;
    }

    public String getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(String dateEvent) {
        this.dateEvent = dateEvent;
    }
}
