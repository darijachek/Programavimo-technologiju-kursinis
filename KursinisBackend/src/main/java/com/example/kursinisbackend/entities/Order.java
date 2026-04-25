package com.example.kursinisbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="orders")
public class Order {
    public enum Status { NEW, ACCEPTED, COOKING, READY, TAKEN, DELIVERING, DONE, CANCELED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private User client;

    @JsonIgnore
    @ManyToOne
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne
    private User driver;

    public Order() {}
    public Order(User client, Restaurant restaurant) {
        this.client = client; this.restaurant = restaurant;
    }

    public String getRestaurantName() {
        return restaurant != null ? restaurant.getName() : "";
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (OrderItem item : items) {
                total = total.add(item.getSubtotal());
            }
        }
        return total;
    }

    public Long getDriverId() {
        return driver != null ? driver.getId() : null;
    }

    public Long getUserId() {
        return client != null ? client.getId() : null;
    }

}
