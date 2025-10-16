package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.BCBA;
import org.example.model.ParseResult;
import org.example.model.Patient;
import org.example.model.results.ClBaHoursAuditResult;
import org.example.model.results.ClVsSqlBaAuditResult;
import org.example.model.results.ClVsSqlPtAuditResult;
import org.example.service.auditor.ClBaHoursAuditor;
import org.example.service.auditor.ClVsSqlAuditor;
import org.example.service.DataStore;
import org.example.service.parser.ExcelParser;
import org.example.service.parser.SqlOptParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CaseloadController {

    @FXML private TableView<ClBaHoursAuditResult> tblAuditResults;
    @FXML private TableColumn<ClBaHoursAuditResult, String> colBAName;
    @FXML private TableColumn<ClBaHoursAuditResult, String> colBALocation;
    @FXML private TableColumn<ClBaHoursAuditResult, Double> colSpreadsheetHours;
    @FXML private TableColumn<ClBaHoursAuditResult, Double> colCalculatedHours;

    @FXML private Label lblMessage;
    @FXML private TextArea txtErrors;
    @FXML private Button btnOpenBAWindow;

    @FXML
    public void initialize() {
        // Setup columns
        colBAName.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getBcbaName()));
        colBALocation.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getBcbaLocation()));
        colSpreadsheetHours.setCellValueFactory(p -> new javafx.beans.property.SimpleDoubleProperty(p.getValue().getSpreadsheetHours()).asObject());
        colCalculatedHours.setCellValueFactory(p -> new javafx.beans.property.SimpleDoubleProperty(p.getValue().getCalculatedHours()).asObject());

        // Disable button initially
        btnOpenBAWindow.setDisable(true);

        // Add row click listener for drill-down
        tblAuditResults.setRowFactory(tv -> {
            TableRow<ClBaHoursAuditResult> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) { // double-click
                    ClBaHoursAuditResult rowData = row.getItem();
                    showBcbaPatients(rowData.getBcba());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleSelectCaseloadSheet(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Caseload Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));

        Stage stage = (Stage) tblAuditResults.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            ExcelParser parser = new ExcelParser();
            ParseResult result = parser.parseFromFile(selectedFile);

            DataStore store = DataStore.getInstance();
            store.setExcelBas(result.getBaMap());
            store.setExcelPatients(result.getPatientMap());

            showErrors(result.getErrors());

            // Audit BAs and create ClBaHoursAuditResult with BCBA references
            List<ClBaHoursAuditResult> mismatches = ClBaHoursAuditor.audit(store.getExcelBas());

            tblAuditResults.setItems(FXCollections.observableArrayList(mismatches));
            lblMessage.setText(mismatches.isEmpty() ? "All BCBAs are accurate. No mismatches found." :
                    mismatches.size() + " mismatched BCBA(s) found.");

            // Enable second button
            btnOpenBAWindow.setDisable(false);
        }
    }

    @FXML
    private void handleSelectSqlCsv(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select SQL CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) btnOpenBAWindow.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                ParseResult parseResult = SqlOptParser.parseBas(selectedFile.getAbsolutePath());

                DataStore store = DataStore.getInstance();
                store.setSqlBas(parseResult.getBaMap());
                store.setSqlPts(parseResult.getPatientMap());

                // Convert to list for display
                List<BCBA> baList = new ArrayList<>(parseResult.getBaMap().values());

                // Open the BA Overview window
                BASqlOverviewWindow baWindow = new BASqlOverviewWindow(baList);
                baWindow.show();

            } catch (IOException e) {
                lblMessage.setText("Failed to parse SQL CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRunSqlVsClAudit(ActionEvent event) {
        DataStore store = DataStore.getInstance();

        if(store.getExcelBas() == null || store.getSqlBas() == null || store.getSqlPatients() == null || store.getExcelPatients() == null){
            lblMessage.setText("Please load Excel and SQL data first");
            return;
        }

        List<ClVsSqlBaAuditResult> baResults = ClVsSqlAuditor.runBaAudit(store.getExcelBas(), store.getSqlBas());
        List<ClVsSqlPtAuditResult> ptResults = ClVsSqlAuditor.runPtAudit(store.getSqlPatients(), store.getExcelPatients());

        SqlVsClAuditResultWindow.show(baResults, ptResults);
    }

    private void showErrors(List<String> errors) {
        if (errors.isEmpty()) {
            txtErrors.setText("No parsing errors.");
            txtErrors.setVisible(false);
        } else {
            txtErrors.setText(String.join("\n", errors));
            txtErrors.setVisible(true);

            int numLines = errors.size();
            double lineHeight = 20;
            double maxHeight = 200;

            txtErrors.setPrefHeight(Math.min(numLines * lineHeight, maxHeight));
        }
    }

    // New method: opens a window showing patients under a BCBA
    private void showBcbaPatients(BCBA bcba) {
        Stage stage = new Stage();
        stage.setTitle("Patients for " + bcba.getName());

        TableView<Patient> tblPatients = new TableView<>();

        TableColumn<Patient, Integer> colId = new TableColumn<>("CR ID");
        colId.setCellValueFactory(p -> new javafx.beans.property.SimpleIntegerProperty(p.getValue().getCrId()).asObject());

        TableColumn<Patient, String> colName = new TableColumn<>("Patient Name");
        colName.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(p.getValue().getName()));

        TableColumn<Patient, Double> colHours = new TableColumn<>("Hours");
        colHours.setCellValueFactory(p -> new javafx.beans.property.SimpleDoubleProperty(p.getValue().getHours()).asObject());

        TableColumn<Patient, String> colInsurance = new TableColumn<>("Insurance");
        colInsurance.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
                p.getValue().getInsurance() != null ? p.getValue().getInsurance().name() : "Unknown"));

        tblPatients.getColumns().addAll(colId, colName, colHours, colInsurance);
        tblPatients.setItems(FXCollections.observableArrayList(bcba.getPatients()));

        VBox vbox = new VBox(tblPatients);
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        Scene scene = new Scene(vbox, 500, 400);
        stage.setScene(scene);
        stage.show();
    }
}