package com.example.kursinis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "author_id")
    @ManyToOne(optional = false)
    private User author;

    @JoinColumn(name = "restaurant_id")
    @ManyToOne
    private Restaurant restaurant;

    @JoinColumn(name = "driver_id")
    @ManyToOne
    private Driver driver;

    @JoinColumn(name = "client_id")
    @ManyToOne
    private Client client;

    @Column(name = "target_type")
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TargetType { RESTAURANT, DRIVER, CLIENT }

    public Review(){}

}
