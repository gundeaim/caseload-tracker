package org.example.service.auditor;

import org.example.model.BCBA;
import org.example.model.results.ClBaHoursAuditResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClBaHoursAuditor {
    public static List<ClBaHoursAuditResult> audit(Map<String, BCBA> bas) {
        List<ClBaHoursAuditResult> mismatches = new ArrayList<>();
        for (Map.Entry<String, BCBA> entry : bas.entrySet()) {
            BCBA ba = entry.getValue();
            double calcHours = ba.calculateClAssignHours();
            double sheetHours = ba.getAssignedHours();
            if (calcHours != sheetHours) {
                mismatches.add(new ClBaHoursAuditResult(
                        ba, sheetHours, calcHours));
            }
        }
        return mismatches;
    }
}