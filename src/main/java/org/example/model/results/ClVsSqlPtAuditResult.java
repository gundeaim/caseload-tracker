package org.example.model.results;

import org.example.model.Avail;
import org.example.model.BCBA;
import org.example.model.Insurance;

public class ClVsSqlPtAuditResult {
    private Integer clientId;
    private String name;
    private BCBA assignedBa;

    private Avail excelAvailability;
    private Avail sqlAvailability;

    private double excelHours;
    private double sqlHours;

    private Insurance excelInsurance;
    private Insurance sqlInsurance;

    private String status; // e.g. "Hours mismatch", "Insurance mismatch", "Match"

    public ClVsSqlPtAuditResult(Integer clientId, String name, BCBA assignedBa,
                                Avail excelAvailability, Avail sqlAvailability,
                                double excelHours, double sqlHours,
                                Insurance excelInsurance, Insurance sqlInsurance,
                                String status) {
        this.clientId = clientId;
        this.name = name;
        this.assignedBa = assignedBa;
        this.excelAvailability = excelAvailability;
        this.sqlAvailability = sqlAvailability;
        this.excelHours = excelHours;
        this.sqlHours = sqlHours;
        this.excelInsurance = excelInsurance;
        this.sqlInsurance = sqlInsurance;
        this.status = status;
    }

    public ClVsSqlPtAuditResult(Integer clientId, String name, String status) {
        this.clientId = clientId;
        this.name = name;
        this.status = status;
    }

    public Integer getClientId() { return clientId; }
    public String getName() { return name; }
    public BCBA getAssignedBa() { return assignedBa; }
    public Avail getExcelAvailability() { return excelAvailability; }
    public Avail getSqlAvailability() { return sqlAvailability; }
    public double getExcelHours() { return excelHours; }
    public double getSqlHours() { return sqlHours; }
    public Insurance getExcelInsurance() { return excelInsurance; }
    public Insurance getSqlInsurance() { return sqlInsurance; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}