package com.example.kursinis.controllers;

import com.example.kursinis.Session;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.Message;
import com.example.kursinis.entities.Order;
import com.example.kursinis.entities.User;
import com.example.kursinis.services.MessageService;
import com.example.kursinis.services.OrderService;
import jakarta.persistence.EntityManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MessagesController {

    @FXML private ListView<Order> chatList;
    @FXML private ListView<Message> messageList;
    @FXML private TextField messageField;
    @FXML private Button sendBtn;
    @FXML private VBox chatArea;
    @FXML private TextField searchField;

    private final MessageService messageService = new MessageService();
    private final OrderService orderService = new OrderService();

    private final ObservableList<Order> chatsData = FXCollections.observableArrayList();
    private final ObservableList<Message> messagesData = FXCollections.observableArrayList();

    private Order currentOrder = null;

    @FXML
    public void initialize() {
        User currentUser = Session.getCurrentUser();

        if (currentUser.getRole() == User.Role.ADMIN) {
            messageField.setDisable(true);
            messageField.setPromptText("Administratorius negali rašyti žinučių.");
            sendBtn.setDisable(true);
        }

        chatList.setItems(chatsData);
        chatList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Order item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String clientName = item.getClient() != null ? item.getClient().getUsername() : "Nežinomas";
                    String restName = item.getRestaurant() != null ? item.getRestaurant().getName() : "?";
                    setText("Užsakymas #" + item.getId() + "\nKlientas: " + clientName + "\nRestoranas: " + restName);
                }
            }
        });

        messageList.setItems(messagesData);
        messageList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    setContextMenu(null);
                } else {
                    String sender = item.getSender() != null ? item.getSender().getUsername() : "Sistema";
                    String time = item.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm"));
                    setText(sender + " [" + time + "]:\n" + item.getText());

                    if (item.getSender() != null && item.getSender().getId().equals(currentUser.getId())) {
                        setStyle("-fx-background-color: #e3f2fd; -fx-padding: 5px;");
                        setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    } else {
                        setStyle("-fx-padding: 5px;");
                        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    }

                    if (currentUser.getRole() == User.Role.ADMIN) {
                        ContextMenu contextMenu = new ContextMenu();

                        MenuItem editItem = new MenuItem("Redaguoti");
                        editItem.setOnAction(e -> editMessage(item));

                        MenuItem deleteItem = new MenuItem("Ištrinti");
                        deleteItem.setOnAction(e -> deleteMessage(item));

                        contextMenu.getItems().addAll(editItem, deleteItem);
                        setContextMenu(contextMenu);
                    } else {
                        setContextMenu(null); // Kitiems vartotojams meniu nėra
                    }
                }
            }
        });

        // Pasirinkus pokalbį
        chatList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                currentOrder = newVal;
                loadMessages();
                chatArea.setDisable(false);
            } else {
                currentOrder = null;
                messagesData.clear();
                chatArea.setDisable(true);
            }
        });

        chatArea.setDisable(true);
        refreshChats();

        // Periodinis atnaujinimas
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            if (currentOrder != null) {
                loadMessages();
            } else {
                refreshChats();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private void deleteMessage(Message m) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ar tikrai norite ištrinti šią žinutę?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            try {
                messageService.delete(m.getId());
                loadMessages();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Klaida trinant: " + e.getMessage()).showAndWait();
            }
        }
    }

    private void editMessage(Message m) {
        TextInputDialog dialog = new TextInputDialog(m.getText());
        dialog.setTitle("Redaguoti žinutę");
        dialog.setHeaderText("Redaguojama žinutė (ID: " + m.getId() + ")");
        dialog.setContentText("Naujas tekstas:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newText -> {
            if (!newText.isBlank()) {
                try {
                    m.setText(newText);
                    messageService.update(m);
                    loadMessages(); // Atnaujiname sąrašą
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Klaida saugant: " + e.getMessage()).showAndWait();
                }
            }
        });
    }


    private void refreshChats() {
        User me = Session.getCurrentUser();
        List<Order> allOrders = orderService.findAll();

        List<Order> myChats;
        if (me.getRole() == User.Role.ADMIN) {
            myChats = allOrders;
        } else if (me.getRole() == User.Role.OWNER) {
            myChats = allOrders.stream()
                    .filter(o -> o.getRestaurant() != null && o.getRestaurant().getOwner().getId().equals(me.getId()))
                    .collect(Collectors.toList());
        } else {
            myChats = List.of();
        }

        String filter = searchField.getText().toLowerCase();
        if (!filter.isBlank()) {
            myChats = myChats.stream()
                    .filter(o -> String.valueOf(o.getId()).contains(filter) ||
                            (o.getClient() != null && o.getClient().getUsername().toLowerCase().contains(filter)))
                    .collect(Collectors.toList());
        }

        Order selected = chatList.getSelectionModel().getSelectedItem();
        chatsData.setAll(myChats);
        if (selected != null && myChats.contains(selected)) {
            chatList.getSelectionModel().select(selected);
        }
    }

    @FXML
    public void onSearch(){
        refreshChats();
    }

    private void loadMessages() {
        if (currentOrder == null) return;
        EntityManager em = JPAUtil.getEntityManager();
        try {

            List<Message> msgs = em.createQuery(
                            "select m from Message m " +
                                    "left join fetch m.sender " +
                                    "left join fetch m.recipient " +
                                    "where m.order.id = :oid order by m.createdAt",
                            Message.class)
                    .setParameter("oid", currentOrder.getId())
                    .getResultList();

            if (msgs.size() != messagesData.size() ||
                    (!msgs.isEmpty() && !msgs.get(msgs.size()-1).getId().equals(messagesData.get(messagesData.size()-1).getId()))) {
                messagesData.setAll(msgs);
                messageList.scrollTo(messagesData.size() - 1);
            } else if (!msgs.isEmpty() && !messagesData.isEmpty()) {
                for (int i=0; i<msgs.size(); i++) {
                    if (!msgs.get(i).getText().equals(messagesData.get(i).getText())) {
                        messagesData.setAll(msgs);
                        break;
                    }
                }
            }
        } finally {
            em.close();
        }
    }

    @FXML
    public void onSend() {
        if (currentOrder == null) return;

        if (Session.getCurrentUser().getRole() == User.Role.ADMIN) {
            new Alert(Alert.AlertType.WARNING, "Administratorius negali siųsti žinučių.").showAndWait();
            return;
        }

        String text = messageField.getText();
        if (text == null || text.isBlank()) return;

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Order ord = em.find(Order.class, currentOrder.getId());
            User me = em.find(User.class, Session.getCurrentUser().getId());

            Message m = new Message();
            m.setOrder(ord);
            m.setSender(me);
            m.setText(text);

            User recipient = null;
            if (me.getRole() == User.Role.OWNER) {
                recipient = ord.getClient();
            }

            m.setRecipient(recipient);

            em.persist(m);
            em.getTransaction().commit();

            messageField.clear();
            loadMessages();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Klaida siunčiant: " + ex.getMessage()).showAndWait();
        } finally {
            em.close();
        }
    }
}