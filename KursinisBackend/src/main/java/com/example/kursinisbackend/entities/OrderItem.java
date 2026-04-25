package com.example.kursinisbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private Order order;

    @JsonIgnore
    @ManyToOne
    private MenuItem menuItem;

    private int quantity = 1;

    public OrderItem() {}
    public OrderItem(Order order, MenuItem menuItem, int quantity) {
        this.order = order; this.menuItem = menuItem; this.quantity = quantity;
    }

    public java.math.BigDecimal getSubtotal() {
        if (menuItem == null || menuItem.getBasePrice() == null) {
            return java.math.BigDecimal.ZERO;
        }

        double multiplier = com.example.kursinisbackend.services.PricingService.multiplierNow();

        return menuItem.getBasePrice()
                .multiply(java.math.BigDecimal.valueOf(quantity))
                .multiply(java.math.BigDecimal.valueOf(multiplier));
    }

}