package com.example.kursinis.controllers;

import com.example.kursinis.Session;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.*;
import com.example.kursinis.services.AuthService;
import com.example.kursinis.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label msgLabel;
    private final AuthService auth = new AuthService();
    private final UserService users = new UserService();

    @FXML public void initialize(){ JPAUtil.connect(); }

    @FXML
    public void onLogin(javafx.event.ActionEvent e) {
        try {
            String u = usernameField.getText();
            String p = passwordField.getText();

            User user = auth.findByUsername(u);
            if (user != null && AuthService.check(p, user.getPasswordHash())) {
                if (user.getRole() == User.Role.CLIENT || user.getRole() == User.Role.DRIVER) {
                    msgLabel.setText("Klaida: Desktop skirta tik Admin/Savininkams");
                    return;
                }
                Session.setCurrentUser(user);

                Parent root = FXMLLoader.load(getClass().getResource("/fxml/main-view.fxml"));
                Stage st = (Stage) usernameField.getScene().getWindow();
                st.setScene(new Scene(root, 1100, 700));
                st.setTitle("Maisto sistema");
            } else {
                msgLabel.setText("Neteisingi prisijungimo duomenys");
            }
        } catch (Exception ex) {
            msgLabel.setText("Klaida: " + ex.getMessage());
        }
    }

    @FXML
    public void onSeed(javafx.event.ActionEvent e) {
        if (auth.findByUsername("admin")   == null) users.create(new Admin("admin",   AuthService.hash("admin")));
        if (auth.findByUsername("owner1")  == null) users.create(new Owner("owner1",  AuthService.hash("owner")));
        if (auth.findByUsername("client1") == null) users.create(new Client("client1",AuthService.hash("client")));
        if (auth.findByUsername("driver1") == null) users.create(new Driver("driver1",AuthService.hash("driver")));
        msgLabel.setText("Sukurtos demo paskyros: admin/owner1/client1/driver1");
    }
}
