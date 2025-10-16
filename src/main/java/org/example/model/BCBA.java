package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class BCBA {
    private String name;
    private BAType type;
    private List<Credential> credentialList;
    private List<Patient> patients = new ArrayList<>();
    private String location;
    private double assignedHours;
    private double desiredHours;

    public BCBA(String name, BAType type, double assignedHours, double desiredHours) {
        this.name = name;
        this.type = type;
        this.assignedHours = assignedHours;
        this.desiredHours = desiredHours;
    }

    public BCBA(String name, BAType type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.assignedHours = 0.0;
    }

    public void addPatient(Patient patient) {
        if (patients.stream().noneMatch(p -> p.getCrId() == patient.getCrId())) {
            patients.add(patient);
        }
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public double calculateClAssignHours(){
        double assignedHours = 0.00;

        for (Patient patient : patients){
            assignedHours = assignedHours + patient.getHours();
        }

        return assignedHours;
    }

    public void setAssignedHours(double hours){
        assignedHours = hours;
    }

    public double getAssignedHours(){
        return assignedHours;
    }


    public double getDesiredHours(){
        return desiredHours;
    }

    public String getName() {
        return name;
    }

    public BAType getType() {
        return type;
    }

    public List<Credential> getCredentialList() {
        return credentialList;
    }

    public void setCredentialList(List<Credential> credentialList) {
        this.credentialList = credentialList;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}