package com.example.kursinisbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private User author;

    @JsonIgnore
    @ManyToOne
    private Restaurant restaurant;

    @JsonIgnore
    @ManyToOne
    private Driver driver;

    @JsonIgnore
    @ManyToOne
    private Client client;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();


    public enum TargetType { RESTAURANT, DRIVER, CLIENT }

    public Review(){}

}