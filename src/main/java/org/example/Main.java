package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

//TODO: Fix the BATs desired hours potentially use BATCaseload??
//TODO: Add a button that has pts who have termed, but aren't out of CR yet.

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view/excel_view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("BA Excel Extractor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}