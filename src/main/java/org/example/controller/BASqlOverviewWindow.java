package org.example.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Avail;
import org.example.model.BCBA;
import org.example.model.Insurance;
import org.example.model.Patient;

import java.util.List;
import java.util.Map;

public class BASqlOverviewWindow {
    private List<BCBA> baList;

    public BASqlOverviewWindow(List<BCBA> baList) {
        this.baList = baList;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("BA and Patient Overview");

        TreeTableView<Object> treeTable = new TreeTableView<>();

        // --- BA Columns ---
        TreeTableColumn<Object, String> colBAName = new TreeTableColumn<>("BA Name");
        colBAName.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof BCBA) return new SimpleStringProperty(((BCBA) value).getName());
            return new SimpleStringProperty("");
        });

        TreeTableColumn<Object, String> colBAType = new TreeTableColumn<>("BA Type");
        colBAType.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof BCBA) return new SimpleStringProperty(((BCBA) value).getType().name());
            return new SimpleStringProperty("");
        });

        TreeTableColumn<Object, Number> colDesiredHours = new TreeTableColumn<>("Desired Hours");
        colDesiredHours.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof BCBA) return new SimpleDoubleProperty(((BCBA) value).getDesiredHours());
            return new SimpleDoubleProperty(0);
        });

        TreeTableColumn<Object, Number> colAssignedHoursBA = new TreeTableColumn<>("Assigned Hours");
        colAssignedHoursBA.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof BCBA) return new SimpleDoubleProperty(((BCBA) value).getAssignedHours());
            return new SimpleDoubleProperty(0);
        });

        // --- Patient Columns ---
        TreeTableColumn<Object, String> colClient = new TreeTableColumn<>("Client");
        colClient.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof Patient) return new SimpleStringProperty(((Patient) value).getName());
            return new SimpleStringProperty("");
        });

        TreeTableColumn<Object, Integer> colClientId = new TreeTableColumn<>("Client ID");
        colClientId.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof Patient) return new SimpleIntegerProperty(((Patient) value).getCrId()).asObject();
            return new SimpleIntegerProperty(0).asObject();
        });

        TreeTableColumn<Object, Number> colAssignedHoursPatient = new TreeTableColumn<>("Assigned Hours");
        colAssignedHoursPatient.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof Patient) return new SimpleDoubleProperty(((Patient) value).getHours());
            return new SimpleDoubleProperty(0);
        });

        TreeTableColumn<Object, String> colInsurance = new TreeTableColumn<>("Insurance");
        colInsurance.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof Patient){
                Insurance insurance = ((Patient) value).getInsurance();
                return new SimpleStringProperty(insurance != null ? insurance.toString() : "N/A");
            }
            return new SimpleStringProperty("");
        });

        TreeTableColumn<Object, String> colAvailability = new TreeTableColumn<>("Avail");
        colAvailability.setCellValueFactory(param -> {
            Object value = param.getValue().getValue();
            if (value instanceof Patient) {
                Avail avail = ((Patient) value).getAvailability();
                return new SimpleStringProperty(avail != null ? avail.toString() : "N/A");
            }
            return new SimpleStringProperty("");
        });

        // Add all columns
        treeTable.getColumns().addAll(colBAName, colBAType, colDesiredHours, colAssignedHoursBA,
                colClient, colClientId, colAssignedHoursPatient, colInsurance, colAvailability);

        // Root
        TreeItem<Object> root = new TreeItem<>("Root");
        root.setExpanded(true);

        // Build tree: BA -> Patients
        for (BCBA ba : baList) {
            TreeItem<Object> baItem = new TreeItem<>(ba);
            if (ba.getPatients() != null) {
                for (Patient p : ba.getPatients()) {
                    baItem.getChildren().add(new TreeItem<>(p));
                }
            }
            root.getChildren().add(baItem);
        }

        treeTable.setRoot(root);
        treeTable.setShowRoot(false);

        VBox rootBox = new VBox(treeTable);
        rootBox.setSpacing(10);

        Scene scene = new Scene(rootBox, 1000, 600);
        stage.setScene(scene);
        stage.show();
    }
}
