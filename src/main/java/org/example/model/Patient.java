package org.example.model;


import java.util.Date;

public class Patient {
    private int crId;
    private Avail availability;
    private double hours;
    private Insurance insurance;
    private String name;
    private BCBA assignedBa;
    private Date termDate;

    public Patient(String name, int crId, Avail availability, double hours, Insurance insurance) {
        this.name = name;
        this.crId = crId;
        this.availability = availability;
        this.hours = hours;
        this.insurance = insurance;
    }

    public int getCrId() { return crId; }
    public Avail getAvailability() { return availability; }
    public double getHours() { return hours; }
    public Insurance getInsurance() { return insurance; }

    @Override
    public String toString() {
        return "Patient{" +
                "Name=" + name +
                "crId=" + crId +
                ", availability='" + availability + '\'' +
                ", hours=" + hours +
                ", insurance='" + insurance + '\'' +
                '}';
    }

    public BCBA getAssignedBa() {
        return assignedBa;
    }

    public String getName(){
        return name;
    }

    public void setAssignedBa(BCBA assignedBa) {
        this.assignedBa = assignedBa;
    }

    public Date getTermDate() {
        return termDate;
    }

    public void setTermDate(Date date) {
        this.termDate = date;
    }
}