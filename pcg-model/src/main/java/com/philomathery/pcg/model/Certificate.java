package com.philomathery.pcg.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "certificate")
@XmlAccessorType(XmlAccessType.FIELD)
public class Certificate {
    @XmlElement
    private String year;

    @XmlElement
    private String meet;

    @XmlElement
    private String place;

    @XmlElement
    private String recipient;

    @XmlElement
    private String event;

    @XmlElement
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


