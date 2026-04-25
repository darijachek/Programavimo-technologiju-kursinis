package com.example.kursinis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToOne
    private User client;

    @ManyToOne
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}
    public Order(User client, Restaurant restaurant) {
        this.client = client; this.restaurant = restaurant;
    }

}