package com.example.kursinis.controllers;

import com.example.kursinis.Session;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.Order;
import com.example.kursinis.entities.OrderItem;
import com.example.kursinis.entities.Restaurant;
import com.example.kursinis.entities.User;
import com.example.kursinis.services.OrderItemService;
import com.example.kursinis.services.OrderService;
import jakarta.persistence.EntityManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter; // Pridėtas importas
import java.util.List;

public class OrdersController {

    @FXML
    private TableView<Order> table;
    @FXML private TextField clientIdField, restaurantIdField, statusField, filterField;

    private final OrderService service = new OrderService();
    private final ObservableList<Order> data = FXCollections.observableArrayList();

    @FXML private TableView<OrderItem> itemsTable;
    @FXML private TextField menuItemIdField, qtyField;
    @FXML private Label totalLabel;
    @FXML private TextField qtyUpdateField;

    private final OrderItemService itemService = new OrderItemService();
    private final ObservableList<OrderItem> itemsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        TableColumn<Order, Long> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, String> c2 = new TableColumn<>("Klientas");
        c2.setCellValueFactory(new PropertyValueFactory<>("client"));

        TableColumn<Order, String> c3 = new TableColumn<>("Restoranas");
        c3.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getRestaurant() != null ? cell.getValue().getRestaurant().getName() : "?"
        ));

        TableColumn<Order, String> c4 = new TableColumn<>("Statusas");
        c4.setCellValueFactory(new PropertyValueFactory<>("status"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        TableColumn<Order, String> c5 = new TableColumn<>("Sukurta");
        c5.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getCreatedAt() != null ? cell.getValue().getCreatedAt().format(formatter) : ""
        ));

        table.getColumns().setAll(c1, c2, c3, c4, c5);

        TableColumn<OrderItem, Long> i1 = new TableColumn<>("ID");
        i1.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<OrderItem, String> i2 = new TableColumn<>("Prekė");
        i2.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMenuItem() == null ? "" : c.getValue().getMenuItem().getTitle()
        ));

        TableColumn<OrderItem, Integer> i3 = new TableColumn<>("Kiekis");
        i3.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<OrderItem, String> i4 = new TableColumn<>("Kaina (dab.)");
        i4.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMenuItem() != null && c.getValue().getMenuItem().getCurrentPrice() != null
                        ? c.getValue().getMenuItem().getCurrentPrice().toPlainString() : ""
        ));

        TableColumn<OrderItem, String> i5 = new TableColumn<>("Suma");
        i5.setCellValueFactory(c -> new SimpleStringProperty(
                itemService.lineTotal(c.getValue()).toPlainString()
        ));
        itemsTable.getColumns().setAll(i1, i2, i3, i4, i5);

        table.getSelectionModel().selectedItemProperty().addListener((o, old, ord) -> loadItems(ord));

        refresh();

        table.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (n != null) {
                clientIdField.setText(n.getClient() != null ? String.valueOf(n.getClient().getId()) : "");
                restaurantIdField.setText(n.getRestaurant() != null ? String.valueOf(n.getRestaurant().getId()) : "");
                statusField.setText(n.getStatus().name());

                boolean isOwner = Session.getCurrentUser().getRole() == User.Role.OWNER;

                clientIdField.setEditable(!isOwner);
                restaurantIdField.setEditable(!isOwner);
            }
        });
    }

    private void refresh() {
        User current = Session.getCurrentUser();
        List<Order> allOrders = service.findAll();

        if (current.getRole() == User.Role.OWNER) {
            data.setAll(allOrders.stream()
                    .filter(o -> o.getRestaurant() != null &&
                            o.getRestaurant().getOwner().getId().equals(current.getId()))
                    .toList());
        } else {
            data.setAll(allOrders);
        }
        table.setItems(data);
    }

    public void onCreate() {
        try {
            if (!Session.isLoggedIn()) { info("Prisijunkite prie sistemos."); return; }
            Long clientId = parseLong(clientIdField.getText(), "Neteisingas kliento ID");
            Long restId = parseLong(restaurantIdField.getText(), "Neteisingas restorano ID");
            User client = find(User.class, clientId);
            Restaurant rest = find(Restaurant.class, restId);
            if (client == null) throw new IllegalArgumentException("Klientas nerastas.");
            if (rest == null) throw new IllegalArgumentException("Restoranas nerastas.");

            Order o = new Order(client, rest);
            o.setStatus(parseStatus(statusField.getText()));
            service.create(o);

            refresh();
            clearHeaderForm();
            info("Sukurtas užsakymas");
        } catch (Exception ex) { error(ex); }
    }

    public void onUpdate() {
        try {
            Order sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }
            User currentUser = Session.getCurrentUser();

            boolean isOrderInProgress = sel.getStatus() == Order.Status.TAKEN ||
                    sel.getStatus() == Order.Status.DELIVERING ||
                    sel.getStatus() == Order.Status.DONE;

            if (currentUser.getRole() == User.Role.OWNER) {
                if (!clientIdField.getText().equals(String.valueOf(sel.getClient().getId()))) {
                    throw new SecurityException("Restoranas negali keisti kliento.");
                }
                if (!restaurantIdField.getText().equals(String.valueOf(sel.getRestaurant().getId()))) {
                    throw new SecurityException("Negalite perkelti užsakymo kitam restoranui.");
                }
            }

            if (isOrderInProgress) {
                if (!clientIdField.getText().equals(String.valueOf(sel.getClient().getId()))) {
                    error(new Exception("Negalima keisti pirkėjo, kai užsakymas jau vykdomas/baigtas"));
                    return;
                }
            }

            if (!isBlank(clientIdField.getText())) {
                Long id = parseLong(clientIdField.getText(), "Neteisingas kliento ID");
                User c = find(User.class, id);
                if (c == null) throw new IllegalArgumentException("Klientas nerastas.");
                sel.setClient(c);
            }

            if (!isBlank(restaurantIdField.getText())) {
                Long id = parseLong(restaurantIdField.getText(), "Neteisingas restorano ID");
                Restaurant r = find(Restaurant.class, id);
                if (r == null) throw new IllegalArgumentException("Restoranas nerastas.");
                sel.setRestaurant(r);
            }

            Order.Status newStatus = parseStatus(statusField.getText());
            sel.setStatus(newStatus);

            service.update(sel);
            refresh();
            info("Užsakymas atnaujintas (Statusas: " + newStatus + ")");
        } catch (Exception ex) { error(ex); }
    }

    public void onDelete() {
        try {
            Order sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { info("Pasirinkite eilutę"); return; }
            service.delete(sel.getId());
            refresh();
            itemsData.clear(); itemsTable.setItems(itemsData); updateTotal();
            info("Ištrinta");
        } catch (Exception ex) { error(ex); }
    }

    public void onFilter() {
        String f = filterField.getText();
        if (isBlank(f)) { refresh(); return; }
        String ff = f.toLowerCase();

        table.setItems(data.filtered(o -> {
            boolean statusMatch = o.getStatus().name().toLowerCase().contains(ff);

            boolean clientMatch = o.getClient() != null && (
                    String.valueOf(o.getClient().getId()).contains(ff) ||
                            o.getClient().getUsername().toLowerCase().contains(ff)
            );

            boolean restMatch = o.getRestaurant() != null && (
                    String.valueOf(o.getRestaurant().getId()).contains(ff) ||
                            o.getRestaurant().getName().toLowerCase().contains(ff)
            );

            boolean dateMatch = o.getCreatedAt() != null &&
                    o.getCreatedAt().toString().contains(ff);

            return statusMatch || clientMatch || restMatch || dateMatch;
        }));
    }


    private void loadItems(Order order) {
        itemsData.clear();
        if (order != null) {
            itemsData.setAll(itemService.findByOrder(order.getId()));
        }
        itemsTable.setItems(itemsData);
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal sum = itemsData.stream()
                .map(itemService::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Suma: " + sum.toPlainString());
    }

    @FXML
    public void onAddItem() {
        Order ord = table.getSelectionModel().getSelectedItem();
        if (ord == null) { info("Pirma pasirinkite užsakymą."); return; }
        try {
            Long menuId = parseLong(menuItemIdField.getText(), "Neteisingas meniu ID");
            int qty = Math.max(1, parseInt(qtyField.getText(), "Neteisingas kiekis"));
            itemService.addItem(ord.getId(), menuId, qty);
            loadItems(ord);
        } catch (Exception ex) { error(ex); }
    }

    @FXML
    public void onRemoveItem() {
        OrderItem sel = itemsTable.getSelectionModel().getSelectedItem();
        Order ord = table.getSelectionModel().getSelectedItem();
        if (sel == null || ord == null) { info("Pasirinkite eilutę ir užsakymą."); return; }
        try {
            itemService.removeItem(sel.getId());
            loadItems(ord);
        } catch (Exception ex) { error(ex); }
    }

    @FXML
    public void onUpdateItemQty(){
        OrderItem sel = itemsTable.getSelectionModel().getSelectedItem();
        Order ord = table.getSelectionModel().getSelectedItem();
        if (sel == null || ord == null) { info("Pasirinkite eilutę ir užsakymą."); return; }
        try {
            int qty = Math.max(1, Integer.parseInt(qtyUpdateField.getText().trim()));
            itemService.updateQuantity(sel.getId(), qty);
            loadItems(ord);
            qtyUpdateField.clear();
        } catch (Exception ex) { error(ex); }
    }


    private static boolean isBlank(String s){ return s == null || s.isBlank(); }

    private Long parseLong(String s, String err){
        try { return Long.parseLong(s.trim()); }
        catch (Exception e){ throw new IllegalArgumentException(err); }
    }
    private int parseInt(String s, String err){
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e){ throw new IllegalArgumentException(err); }
    }
    private Order.Status parseStatus(String s){
        if (isBlank(s)) return Order.Status.NEW;
        try { return Order.Status.valueOf(s.trim().toUpperCase()); }
        catch (Exception e){ return Order.Status.NEW; }
    }

    private <T> T find(Class<T> type, Long id){
        EntityManager em = JPAUtil.getEntityManager();
        try { return em.find(type, id); }
        finally { em.close(); }
    }

    private void clearHeaderForm(){
        clientIdField.clear();
        restaurantIdField.clear();
        statusField.clear();
    }


    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void error(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); }
}