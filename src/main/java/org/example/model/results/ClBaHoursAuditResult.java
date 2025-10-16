package org.example.model.results;

import org.example.model.BCBA;

public class ClBaHoursAuditResult {
    private String baName;
    private String baLocation;
    private double spreadsheetHours;
    private double calculatedHours;
    private BCBA bcba; // new reference to the actual BCBA

    public ClBaHoursAuditResult(BCBA bcba, double spreadsheetHours, double calculatedHours){
        this.bcba = bcba;
        this.baName = bcba.getName();
        this.baLocation = bcba.getLocation();
        this.spreadsheetHours = spreadsheetHours;
        this.calculatedHours = calculatedHours;
    }

    public String getBcbaName() { return baName; }
    public String getBcbaLocation() { return baLocation; }
    public double getSpreadsheetHours() { return spreadsheetHours; }
    public double getCalculatedHours() { return calculatedHours; }
    public BCBA getBcba() { return bcba; } // getter for drill-down
}