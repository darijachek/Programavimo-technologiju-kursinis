package com.example.kursinis.controllers;

import com.example.kursinis.MainApp;
import com.example.kursinis.Session;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private Label statusLabel;
    @FXML private TabPane tabPane;

    @FXML public void initialize() {
        updateStatus();
        applyRoleVisibility();
        selectFirstAvailableTab();
    }

    @FXML public void connectDB(){ JPAUtil.connect(); updateStatus(); info("Prisijungta prie DB"); }
    @FXML public void disconnectDB(){ JPAUtil.disconnect(); updateStatus(); info("Atsijungta nuo DB"); }
    @FXML public void about(ActionEvent e){ info("Maisto rezervavimo/valdymo sistema"); }
    @FXML public void exitApp(){ Platform.exit(); }
    @FXML
    public void onLogout() {
        try {
            Session.setCurrentUser(null);

            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/login-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setTitle("Prisijungimas");
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyRoleVisibility() {
        User u = Session.getCurrentUser();
        if (u == null || tabPane == null) return;

        switch (u.getRole()) {
            case ADMIN -> {}
            case OWNER -> tabPane.getTabs().get(0).setDisable(true);
            case CLIENT -> tabPane.getTabs().get(0).setDisable(true);
            case DRIVER -> {
                tabPane.getTabs().get(0).setDisable(true);
                tabPane.getTabs().get(1).setDisable(true);
                tabPane.getTabs().get(2).setDisable(true);
            }
        }
    }

    private void selectFirstAvailableTab() {
        for (Tab t : tabPane.getTabs()) {
            if (!t.isDisable()) {
                tabPane.getSelectionModel().select(t);
                return;
            }
        }
    }

    private void updateStatus(){
        String who = Session.isLoggedIn() ? Session.getCurrentUser().toString() : "svečias";
        statusLabel.setText((JPAUtil.isConnected() ? "DB: ON" : "DB: OFF") + " | " + who);
    }

    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
}
