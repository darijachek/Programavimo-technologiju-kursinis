package com.example.kursinisbackend.entities;

import com.example.kursinisbackend.services.PricingService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String category;
    private BigDecimal basePrice;

    @JsonIgnore
    @ManyToOne
    private Restaurant restaurant;

    public MenuItem() {}
    public MenuItem(String title, String category, BigDecimal price, Restaurant restaurant) {
        this.title = title; this.category = category; this.basePrice = price; this.restaurant = restaurant;
    }

    public BigDecimal getPrice() { return basePrice; }
    public void setPrice(BigDecimal price) { this.basePrice = price; }

}
