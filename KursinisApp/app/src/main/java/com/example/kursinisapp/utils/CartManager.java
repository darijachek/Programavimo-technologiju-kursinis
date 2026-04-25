package com.example.kursinisapp.utils;

import com.example.kursinisapp.models.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> items = new ArrayList<>();
    private Long restaurantId;

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    public void addItem(MenuItem menuItem, int quantity, Long rId) {
        if (this.restaurantId != null && !this.restaurantId.equals(rId)) {
            items.clear();
        }
        this.restaurantId = rId;
        items.add(new CartItem(menuItem.id, menuItem.title, quantity, menuItem.basePrice));
    }

    public List<CartItem> getItems() { return items; }
    public Long getRestaurantId() { return restaurantId; }
    public void clear() { items.clear(); restaurantId = null; }

    public static class CartItem {
        public Long menuItemId;
        public String title;
        public int quantity;
        public double price;

        public CartItem(Long id, String title, int q, double p) {
            this.menuItemId = id; this.title = title; this.quantity = q; this.price = p;
        }
    }
}
