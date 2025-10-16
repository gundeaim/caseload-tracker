package org.example.service.auditor;

import org.example.model.Avail;
import org.example.model.BCBA;
import org.example.model.Patient;
import org.example.model.results.ClVsSqlBaAuditResult;
import org.example.model.results.ClVsSqlPtAuditResult;
import org.apache.commons.text.similarity.JaroWinklerDistance;



import java.util.*;

public class ClVsSqlAuditor {

    /**
     * Compares Excel BA Map and Pt Maps SQL BA and pt maps and returns audit results.
     */
    public static List<ClVsSqlBaAuditResult> runBaAudit(Map<String, BCBA> excelBas, Map<String, BCBA> sqlBas) {
        // stub: return empty list for now
        return new ArrayList<>();
    }

    public static List<ClVsSqlPtAuditResult> runPtAudit(
            Map<Integer, Patient> sqlPatients,
            Map<Integer, Patient> excelPatients) {

        List<ClVsSqlPtAuditResult> results = new ArrayList<>();

        // Pass 1: look at SQL patients, check if Excel has them
        for (Map.Entry<Integer, Patient> entry : sqlPatients.entrySet()) {
            Integer id = entry.getKey();
            Patient sqlPt = entry.getValue();
            Patient excelPt = excelPatients.get(id);

            if (excelPt == null) {
                // Excel missing this patient entirely
                results.add(new ClVsSqlPtAuditResult(
                        id,
                        sqlPt.getName(),
                        "Patient ID: " + sqlPt.getCrId() + " Name: " + sqlPt.getName() + " is missing in Excel Caseload"
                ));
                continue;
            }

            // Compare fields
            if (!Objects.equals(sqlPt.getAvailability(), excelPt.getAvailability())) {
                if (sqlPt.getAvailability() == Avail.AM_MID && excelPt.getAvailability() == Avail.AM_MID_PM) {}
                else if (sqlPt.getAvailability() == Avail.AM_MID_PM && excelPt.getAvailability() == Avail.AM_MID) {}
                else if (sqlPt.getAvailability() == null){}
                else {
                    results.add(new ClVsSqlPtAuditResult(
                            id, sqlPt.getName(),
                            "Availability mismatch: SQL=" + sqlPt.getAvailability()
                                    + ", Excel=" + excelPt.getAvailability()));
                }
            }

            double sqlHours   = sqlPt.getHours();
            double excelHours = excelPt.getHours();

            if (Math.abs(sqlHours - excelHours) > 0.1) {
                if(excelHours == 0.0){
                    if(sqlPt.getTermDate() == null){
                        results.add(new ClVsSqlPtAuditResult(
                                id, sqlPt.getName(),
                                "Hours mismatch: SQL=" + sqlPt.getHours()
                                        + ", Excel=" + excelPt.getHours() + " Please add pt term date in CR."));
                    }
                } else {
                    results.add(new ClVsSqlPtAuditResult(
                            id, sqlPt.getName(),
                            "Hours mismatch: SQL=" + sqlPt.getHours()
                                    + ", Excel=" + excelPt.getHours()));
                }
            }

            if (!Objects.equals(sqlPt.getInsurance(), excelPt.getInsurance())) {
                if (sqlPt.getInsurance() != null) {
                    results.add(new ClVsSqlPtAuditResult(
                            id, sqlPt.getName(),
                            "Insurance mismatch: SQL=" + sqlPt.getInsurance()
                                    + ", Excel=" + excelPt.getInsurance()));

                }
            }

            if (!namesAlmostMatch(sqlPt.getAssignedBa().getName(), excelPt.getAssignedBa().getName())) {
                results.add(new ClVsSqlPtAuditResult(
                        id, sqlPt.getName(),
                        "Assigned BA mismatch: SQL=" + sqlPt.getAssignedBa().getName()
                                + ", Excel=" + excelPt.getAssignedBa().getName()));
            }
        }

        // Pass 2: catch extra Excel patients not in SQL
        for (Integer id : excelPatients.keySet()) {
            if (!sqlPatients.containsKey(id)) {
                Patient excelPt = excelPatients.get(id);
                results.add(new ClVsSqlPtAuditResult(
                        id, excelPt.getName(),
                        "Pt in Excel not found in SQL. Please check that CR ID is correct."
                ));
            }
        }

        return results;
    }


    public static boolean namesAlmostMatch(String name1, String name2) {
        name1 = normalizeName(name1);
        name2 = normalizeName(name2);

        JaroWinklerDistance distance = new JaroWinklerDistance();
        Double score = distance.apply(name1, name2);
        if (score == null) return false;

        return score <= 0.20;
    }


    private static String normalizeName(String name) {
        if (name == null) return "";
        // Remove quotes, collapse whitespace, convert non-breaking spaces to regular spaces
        name = name.replace("\"", "")
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
        return name;
    }


}