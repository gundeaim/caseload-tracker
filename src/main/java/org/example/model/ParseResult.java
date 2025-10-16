package org.example.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseResult {
    public Map<Integer, Patient> patientMap = new HashMap<>();
    public Map<String, BCBA> baMap = new HashMap<>();
    public List<String> errors = new ArrayList<>();


    public Map<String, BCBA> getBaMap() {
        return baMap;
    }

    public void setBaMap(Map<String, BCBA> baMap) {
        this.baMap = baMap;
    }

    public Map<Integer, Patient> getPatientMap() {
        return patientMap;
    }

    public void setPatientMap(Map<Integer, Patient> patientMap) {
        this.patientMap = patientMap;
    }

    public List<String> getErrors() { return errors; }

    public void addError(String error) {
        errors.add(error);
    }
}
