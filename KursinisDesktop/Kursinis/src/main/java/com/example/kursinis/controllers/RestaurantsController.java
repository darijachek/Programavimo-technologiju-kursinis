package com.example.kursinis.controllers;

import com.example.kursinis.Session;
import com.example.kursinis.entities.Restaurant;
import com.example.kursinis.entities.User;
import com.example.kursinis.services.RestaurantService;
import com.example.kursinis.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class RestaurantsController {

    @FXML
    private Button newBtn, updateBtn, deleteBtn;
    @FXML private TableView<Restaurant> table;
    @FXML private TextField nameField, addressField, ownerIdField, activeField, filterField, phoneField;

    private final RestaurantService service = new RestaurantService();
    private final UserService userService   = new UserService();
    private final ObservableList<Restaurant> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        TableColumn<Restaurant, Long> c1 = new TableColumn<>("ID");        c1.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Restaurant, String>  c2 = new TableColumn<>("Pavadinimas");c2.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Restaurant, String>  c3 = new TableColumn<>("Adresas");    c3.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<Restaurant, Boolean> c4 = new TableColumn<>("Aktyvus");    c4.setCellValueFactory(new PropertyValueFactory<>("active"));
        table.getColumns().setAll(c1, c2, c3, c4);

        refresh();

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                nameField.setText(n.getName());
                addressField.setText(n.getAddress());
                activeField.setText(Boolean.toString(n.isActive()));
                ownerIdField.setText(n.getOwner() != null ? n.getOwner().getId().toString() : "");
                if (phoneField != null) phoneField.setText(n.getPhone());
            }
        });

        boolean canEdit = Session.isLoggedIn() &&
                (Session.getCurrentUser().getRole() == User.Role.ADMIN ||
                        Session.getCurrentUser().getRole() == User.Role.OWNER);
        setEditable(canEdit);

        if (Session.isLoggedIn() && Session.getCurrentUser().getRole() == User.Role.OWNER) {
            ownerIdField.setText(Session.getCurrentUser().getId().toString());
            ownerIdField.setEditable(false);
        }
        if (!canEdit) ownerIdField.setEditable(false);
    }

    private void refresh() {
        data.setAll(service.findAll());
        table.setItems(data);
    }

    private void setEditable(boolean canEdit) {
        newBtn.setDisable(!canEdit);
        updateBtn.setDisable(!canEdit);
        deleteBtn.setDisable(!canEdit);

        nameField.setEditable(canEdit);
        addressField.setEditable(canEdit);
        if (phoneField != null) phoneField.setEditable(canEdit);
        activeField.setEditable(canEdit);

        table.setEditable(false);
    }

    private boolean ensureCanEdit() {
        if (!Session.isLoggedIn()) { info("Prisijunkite."); return false; }
        var role = Session.getCurrentUser().getRole();
        if (role != User.Role.ADMIN && role != User.Role.OWNER) {
            info("Tik administratorius ar savininkas gali redaguoti restoranus.");
            return false;
        }
        return true;
    }

    public void onCreate() {
        if (!ensureCanEdit()) return;
        try {
            if (!Session.isLoggedIn()) { info("Prisijunkite prie sistemos."); return; }
            Long ownerId = parseLong(ownerIdField.getText(), "Neteisingas savininko ID");
            Restaurant r = new Restaurant();
            r.setName(nameField.getText());
            r.setAddress(addressField.getText());
            r.setActive(Boolean.parseBoolean(activeField.getText()));
            if (phoneField != null) r.setPhone(phoneField.getText());

            if (Session.getCurrentUser().getRole() == User.Role.ADMIN) {
                if (!ownerIdField.getText().isBlank()) {
                    Long id = Long.parseLong(ownerIdField.getText());
                    User owner = userService.findAll().stream()
                            .filter(u -> u.getId().equals(id))
                            .findFirst().orElse(null);
                    r.setOwner(owner);
                } else {
                    r.setOwner(null);
                }
            }
            service.createAs(Session.getCurrentUser(), r);
            refresh();
            info("Sukurtas restoranas");
        } catch (Exception ex) { error(ex); }
    }

    public void onUpdate() {
        if (!Session.isLoggedIn()) { info("Prisijunkite prie sistemos."); return; }
        if (!ensureCanEdit()) return;
        try {
            Restaurant sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }

            sel.setName(nameField.getText());
            sel.setAddress(addressField.getText());
            sel.setActive(Boolean.parseBoolean(activeField.getText()));
            if (phoneField != null) sel.setPhone(phoneField.getText());

            // admin gali pakeisti savininką; owner – ne
            if (Session.getCurrentUser().getRole() == User.Role.ADMIN && !ownerIdField.getText().isBlank()) {
                Long id = Long.parseLong(ownerIdField.getText());
                User owner = userService.findAll().stream()
                        .filter(u -> u.getId().equals(id))
                        .findFirst().orElse(null);
                sel.setOwner(owner);
            }

            service.updateAs(Session.getCurrentUser(), sel);
            refresh();
            info("Atnaujinta");
        } catch (Exception ex) { error(ex); }
    }

    public void onDelete() {
        if (!Session.isLoggedIn()) { info("Prisijunkite prie sistemos."); return; }
        if (!ensureCanEdit()) return;
        try {
            Restaurant sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }
            service.deleteAs(Session.getCurrentUser(), sel.getId());
            refresh();
            info("Ištrinta");
        } catch (Exception ex) { error(ex); }
    }

    public void onFilter() {
        String f = filterField.getText();
        if (f == null || f.isBlank()) { refresh(); return; }
        String ff = f.toLowerCase();
        table.setItems(data.filtered(r ->
                r.getName() != null && r.getName().toLowerCase().contains(ff)
                        || r.getAddress() != null && r.getAddress().toLowerCase().contains(ff)
        ));
    }

    private Long parseLong(String s, String err){
        try { return Long.parseLong(s.trim()); }
        catch (Exception e){ throw new IllegalArgumentException(err); }
    }

    private void info(String m)  { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void error(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
}
