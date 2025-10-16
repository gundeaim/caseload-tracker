package org.example.controller;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.results.ClVsSqlBaAuditResult;
import org.example.model.results.ClVsSqlPtAuditResult;

import java.util.List;

public class SqlVsClAuditResultWindow {

    public static void show(List<ClVsSqlBaAuditResult> baResults,
                            List<ClVsSqlPtAuditResult> ptResults) {

        Stage resultStage = new Stage();
        resultStage.setTitle("SQL vs CL Audit Results");

        TabPane tabPane = new TabPane();

        // BA tab
        TableView<ClVsSqlBaAuditResult> baTable = new TableView<>();
        baTable.setItems(FXCollections.observableArrayList(baResults));

        TableColumn<ClVsSqlBaAuditResult, String> baNameCol = new TableColumn<>("BA Name");
        baNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBaName()));

        TableColumn<ClVsSqlBaAuditResult, String> baStatusCol = new TableColumn<>("Status");
        baStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        baTable.getColumns().addAll(baNameCol, baStatusCol);
        tabPane.getTabs().add(new Tab("BA Audit", baTable));

        // Patient tab
        TableView<ClVsSqlPtAuditResult> ptTable = new TableView<>();
        ptTable.setItems(FXCollections.observableArrayList(ptResults));

        TableColumn<ClVsSqlPtAuditResult, String> ptNameCol = new TableColumn<>("Patient Name");
        ptNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<ClVsSqlPtAuditResult, Number> ptCrIdCol = new TableColumn<>("CR ID");

        ptCrIdCol.setCellValueFactory(data -> new ReadOnlyIntegerWrapper(data.getValue().getClientId()));

        TableColumn<ClVsSqlPtAuditResult, String> ptStatusCol = new TableColumn<>("Status");
        ptStatusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        ptTable.getColumns().addAll(ptNameCol, ptCrIdCol, ptStatusCol);
        tabPane.getTabs().add(new Tab("Patient Audit", ptTable));

        // Finalize stage
        Scene scene = new Scene(tabPane, 800, 600);
        resultStage.setScene(scene);
        resultStage.show();
    }
}