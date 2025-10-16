package org.example.model.results;

public class ClVsSqlBaAuditResult {
    private String baName;
    private double clAssignedHours;
    private double sqlAssignedHours;
    private String status;


    public ClVsSqlBaAuditResult(String baName, double clAssignedHours, double sqlAssignedHours, String status) {
        this.baName = baName;
        this.clAssignedHours = clAssignedHours;
        this.sqlAssignedHours = sqlAssignedHours;
        this.status = status;
    }

    public String getBaName() {
        return baName;
    }

    public void setBaName(String baName) {
        this.baName = baName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}