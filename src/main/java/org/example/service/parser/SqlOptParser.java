package org.example.service.parser;

import org.example.model.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.time.DateUtils.parseDate;

/**
 *12 = "ProviderName2"	12 = "Emma Maloney - BCBA - Client(s) Sched: 9 - Not Sched: 0"	BA Name
 * 14 = "BA_Job_Title"	14 = "BCBA - BCBA"	Job Title
 * 20 = "BA_Desired_Hours"	20 = "280.0"	Desired Hours
 * 22 = "Textbox8"	22 = "265.0"	Assigned Hours
 * 37 = "Textbox102"	37 = "Ian Mateo Luna Herrera"	Pt Name
 * 38 = "BA_Trainee_CR"	38 = ""	BA Trainee
 * 39  → Fade_Start	39  → 11/12/2025
 * 40 = "clientid2"	40 = "3404159"	CR ID
 * 45 = "Max_IA_Date"	45 = "06/06/24"	IA Date ** Maybe
 * 50 = "wedtimes"	50 = "38.8"	Assigned hours
 * 59 = "availability3"	59 = "M-F 9:15-5:00 (HEALTHPARK)"	Availability
 * 60 = "Auth_Payor3"	60 = "Blue Cross NC | Healthy B"	Insurance
 *
 */

public class SqlOptParser {
    private int BaNameIndex = -1;
    private int BaTypeIndex = -1;
    private int BaDesiredHrsIndex = -1;
    private int BaAssignHrsIndex = -1;
    private int BatNameIndex = -1;
    private int PtCrIdIndex = -1;
    private int PtNameIndex = -1;
    private int PtIaDateIndex = -1;
    private int PtAssignHrsIndex = -1;
    private int PtAvailIndex = -1;
    private int PtInsuranceIndex = -1;
    private int PtTermDateIndex = -1;


    private void setIndices(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim();

            switch (header) {
                case "ProviderName2":
                    BaNameIndex = i;
                    break;

                case "BA_Job_Title":
                    BaTypeIndex = i;
                    break;

                case "BA_Desired_Hours":
                    BaDesiredHrsIndex = i;
                    break;

                case "Textbox8":
                    BaAssignHrsIndex = i;
                    break;

                case "Textbox102":
                    PtNameIndex = i;
                    break;

                case "BA_Trainee_CR":
                    BatNameIndex = i;
                    break;

                case "Fade_Start":
                    PtTermDateIndex = i;
                    break;

                case "clientid2":
                    PtCrIdIndex = i;
                    break;

                case "Max_IA_Date":
                    PtIaDateIndex = i;
                    break;

                case "tuetimes":
                    PtAssignHrsIndex = i;
                    break;

                case "availability3":
                    PtAvailIndex = i;
                    break;

                case "Auth_Payor3":
                    PtInsuranceIndex = i;
                    break;

                default:
                    // ignore extras
                    break;
            }
        }

        // Validation: throw if any required index was not found
        if (BaNameIndex == -1) throw new IllegalStateException("Missing header: BA Name / ProviderName2");
        if (BaTypeIndex == -1) throw new IllegalStateException("Missing header: BA_Job_Title");
        if (BaDesiredHrsIndex == -1) throw new IllegalStateException("Missing header: BA_Desired_Hours");
        if (BaAssignHrsIndex == -1) throw new IllegalStateException("Missing header: Assigned Hours / Textbox8");
        if (PtNameIndex == -1) throw new IllegalStateException("Missing header: Pt Name / Textbox102");
        if (PtIaDateIndex == -1) throw new IllegalStateException("Missing header: Max_IA_Date");
        if (PtAssignHrsIndex == -1) throw new IllegalStateException("Missing header: wedtimes / Assigned hours");
        if (PtAvailIndex == -1) throw new IllegalStateException("Missing header: availability3");
        if (PtInsuranceIndex == -1) throw new IllegalStateException("Missing header: Auth_Payor3");
    }

    private String cleanName(String name){
        return name.split(" - ")[0];
    }


    public static ParseResult parseBas(String filePath) throws IOException {
        ParseResult parseResult = new ParseResult();
        Map<Integer, Patient> patientMap = new HashMap<>();
        Map<String, BCBA> baMap = new HashMap<>(); // Map by BA name to avoid duplicates

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;

            SqlOptParser parser = new SqlOptParser();


            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Split CSV line with commas inside quotes handled
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                if (headers == null) {
                    if (isDataHeader(columns)) {
                        headers = columns;
                        parser.setIndices(headers);
                    }
                    continue; // skip rows until header
                }

                if (columns.length <= Math.max(parser.BatNameIndex, parser.PtInsuranceIndex)) {
                    continue;
                }

                String baName;
                BCBA ba;

                //check if we need to track the BAT or the BA
                if (columns[parser.BatNameIndex].trim().isEmpty()) {
                    baName = parser.cleanName(columns[parser.BaNameIndex]);
                } else{
                    baName = columns[parser.BatNameIndex];
                }

                //if the baMap doesn't have the ba already listed make new BA and add them
                if(!baMap.containsKey(baName)) {
                    //get BA info and add to the BAMap
                    String baType = columns[parser.BaTypeIndex];
                    BAType type;
                    String baTypeNorm = baType.trim().toLowerCase();
                    if (baTypeNorm.contains("visiting bcba")) {
                        type = BAType.VisitingBCBA;
                    } else if (baTypeNorm.contains("astdir") || baTypeNorm.contains("job title missing")) {
                        if (columns[parser.BatNameIndex].trim().isEmpty()) {
                            type = BAType.BCBA;
                        } else {
                            type = BAType.BAT;
                        }
                    } else {
                        type = BAType.BCBA;
                    }

                    double baDesiredHours = parseDouble(columns[parser.BaDesiredHrsIndex]);
                    double baAssignedHours = parseDouble(columns[parser.BaAssignHrsIndex]);

                    ba = baMap.computeIfAbsent(baName,
                            k -> new BCBA(baName, type, baAssignedHours, baDesiredHours));
                }

                //get pt info
                int ptCrId = parseInt(columns[parser.PtCrIdIndex]);
                Avail ptAvail = parseAvailability(columns[parser.PtAvailIndex]);
                String ptInsuranceStr = columns[parser.PtInsuranceIndex];
                Insurance ptInsurance = Insurance.fromString(ptInsuranceStr);
                String ptName = columns[parser.PtNameIndex];
                String val = columns[parser.PtAssignHrsIndex].trim();
                double ptAssignHrs = parseDouble(val);


                // Use ptAssignHrs[0] inside lambda
                Patient pt = patientMap.computeIfAbsent(ptCrId,
                        k -> new Patient(ptName, ptCrId, ptAvail, ptAssignHrs, ptInsurance));


                String termDateStr = columns[parser.PtTermDateIndex];
                Date termDate = null;

                if (termDateStr != null && !termDateStr.trim().isEmpty()) {
                    try {
                        termDate = parseDate(termDateStr.trim(), "MM/dd/yyyy");
                    } catch (ParseException e) {
                        System.out.println("Skipping invalid term date: " + termDateStr);
                    }
                }

                pt.setTermDate(termDate);


                pt.setAssignedBa(baMap.get(baName));
                baMap.get(baName).addPatient(pt);
            }
        }

        parseResult.setBaMap(baMap);
        parseResult.setPatientMap(patientMap);

        return parseResult;
    }

    private static boolean isDataHeader(String[] columns) {
        List<String> headers = Arrays.asList(columns);
        return headers.contains("Region") && headers.contains("ProviderName2") && headers.contains("BA_Job_Title");
    }

    private static Avail parseAvailability(String input) {
        if (input == null || input.isEmpty()) return null;

        if (input.toLowerCase().contains("full day")) {
            return Avail.AM_MID;
        }

        // remove location parentheses and weekdays
        input = input.replaceAll("(?i)M-F", "").replaceAll("\\(.*?\\)", "").trim();

        // regex to find all time ranges like "9:15-5:00" or "12-16"
        Pattern pattern = Pattern.compile("(\\d{1,2})(:\\d{2})?\\s*-\\s*(\\d{1,2})(:\\d{2})?");
        Matcher matcher = pattern.matcher(input);

        boolean am = false, mid = false, pm = false;



        while (matcher.find()) {
            int startHour = Integer.parseInt(matcher.group(1));
            int endHour = Integer.parseInt(matcher.group(3));

            int[] times = makeMilitaryTime(startHour, endHour);
            startHour = times[0];
            endHour = times[1];

            if(startHour == 12 && endHour == 4){
                return Avail.MID;
            }

            if (startHour < 12) am = true;
            if (startHour < 15 && endHour > 12) mid = true;
            if (endHour > 15) pm = true;
        }

        if (am && mid && pm) return Avail.AM_MID_PM;
        if (am && mid) return Avail.AM_MID;
        if (am && pm) return Avail.AM_PM;
        if (mid && pm) return Avail.MID_PM;
        if (am) return Avail.AM;
        if (mid) return Avail.MID;
        if (pm) return Avail.PM;

        return null;
    }

    private static int[] makeMilitaryTime(int startHour, int endHour) {
        if(startHour > 0 && startHour < 6){
            startHour = startHour + 12;
        }
        if (endHour > 0 && endHour < 10){
            endHour = endHour + 12;
        }

        return new int[]{startHour, endHour};
    }

    private static double parseDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        value = value.replaceAll("[^0-9.]", "");
        return value.isEmpty() ? 0.0 : Double.parseDouble(value);
    }

    private static int parseInt(String value) {
        if (value == null || value.isEmpty()) return 0;
        value = value.replaceAll("[^0-9]", "");
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }
}