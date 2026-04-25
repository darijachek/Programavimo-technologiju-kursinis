package com.example.kursinis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var url = Objects.requireNonNull(
                getClass().getResource("/fxml/login-view.fxml"),
                "login-view.fxml not found on classpath"
        );

        Parent root = FXMLLoader.load(url);
        stage.setTitle("Prisijungimas");
        stage.setScene(new Scene(root, 600, 300));
        stage.show();
    }

    @Override public void stop() {  }

    public static void main(String[] args) { launch(); }
}
