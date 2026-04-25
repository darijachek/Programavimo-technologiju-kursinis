package com.example.kursinis.controllers;

import com.example.kursinis.Session;
import com.example.kursinis.entities.Restaurant;
import com.example.kursinis.entities.User;
import com.example.kursinis.entities.MenuItem;
import com.example.kursinis.services.MenuItemService;
import com.example.kursinis.services.RestaurantService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;

public class MenuItemsController {

    @FXML private Button newBtn, updateBtn, deleteBtn;
    @FXML private TableView<MenuItem> table;
    @FXML private TextField titleField, categoryField, descriptionField, basePriceField;
    @FXML private ComboBox<Restaurant> restaurantBox;
    @FXML private TextField filterField;

    private final MenuItemService service = new MenuItemService();
    private final RestaurantService restaurantService = new RestaurantService();
    private final ObservableList<MenuItem> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        TableColumn<MenuItem, Long> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MenuItem, String> c2 = new TableColumn<>("Pavad.");
        c2.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<MenuItem, String> c3 = new TableColumn<>("Rest.");
        c3.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getRestaurant() == null ? "" : cell.getValue().getRestaurant().getName()
        ));

        TableColumn<MenuItem, String> c4 = new TableColumn<>("Kaina");
        c4.setCellValueFactory(mi -> new SimpleStringProperty(
                mi.getValue().getBasePrice() == null ? "" : mi.getValue().getBasePrice().toPlainString()
        ));

        TableColumn<MenuItem, String> c5 = new TableColumn<>("Kat.");
        c5.setCellValueFactory(new PropertyValueFactory<>("category"));

        table.getColumns().setAll(c1, c2, c3, c4, c5);

        restaurantBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                refreshTable(newVal);
            } else {
                table.getItems().clear();
            }
        });

        setupRestaurantBox();


        table.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (n != null) {
                titleField.setText(n.getTitle());
                descriptionField.setText(n.getDescription());
                basePriceField.setText(n.getBasePrice() != null ? n.getBasePrice().toPlainString() : "");
                categoryField.setText(n.getCategory() != null ? n.getCategory() : "");

                if (n.getRestaurant() != null) {
                    for (Restaurant r : restaurantBox.getItems()) {
                        if (r.getId().equals(n.getRestaurant().getId())) {
                            restaurantBox.setValue(r);
                            break;
                        }
                    }
                }
            }
        });

        boolean canEdit = Session.isLoggedIn() &&
                (Session.getCurrentUser().getRole() == User.Role.ADMIN ||
                        Session.getCurrentUser().getRole() == User.Role.OWNER);
        setEditable(canEdit);
    }

    private void setupRestaurantBox() {
        List<Restaurant> allRestaurants = restaurantService.findAll();
        User currentUser = Session.getCurrentUser();

        restaurantBox.setConverter(new StringConverter<Restaurant>() {
            @Override
            public String toString(Restaurant r) {
                return r == null ? "" : r.getName() + " (ID: " + r.getId() + ")";
            }
            @Override
            public Restaurant fromString(String string) { return null; }
        });

        if (currentUser.getRole() == User.Role.OWNER) {
            List<Restaurant> myRestaurants = allRestaurants.stream()
                    .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(currentUser.getId()))
                    .toList();
            restaurantBox.getItems().setAll(myRestaurants);
            if (!myRestaurants.isEmpty()) {
                restaurantBox.getSelectionModel().selectFirst();
            }
        } else {
            // Admin mato visus
            restaurantBox.getItems().setAll(allRestaurants);
            if (!allRestaurants.isEmpty()) {
                restaurantBox.getSelectionModel().selectFirst();
            }
        }
    }

    private void refreshTable(Restaurant r) {
        if (r == null) {
            table.getItems().clear();
            return;
        }

        System.out.println("DEBUG: Ieškoma meniu restoranui ID=" + r.getId());
        List<MenuItem> items = service.findByRestaurant(r.getId());
        System.out.println("DEBUG: Rasta patiekalų: " + items.size());

        data.setAll(items);
        table.setItems(data);
    }

    private void setEditable(boolean canEdit){
        newBtn.setDisable(!canEdit);
        updateBtn.setDisable(!canEdit);
        deleteBtn.setDisable(!canEdit);
        titleField.setEditable(canEdit);
        descriptionField.setEditable(canEdit);
        basePriceField.setEditable(canEdit);
        categoryField.setEditable(canEdit);
        restaurantBox.setDisable(true);
        if (Session.getCurrentUser().getRole() == User.Role.ADMIN) {
            restaurantBox.setDisable(false);
        }
    }

    public void onCreate(){
        if (!Session.isLoggedIn()) return;
        try {
            Restaurant selectedRest = restaurantBox.getValue();
            if (selectedRest == null) {
                info("Pasirinkite restoraną iš sąrašo.");
                return;
            }

            MenuItem m = new MenuItem();
            m.setTitle(titleField.getText());
            m.setDescription(descriptionField.getText());
            m.setCategory(emptyToNull(categoryField.getText()));
            m.setBasePrice(parsePrice(basePriceField.getText()));

            service.createAs(Session.getCurrentUser(), m, selectedRest.getId());

            refreshTable(selectedRest);
            info("Patiekalas sukurtas");
        } catch (Exception ex) { error(ex); }
    }

    public void onUpdate(){
        if (!Session.isLoggedIn()) return;
        try {
            MenuItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }

            sel.setTitle(titleField.getText());
            sel.setDescription(descriptionField.getText());
            sel.setCategory(emptyToNull(categoryField.getText()));
            sel.setBasePrice(parsePrice(basePriceField.getText()));

            Long restId = sel.getRestaurant() != null ? sel.getRestaurant().getId() : null;

            service.updateAs(Session.getCurrentUser(), sel, restId);

            refreshTable(restaurantBox.getValue());
            info("Atnaujinta");
        } catch (Exception ex) { error(ex); }
    }

    public void onDelete(){
        if (!Session.isLoggedIn()) return;
        try {
            MenuItem sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }

            service.deleteAs(Session.getCurrentUser(), sel.getId());
            refreshTable(restaurantBox.getValue());
            info("Ištrinta");
        } catch (Exception ex) { error(ex); }
    }

    public void onFilter(){
        String f = filterField.getText();
        if (f == null || f.isBlank()) {
            refreshTable(restaurantBox.getValue());
            return;
        }
        String ff = f.toLowerCase();
        table.setItems(data.filtered(m ->
                (m.getTitle()!=null && m.getTitle().toLowerCase().contains(ff)) ||
                        (m.getCategory()!=null && m.getCategory().toLowerCase().contains(ff))
        ));
    }

    private BigDecimal parsePrice(String s){
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Neteisingas kainos formatas. Pvz.: 9.99");
        }
    }

    private static String emptyToNull(String s){
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void error(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
}