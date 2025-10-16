package org.example.service;

import org.example.model.BCBA;
import org.example.model.Patient;

import java.util.Map;

public class DataStore {
    private Map<String, BCBA> excelBas;
    private Map<Integer, Patient> excelPatients;

    private Map<String, BCBA> sqlBas;
    private Map<Integer, Patient> sqlPts;

    private static DataStore instance = new DataStore();

    private DataStore() {}

    public static DataStore getInstance() {
        return instance;
    }

    public Map<String, BCBA> getExcelBas() {return excelBas;}
    public void setExcelBas (Map<String, BCBA> excelBas){ this.excelBas = excelBas;}

    public Map<Integer, Patient> getExcelPatients() { return excelPatients; }
    public void setExcelPatients(Map<Integer, Patient> excelPatients) { this.excelPatients = excelPatients; }

    public Map<String, BCBA> getSqlBas() { return sqlBas; }
    public void setSqlBas(Map<String, BCBA> sqlBas) { this.sqlBas = sqlBas; }

    public Map<Integer, Patient> getSqlPatients() { return sqlPts; }
    public void setSqlPts(Map<Integer, Patient> sqlPts) { this.sqlPts = sqlPts; }
}
