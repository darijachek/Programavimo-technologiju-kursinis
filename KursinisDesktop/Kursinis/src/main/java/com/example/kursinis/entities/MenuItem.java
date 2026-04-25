package com.example.kursinis.entities;

import com.example.kursinis.services.PricingService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "menu_item")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String category;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Transient
    public BigDecimal getCurrentPrice(){
        double m = PricingService.multiplierNow();
        return basePrice==null ? null : basePrice.multiply(BigDecimal.valueOf(m));
    }

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public MenuItem() {}
    public MenuItem(String title, String category, BigDecimal price, Restaurant restaurant) {
        this.title = title;
        this.category = category;
        this.basePrice = price;
        this.restaurant = restaurant;
    }

}