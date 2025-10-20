package com.philomathery.pcg.model;

public class Certificate {
    private String year;
    private String meet;
    private String place;
    private String recipient;
    private String event;
    private Officiator officiator;

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getMeet() { return meet; }
    public void setMeet(String meet) { this.meet = meet; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public Officiator getOfficiator() { return officiator; }
    public void setOfficiator(Officiator officiator) { this.officiator = officiator; }
}


