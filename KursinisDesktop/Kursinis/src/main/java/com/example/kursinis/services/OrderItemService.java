package com.example.kursinis.services;

import com.example.kursinis.dao.GenericDAO;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.MenuItem;
import com.example.kursinis.entities.Order;
import com.example.kursinis.entities.OrderItem;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;

public class OrderItemService extends GenericDAO<OrderItem> {
    public OrderItemService() { super(OrderItem.class); }

    public List<OrderItem> findByOrder(Long orderId){
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select oi from OrderItem oi left join fetch oi.menuItem where oi.order.id=:id", OrderItem.class)
                    .setParameter("id", orderId)
                    .getResultList();
        } finally { em.close(); }
    }

    public void addItem(Long orderId, Long menuItemId, int qty){
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, orderId);
            MenuItem mi = em.find(MenuItem.class, menuItemId);
            if (order == null || mi == null) throw new IllegalArgumentException("Order arba MenuItem nerastas");

            OrderItem existing = em.createQuery(
                            "select oi from OrderItem oi where oi.order.id=:o and oi.menuItem.id=:m",
                            OrderItem.class)
                    .setParameter("o", orderId)
                    .setParameter("m", menuItemId)
                    .getResultStream().findFirst().orElse(null);

            if (existing != null){
                existing.setQuantity(existing.getQuantity() + qty);
            } else {
                OrderItem oi = new OrderItem(order, mi, qty);
                em.persist(oi);
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally { em.close(); }
    }

    public void updateQuantity(Long orderItemId, int qty){
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            OrderItem oi = em.find(OrderItem.class, orderItemId);
            if (oi == null) throw new IllegalArgumentException("Eilutė nerasta");
            oi.setQuantity(qty);
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally { em.close(); }
    }

    public void removeItem(Long orderItemId){
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            OrderItem oi = em.find(OrderItem.class, orderItemId);
            if (oi != null) em.remove(oi);
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally { em.close(); }
    }

    public BigDecimal lineTotal(OrderItem oi){
        if (oi.getMenuItem()==null || oi.getMenuItem().getCurrentPrice()==null) return BigDecimal.ZERO;
        return oi.getMenuItem().getCurrentPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
    }
}