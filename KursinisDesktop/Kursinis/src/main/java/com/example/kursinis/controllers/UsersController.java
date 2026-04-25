package com.example.kursinis.controllers;

import com.example.kursinis.Session;
import com.example.kursinis.entities.Client;
import com.example.kursinis.entities.User;
import com.example.kursinis.services.AuthService;
import com.example.kursinis.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UsersController {
    @FXML
    private TableView<User> table;
    @FXML private TextField usernameField, passwordField, filterField;
    @FXML private ComboBox<User.Role> roleBox;
    private final UserService service = new UserService();
    private final ObservableList<User> data = FXCollections.observableArrayList();

    @FXML public void initialize(){
        roleBox.getItems().setAll(User.Role.values());
        roleBox.getSelectionModel().select(User.Role.CLIENT);
        TableColumn<User, Long> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<User, String> c2 = new TableColumn<>("Vartotojo vardas");
        c2.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<User, String> c3 = new TableColumn<>("Rolė");
        c3.setCellValueFactory(new PropertyValueFactory<>("role"));
        table.getColumns().setAll(c1,c2,c3);
        refresh();
        table.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->{
            if (n!=null){ usernameField.setText(n.getUsername()); roleBox.getSelectionModel().select(n.getRole()); }
        });
    }
    private void refresh(){ data.setAll(service.findAll()); table.setItems(data); }

    public void onCreate() {
        try {
            if (!Session.isLoggedIn()) {
                info("Prisijunkite prie sistemos.");
                return;
            }

            String userTxt = usernameField.getText();
            String passTxt = passwordField.getText();

            if (userTxt == null || userTxt.isBlank()) {
                info("Vartotojo vardas negali būti tuščias");
                return;
            }
            if (passTxt == null || passTxt.isBlank()) {
                info("Slaptažodis negali būti tuščias");
                return;
            }

            var u = new User(userTxt, AuthService.hash(passTxt), roleBox.getValue());

            service.create(u);
            refresh();
            info("Sukurtas vartotojas");

            usernameField.clear();
            passwordField.clear();

        } catch (Exception ex) {
            error(ex);
        }
    }
    public void onUpdate(){
        try{
            var sel = table.getSelectionModel().getSelectedItem();
            if (sel==null){ info("Pasirinkite eilutę"); return; }
            sel.setUsername(usernameField.getText());
            if (passwordField.getText()!=null && !passwordField.getText().isBlank())
                sel.setPasswordHash(AuthService.hash(passwordField.getText()));
            sel.setRole(roleBox.getValue());
            service.update(sel); refresh(); info("Atnaujinta");
        } catch(Exception ex){ error(ex); }
    }
    public void onDelete() {
        try {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { info("Pasirinkite eilutę"); return; }
            service.delete(selected.getId()); refresh();
            info("Ištrinta");
        } catch (Exception ex) { error(ex); }
    }


    public void onFilter() {
        String f = filterField.getText().toLowerCase();
        if (f == null || f.isBlank()) { refresh(); return; }

        table.setItems(data.filtered(u ->
                u.getUsername().toLowerCase().contains(f) ||
                        u.getRole().toString().contains(f.toUpperCase()) ||
                        String.valueOf(u.getId()).contains(f) ||
                        (u instanceof Client && ((Client)u).getAddress() != null &&
                                ((Client)u).getAddress().toLowerCase().contains(f))
        ));
    }
    private void info(String m){ new Alert(Alert.AlertType.INFORMATION,m).showAndWait(); }
    private void error(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }


}
