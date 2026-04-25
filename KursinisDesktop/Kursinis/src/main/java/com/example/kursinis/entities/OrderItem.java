package com.example.kursinis.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    private int quantity = 1;

    public OrderItem() {}

    public OrderItem(Order order, MenuItem menuItem, int quantity) {
        this.order = order;
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        if (menuItem == null || menuItem.getCurrentPrice() == null) {
            return BigDecimal.ZERO;
        }
        return menuItem.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));
    }
}