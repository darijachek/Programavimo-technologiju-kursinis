package com.example.kursinis.services;

import com.example.kursinis.dao.GenericDAO;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.Client;
import com.example.kursinis.entities.Order;
import com.example.kursinis.entities.OrderItem;

import java.math.BigDecimal;

public class OrderService extends GenericDAO<Order> {
    public OrderService() { super(Order.class); }

    public BigDecimal calculateTotal(Order order){
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem it : order.getItems()){
            var mi = it.getMenuItem();
            if (mi!=null && mi.getCurrentPrice()!=null)
                total = total.add(mi.getCurrentPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
        }
        return total;
    }

    @Override
    public void create(Order order){
        super.create(order);
        if (order.getClient() instanceof Client c){
            int add = calculateTotal(order).intValue(); // 1€ -> 1 taškas
            var em = JPAUtil.getEntityManager();
            try{
                em.getTransaction().begin();
                var managed = em.find(Client.class, c.getId());
                managed.setLoyaltyPoints(managed.getLoyaltyPoints() + add);
                em.getTransaction().commit();
            } finally { em.close(); }
        }
    }
}
