package com.example.kursinis.controllers;

import com.example.kursinis.Session;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.Review;
import com.example.kursinis.entities.User;
import com.example.kursinis.entities.Restaurant;
import com.example.kursinis.entities.Driver;
import com.example.kursinis.entities.Client;
import com.example.kursinis.services.ReviewService;
import jakarta.persistence.EntityManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;

public class ReviewsController {

    @FXML private TableView<Review> table;
    @FXML private TextField filterField, restaurantIdField, driverIdField, clientIdField, ratingField, commentField;
    @FXML private ComboBox<Review.TargetType> targetBox;

    private final ReviewService service = new ReviewService();
    private final ObservableList<Review> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm");

        TableColumn<Review, Long> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Review, String> c2 = new TableColumn<>("Autorius");
        c2.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getAuthor() == null ? "?" : cell.getValue().getAuthor().getUsername()
        ));

        TableColumn<Review, Integer> c3 = new TableColumn<>("Įvert.");
        c3.setCellValueFactory(new PropertyValueFactory<>("rating"));

        TableColumn<Review, String> c4 = new TableColumn<>("Komentaras");
        c4.setCellValueFactory(new PropertyValueFactory<>("comment"));

        TableColumn<Review, String> c5 = new TableColumn<>("Data");
        c5.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCreatedAt() == null ? "" : cd.getValue().getCreatedAt().format(fmt)
        ));

        table.getColumns().setAll(c1, c2, c3, c4, c5);

        var role = Session.isLoggedIn() ? Session.getCurrentUser().getRole() : User.Role.CLIENT;
        Set<Review.TargetType> allowed = allowedTargets(role);

        targetBox.getItems().setAll(allowed);
        if (!allowed.isEmpty()) {
            targetBox.getSelectionModel().select(allowed.iterator().next());
        }

        targetBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> toggleTargetFields(val));
        toggleTargetFields(targetBox.getValue());

        table.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (n != null) {
                if (n.getRestaurant() != null) {
                    targetBox.getSelectionModel().select(Review.TargetType.RESTAURANT);
                    restaurantIdField.setText(n.getRestaurant().getId().toString());
                    driverIdField.clear(); clientIdField.clear();
                } else if (n.getDriver() != null) {
                    targetBox.getSelectionModel().select(Review.TargetType.DRIVER);
                    driverIdField.setText(n.getDriver().getId().toString());
                    restaurantIdField.clear(); clientIdField.clear();
                } else if (n.getClient() != null) {
                    targetBox.getSelectionModel().select(Review.TargetType.CLIENT);
                    clientIdField.setText(n.getClient().getId().toString());
                    restaurantIdField.clear(); driverIdField.clear();
                }
                ratingField.setText(Integer.toString(n.getRating()));
                commentField.setText(n.getComment());
            }
        });

        refresh();
    }

    private void toggleTargetFields(Review.TargetType t) {
        if (t == null) return;
        boolean rest = t == Review.TargetType.RESTAURANT;
        boolean driv = t == Review.TargetType.DRIVER;
        boolean cli  = t == Review.TargetType.CLIENT;

        restaurantIdField.setDisable(!rest);
        driverIdField.setDisable(!driv);
        clientIdField.setDisable(!cli);

        if (!rest) restaurantIdField.clear();
        if (!driv) driverIdField.clear();
        if (!cli)  clientIdField.clear();
    }

    private void refresh() {
        try {
            var reviews = service.findAll();
            System.out.println("DEBUG: Rasta atsiliepimų: " + reviews.size());
            data.setAll(reviews);
            table.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void onCreate() {
        try {
            User me = Session.getCurrentUser();
            if (!Session.isLoggedIn()) { info("Prisijunkite prie sistemos."); return; }

            Review r = new Review();
            r.setAuthor(me);
            r.setTargetType(targetBox.getValue());
            try {
                r.setRating(Integer.parseInt(ratingField.getText()));
            } catch (NumberFormatException e) { throw new IllegalArgumentException("Įvertinimas turi būti skaičius."); }
            r.setComment(commentField.getText());

            EntityManager em = JPAUtil.getEntityManager();
            try {
                switch (targetBox.getValue()) {
                    case RESTAURANT -> {
                        long id = parseId(restaurantIdField.getText());
                        r.setRestaurant(em.find(Restaurant.class, id));
                        if (r.getRestaurant()==null) throw new IllegalArgumentException("Restoranas nerastas.");
                    }
                    case DRIVER -> {
                        long id = parseId(driverIdField.getText());
                        r.setDriver(em.find(Driver.class, id));
                        if (r.getDriver()==null) throw new IllegalArgumentException("Vairuotojas nerastas.");
                    }
                    case CLIENT -> {
                        long id = parseId(clientIdField.getText());
                        r.setClient(em.find(Client.class, id));
                        if (r.getClient()==null) throw new IllegalArgumentException("Klientas nerastas.");
                    }
                }
            } finally { em.close(); }

            validateReview(r, me);
            service.create(r);
            refresh();
            info("Atsiliepimas išsaugotas.");
        } catch (Exception ex) { error(ex); }
    }

    @FXML
    public void onUpdate() {
        try {
            if (!Session.isLoggedIn()) { info("Prisijunkite prie sistemos."); return; }
            Review sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }

            User me = Session.getCurrentUser();
            if (me.getRole()!=User.Role.ADMIN && !me.getId().equals(sel.getAuthor().getId()))
                throw new SecurityException("Galite redaguoti tik savo atsiliepimą.");

            sel.setRating(Integer.parseInt(ratingField.getText()));
            sel.setComment(commentField.getText());

            service.update(sel);
            refresh();
            info("Atnaujinta.");
        } catch (Exception ex) { error(ex); }
    }

    @FXML
    public void onDelete() {
        try {
            if (!Session.isLoggedIn()) { info("Prisijunkite prie sistemos."); return; }
            Review sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }

            User me = Session.getCurrentUser();
            if (me.getRole()!=User.Role.ADMIN && !me.getId().equals(sel.getAuthor().getId()))
                throw new SecurityException("Galite trinti tik savo atsiliepimą.");

            service.delete(sel.getId());
            refresh();
            info("Ištrinta.");
        } catch (Exception ex) { error(ex); }
    }

    @FXML
    public void onFilter() {
        String f = filterField.getText();
        if (f == null || f.isBlank()) { refresh(); return; }

        Integer min = null;
        try { min = Integer.parseInt(f.trim()); } catch (Exception ignored) {}

        Integer finalMin = min;
        table.setItems(data.filtered(rv ->
                (finalMin != null && rv.getRating() >= finalMin)
                        || (rv.getComment()!=null && rv.getComment().toLowerCase().contains(f.toLowerCase()))
        ));
    }

    private void validateReview(Review r, User current){
        if (r.getRating() < 1 || r.getRating() > 5)
            throw new IllegalArgumentException("Įvertinimas turi būti 1..5.");

        int targets = (r.getRestaurant()!=null?1:0) + (r.getDriver()!=null?1:0) + (r.getClient()!=null?1:0);
        if (targets != 1) throw new IllegalArgumentException("Pasirinkite tik vieną tikslą (Restoraną, Vairuotoją arba Klientą).");

        switch (current.getRole()){
            case CLIENT -> {
                if (r.getClient()!=null) throw new SecurityException("Klientas negali vertinti klientų.");
            }
            case DRIVER -> {
                if (r.getRestaurant()!=null || r.getDriver()!=null) throw new SecurityException("Vairuotojas gali vertinti tik klientus.");
            }
            case OWNER -> {
                if (r.getRestaurant()!=null) throw new SecurityException("Restoranas negali vertinti restoranų.");
            }
            case ADMIN -> { }
        }

        if (!allowedTargets(current.getRole()).contains(r.getTargetType()))
            throw new SecurityException("Jūsų rolei šis vertinimo tipas neleidžiamas.");
    }

    private long parseId(String s){
        if (s==null || s.isBlank()) throw new IllegalArgumentException("ID privalomas.");
        return Long.parseLong(s.trim());
    }

    private Set<Review.TargetType> allowedTargets(User.Role role) {
        return switch (role) {
            case ADMIN -> EnumSet.allOf(Review.TargetType.class);
            case CLIENT -> EnumSet.of(Review.TargetType.RESTAURANT, Review.TargetType.DRIVER);
            case DRIVER -> EnumSet.of(Review.TargetType.CLIENT);
            case OWNER -> EnumSet.of(Review.TargetType.CLIENT, Review.TargetType.DRIVER);
        };
    }


    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void error(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
}