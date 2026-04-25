package com.example.kursinisbackend.controllers;

import com.example.kursinisbackend.entities.*;
import com.example.kursinisbackend.repos.*;
import com.example.kursinisbackend.services.PricingService;
import com.example.kursinisbackend.util.HashUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.Timestamp;

@RestController
public class ApiController {

    @Autowired private UserRepository userRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private MessageRepository messageRepo;
    @Autowired private RestaurantRepository restaurantRepo;
    @Autowired private MenuItemRepository menuItemRepo;
    @Autowired private ReviewRepository reviewRepo;

    // Loginas, hashinimas
    @PostMapping("/login")
    public String login(@RequestBody String body) {
        Gson gson = new Gson();
        Properties props = gson.fromJson(body, Properties.class);
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        User user = userRepo.findByUsername(username);

        if (user != null && HashUtil.checkPassword(password, user.getPasswordHash())) {
            return gson.toJson(user);
        }
        return "Error";
    }

    // Registracija, hashinimas
    @PostMapping("/register")
    public String register(@RequestBody String body) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);

        try {
            String roleStr = json.get("role").getAsString();
            String username = json.get("username").getAsString();
            String rawPassword = json.get("password").getAsString();

            String passwordHash = HashUtil.hashPassword(rawPassword);

            if (roleStr.equalsIgnoreCase("CLIENT")) {
                Client client = new Client();
                client.setUsername(username);
                client.setPasswordHash(passwordHash);
                client.setRole(User.Role.CLIENT);
                if (json.has("address")) client.setAddress(json.get("address").getAsString());
                userRepo.save(client);
            } else if (roleStr.equalsIgnoreCase("DRIVER")) {
                Driver driver = new Driver();
                driver.setUsername(username);
                driver.setPasswordHash(passwordHash);
                driver.setRole(User.Role.DRIVER);
                if (json.has("vehicle")) driver.setVehicle(json.get("vehicle").getAsString());
                userRepo.save(driver);
            } else {
                return "Error: Unknown Role";
            }
            return "Success";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    @GetMapping("/restaurants")
    public List<Restaurant> getRestaurants() {
        return restaurantRepo.findAll();
    }

    @GetMapping("/restaurant/{id}/menu")
    public List<MenuItem> getMenu(@PathVariable Long id) {
        Restaurant r = restaurantRepo.findById(id).orElse(null);
        if (r == null) return List.of();

        List<MenuItem> menu = r.getMenu();
        double multiplier = PricingService.multiplierNow();

        for (MenuItem item : menu) {
            if (item.getBasePrice() != null) {
                java.math.BigDecimal newPrice = item.getBasePrice()
                        .multiply(java.math.BigDecimal.valueOf(multiplier));
                item.setBasePrice(newPrice);
            }
        }
        return menu;
    }

    @PostMapping("/createOrder")
    public String createOrder(@RequestBody String body) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);
        try {
            Long userId = json.get("userId").getAsLong();
            Long restaurantId = json.get("restaurantId").getAsLong();
            User client = userRepo.findById(userId).orElseThrow();
            Restaurant rest = restaurantRepo.findById(restaurantId).orElseThrow();

            Order order = new Order(client, rest);
            order.setStatus(Order.Status.NEW);

            JsonArray itemsArray = json.getAsJsonArray("items");
            List<OrderItem> orderItems = new ArrayList<>();
            for (JsonElement itemElem : itemsArray) {
                JsonObject itemObj = itemElem.getAsJsonObject();
                MenuItem menuItem = menuItemRepo.findById(itemObj.get("menuItemId").getAsLong()).orElse(null);
                if (menuItem != null) {
                    orderItems.add(new OrderItem(order, menuItem, itemObj.get("quantity").getAsInt()));
                }
            }
            order.setItems(orderItems);
            orderRepo.save(order);
            return "Order Created";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/orders/available")
    public List<Order> getAvailableOrders() {
        return orderRepo.findByStatus(Order.Status.READY);
    }

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderRepo.findById(id).orElse(null);
    }

    @PostMapping("/orders/take")
    public String takeOrder(@RequestBody String body) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);
        try {
            Order order = orderRepo.findById(json.get("orderId").getAsLong()).orElseThrow();
            if (order.getStatus() == Order.Status.READY) {
                order.setStatus(Order.Status.TAKEN);
                order.setDriver(userRepo.findById(json.get("driverId").getAsLong()).orElseThrow());
                orderRepo.save(order);
                return "Taken";
            }
            return "Error: Order not ready";
        } catch (Exception e) {
            return "Error";
        }
    }

    @PostMapping("/orders/pickup")
    public String pickupOrder(@RequestBody String body) {
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        try {
            Order order = orderRepo.findById(json.get("orderId").getAsLong()).orElseThrow();
            if (order.getStatus() == Order.Status.TAKEN) {
                order.setStatus(Order.Status.DELIVERING);
                orderRepo.save(order);
                return "PickedUp";
            }
            return "Error: Order not in TAKEN status";
        } catch (Exception e) {
            return "Error";
        }
    }

    @PostMapping("/orders/complete")
    public String completeOrder(@RequestBody String body) {
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        try {
            Order order = orderRepo.findById(json.get("orderId").getAsLong()).orElseThrow();
            if (order.getStatus() == Order.Status.DELIVERING) {
                order.setStatus(Order.Status.DONE);

                User user = order.getClient();
                if (user instanceof Client client) {
                    int pointsEarned = order.getTotalPrice().intValue();
                    client.setLoyaltyPoints(client.getLoyaltyPoints() + pointsEarned);
                    userRepo.save(client);
                }

                orderRepo.save(order);
                return "Completed and points added";
            }
            return "Error: Order must be in DELIVERING status";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    @PostMapping("/orders/{id}/complete")
    public String completeOrderById(@PathVariable Long id) {
        try {
            Order order = orderRepo.findById(id).orElseThrow();

            if (order.getStatus() == Order.Status.DELIVERING || order.getStatus() == Order.Status.TAKEN) {
                order.setStatus(Order.Status.DONE);

                User user = order.getClient();
                if (user instanceof Client client) {
                    int pointsEarned = order.getTotalPrice().intValue();
                    client.setLoyaltyPoints(client.getLoyaltyPoints() + pointsEarned);
                    userRepo.save(client);
                }

                orderRepo.save(order);
                return "Success: Order completed";
            }
            return "Error: Order not in DELIVERING status";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/orders/client/{id}")
    public List<Order> getClientOrders(@PathVariable Long id) {
        return orderRepo.findByClient_Id(id);
    }

    @GetMapping("/orders/driver/{id}")
    public List<Order> getDriverOrders(@PathVariable Long id) {
        return orderRepo.findByDriver_Id(id);
    }

    @PutMapping("/client/{id}/update-address")
    public String updateAddress(@PathVariable Long id, @RequestBody String body) {
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        try {
            User user = userRepo.findById(id).orElseThrow();
            if (user instanceof Client client) {
                client.setAddress(json.get("address").getAsString());
                userRepo.save(client);
                return "Address updated";
            }
            return "Error";
        } catch (Exception e) {
            return "Error";
        }
    }

    @PutMapping("/driver/{id}/update-vehicle")
    public String updateVehicle(@PathVariable Long id, @RequestBody String body) {
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        try {
            User user = userRepo.findById(id).orElseThrow();
            if (user instanceof Driver driver) {
                driver.setVehicle(json.get("vehicle").getAsString());
                userRepo.save(driver);
                return "Vehicle updated";
            }
            return "Error";
        } catch (Exception e) {
            return "Error";
        }
    }

    @GetMapping("/chat/{orderId}")
    public List<Message> getChatMessages(@PathVariable Long orderId) {
        return messageRepo.findByOrderId(orderId);
    }

    @PostMapping("/messages/send")
    public String sendMessage(@RequestBody String body) {
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        try {
            Long orderId = json.get("orderId").getAsLong();
            Order order = orderRepo.findById(orderId).orElseThrow();

            if (order.getStatus() == Order.Status.DONE || order.getStatus() == Order.Status.CANCELED) {
                return "Error: Chat is closed.";
            }

            Message msg = new Message();
            msg.setOrder(order);
            msg.setText(json.get("text").getAsString());
            msg.setCreatedAt(java.time.LocalDateTime.now());

            Long senderId = json.get("senderId").getAsLong();
            User sender = userRepo.findById(senderId).orElseThrow();
            msg.setSender(sender);

            User recipient = null;
            if (sender.getRole() == User.Role.CLIENT) {
                if (order.getDriver() != null) recipient = order.getDriver();
                else if (order.getRestaurant() != null) recipient = order.getRestaurant().getOwner();
            }
            else if (sender.getRole() == User.Role.DRIVER) {
                recipient = order.getClient();
            }
            else if (sender.getRole() == User.Role.OWNER || sender.getRole() == User.Role.ADMIN) {
                if (order.getDriver() != null) recipient = order.getDriver();
                else recipient = order.getClient();
            }

            msg.setRecipient(recipient);

            messageRepo.save(msg);
            return "Sent";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/review/add")
    public String addReview(@RequestBody String body) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);
        try {
            Review review = new Review();
            review.setAuthor(userRepo.findById(json.get("authorId").getAsLong()).orElseThrow());
            review.setRating(json.get("rating").getAsInt());
            review.setComment(json.get("comment").getAsString());
            review.setCreatedAt(LocalDateTime.now());
            review.setTargetType(Review.TargetType.valueOf(json.get("targetType").getAsString().toUpperCase()));

            if (json.has("restaurantId")) review.setRestaurant(restaurantRepo.findById(json.get("restaurantId").getAsLong()).orElse(null));
            else if (json.has("driverId")) review.setDriver((Driver) userRepo.findById(json.get("driverId").getAsLong()).orElse(null));
            else if (json.has("clientId")) review.setClient((Client) userRepo.findById(json.get("clientId").getAsLong()).orElse(null));

            reviewRepo.save(review);
            return "Review Added";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/reviews/restaurant/{id}")
    public List<Review> getRestaurantReviews(@PathVariable int id) {
        return reviewRepo.findByRestaurantId((long) id);
    }

    @GetMapping("/users/search")
    public String searchUsers(@RequestParam(required = false) String username,
                              @RequestParam(required = false) User.Role role) {
        List<User> results;

        if (username != null && !username.isEmpty()) {
            results = userRepo.findByUsernameContainingIgnoreCase(username);
        } else if (role != null) {
            results = userRepo.findByRole(role);
        } else {
            results = userRepo.findAll();
        }

        return new Gson().toJson(results);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepo.findById(id).orElse(null);
    }
}