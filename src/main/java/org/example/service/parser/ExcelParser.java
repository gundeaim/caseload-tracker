package org.example.service.parser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.*;

import java.io.File;
import java.io.FileInputStream;

public class ExcelParser {


    //helper method for scrubbing license and position details
    private String extractName(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) return "";

        String name = rawValue.trim();

        // Remove prefix: any letters at the start followed by ':' or space(s)
        // e.g., "SBA: ", "ADAS ", "BCBA: "
        name = name.replaceFirst("^[A-Z]+[:\\s]+", "").trim();

        // Remove everything after the first comma
        int commaIndex = name.indexOf(',');
        if (commaIndex != -1) {
            name = name.substring(0, commaIndex).trim();
        }

        // Optional: collapse multiple spaces inside the name
        name = name.replaceAll("\\s+", " ");

        return name;
    }




    public ParseResult parseFromFile(File excelFile) {
        //holds the results for all sheets
        ParseResult result = new ParseResult();

        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFile))) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);

                //holds results for a single tab in the excel
                ParseResult sheetResult = parseSheet(sheet, result);

                // Merge sheet results into overall result
                result.getBaMap().putAll(sheetResult.getBaMap());
                result.getPatientMap().putAll(sheetResult.getPatientMap());
                result.getErrors().addAll(sheetResult.getErrors());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }



    private ParseResult parseSheet(Sheet sheet, ParseResult workbookResult) {
        ParseResult result = new ParseResult();
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        BCBA currentBA = null;

        // Last known good column indices for fallback
        int lastCrIdCol = 2, lastAvailCol = 5, lastHoursCol = 9, lastInsuranceCol = 10;

        // Current BA table column indices
        int crIdCol = -1, availCol = -1, hoursCol = -1, insuranceCol = -1;

        for (Row row : sheet) {
            String firstCell = getCellText(row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), evaluator).trim();

            if (firstCell.toLowerCase().startsWith("slot")) {
                // Parse BA header
                currentBA = parseBaHeader(row, sheet, evaluator);
                if(workbookResult.baMap.containsKey(currentBA.getName())){
                    BCBA workbookBa = workbookResult.baMap.get(currentBA.getName());
                    currentBA = workbookBa;

                } else {
                    result.baMap.put(currentBA.getName(), currentBA);
                }
                // Detect column indexes
                int[] cols = detectColumnIndexes(row, evaluator);
                crIdCol = cols[0] != -1 ? cols[0] : lastCrIdCol;
                availCol = cols[1] != -1 ? cols[1] : lastAvailCol;
                hoursCol = cols[2] != -1 ? cols[2] : lastHoursCol;
                insuranceCol = cols[3] != -1 ? cols[3] : lastInsuranceCol;

                // Add missing column errors
                if (cols[0] == -1) result.addError("CR ID column missing on sheet: " + sheet.getSheetName() + " row: " + (row.getRowNum() + 1));
                if (cols[1] == -1) result.addError("Availability column missing on sheet: " + sheet.getSheetName() + " row: " + (row.getRowNum() + 1));
                if (cols[2] == -1) result.addError("Hours column missing on sheet: " + sheet.getSheetName() + " row: " + (row.getRowNum() + 1));
                if (cols[3] == -1) result.addError("Insurance column missing on sheet: " + sheet.getSheetName() + " row: " + (row.getRowNum() + 1));

                // Update last known good columns
                lastCrIdCol = crIdCol;
                lastAvailCol = availCol;
                lastHoursCol = hoursCol;
                lastInsuranceCol = insuranceCol;

            } else if (firstCell.isEmpty() && currentBA != null && hoursCol != -1) {
                parseBaTotalHours(row, currentBA, hoursCol, evaluator);
            } else if (currentBA != null) {
                parsePatientRow(row, currentBA, crIdCol, availCol, hoursCol, insuranceCol, evaluator, result, sheet.getSheetName(),
                        lastCrIdCol, lastAvailCol, lastHoursCol, lastInsuranceCol);
            }
        }

        return result;
    }

    private int parseIntCell(Cell cell, FormulaEvaluator evaluator) {
        String text = getCellText(cell, evaluator).trim();
        if (text.isEmpty()) return 0;

        try {
            if (text.contains(".")) {
                return (int) Double.parseDouble(text);
            } else {
                return Integer.parseInt(text);
            }
        } catch (NumberFormatException e) {
            System.out.println("Skipping row due to invalid number: " + text);
            return 0;
        }
    }

    private double parseDoubleCell(Cell cell, FormulaEvaluator evaluator) {
        String text = getCellText(cell, evaluator).trim();
        if (text.isEmpty()) return 0.0;

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            System.out.println("Skipping row due to invalid number: " + text);
            return 0.0;
        }
    }

    private String getCellText(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        return Double.toString(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return Boolean.toString(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        CellValue cellValue = evaluator.evaluate(cell);
                        if (cellValue == null) return "";
                        switch (cellValue.getCellType()) {
                            case STRING:  return cellValue.getStringValue();
                            case NUMERIC: return Double.toString(cellValue.getNumberValue());
                            case BOOLEAN: return Boolean.toString(cellValue.getBooleanValue());
                            case ERROR:   return ""; // gracefully ignore bad formulas
                            default:      return "";
                        }
                    } catch (Exception e) {
                        // Formula failed to evaluate (COUNTIF error, etc.)
                        return "";
                    }
                case BLANK:
                case ERROR:
                default:
                    return "";
            }
        } catch (Exception e) {
            return ""; // fallback catch-all
        }
    }

    private BCBA parseBaHeader(Row row, Sheet sheet, FormulaEvaluator evaluator) {
        Cell secondCell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        String nameRaw = getCellText(secondCell, evaluator).trim();
        String cleanName = extractName(nameRaw);

        BAType type;
        if (nameRaw.toLowerCase().contains("bat")) {
            type = BAType.BAT;
        } else if (nameRaw.toLowerCase().contains("bcaba")) {
            type = BAType.BCaBA;
        } else {
            type = BAType.BCBA;
        }

        return new BCBA(cleanName, type, sheet.getSheetName());
    }

    private int[] detectColumnIndexes(Row row, FormulaEvaluator evaluator) {
        int crIdCol = -1;
        int availCol = -1;
        int hoursCol = -1;
        int insuranceCol = -1;

        for (Cell cell : row) {
            String columnName = getCellText(cell, evaluator).trim().toLowerCase();
            int colIndex = cell.getColumnIndex();

            switch (columnName) {
                case "crid", "cr id", "client id", "id" -> crIdCol = colIndex;
                case "avail", "availability" -> availCol = colIndex;
                case "hours" -> hoursCol = colIndex;
                case "insurance" -> insuranceCol = colIndex;
            }
        }

        // Simply return the detected column indexes; -1 means column not found
        return new int[]{crIdCol, availCol, hoursCol, insuranceCol};
    }

    private void parseBaTotalHours(Row row, BCBA currentBA, int hoursColumn, FormulaEvaluator evaluator) {
        Cell hoursCell = row.getCell(hoursColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        String hoursVal = getCellText(hoursCell, evaluator).trim();

        if (!hoursVal.isEmpty()) {
            try {
                double baTotalHours = Double.parseDouble(hoursVal);
                if(currentBA.getAssignedHours() < baTotalHours) {
                    currentBA.setAssignedHours(baTotalHours);
                }
            } catch (NumberFormatException e) {
                System.out.println("Could not parse BA total hours: " + hoursVal);
            }
        }
    }

    private void parsePatientRow(Row row, BCBA currentBA,
                                 int crIdColumn, int availColumn, int hoursColumn, int insuranceColumn,
                                 FormulaEvaluator evaluator, ParseResult result, String sheetName,
                                 int lastCrIdCol, int lastAvailCol, int lastHoursCol, int lastInsuranceCol) {

        int rowNum = row.getRowNum();

        // Ensure all column indices are valid
        crIdColumn = crIdColumn >= 0 ? crIdColumn : lastCrIdCol;
        availColumn = availColumn >= 0 ? availColumn : lastAvailCol;
        hoursColumn = hoursColumn >= 0 ? hoursColumn : lastHoursCol;
        insuranceColumn = insuranceColumn >= 0 ? insuranceColumn : lastInsuranceCol;

        try {
            String ptName = getCellText(row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), evaluator);
            int crId = parseIntCell(row.getCell(crIdColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), evaluator);
            String availabilityStr = getCellText(row.getCell(availColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), evaluator);
            Avail availability = Avail.fromString(availabilityStr);
            double hours = parseDoubleCell(row.getCell(hoursColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), evaluator);
            String insuranceStr = getCellText(row.getCell(insuranceColumn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK), evaluator);
            Insurance insurance = Insurance.fromString(insuranceStr);


            boolean isRowEmpty = crId == 0 &&
                    (availability == null || availabilityStr.isEmpty())
                    && insuranceStr.isEmpty();

            if (!isRowEmpty) {
                Patient patient = result.patientMap.getOrDefault(crId, new Patient(ptName, crId, availability, hours, insurance));
                patient.setAssignedBa(currentBA);
                currentBA.addPatient(patient);
                result.patientMap.put(crId, patient);

                if(insurance == null){
                    result.addError("Error Pt: " + ptName + " pt ID: " + crId + " has an unrecognized Payor Code: "  + insuranceStr);
                }
            }
        } catch (IllegalArgumentException e) {
            // Catch invalid cell index errors
            result.addError("Error reading row " + rowNum + " on sheet: " + sheetName + " - " + e.getMessage());
        }
    }
}
