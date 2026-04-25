package com.example.kursinis.services;

import com.example.kursinis.dao.GenericDAO;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.Restaurant;
import com.example.kursinis.entities.MenuItem;
import com.example.kursinis.entities.User;
import jakarta.persistence.EntityManager;

import java.awt.*;

public class MenuItemService extends GenericDAO<MenuItem> {
    public MenuItemService(){ super(MenuItem.class); }

    private Restaurant findRestaurant(Long id){
        if (id == null) return null;
        EntityManager em = JPAUtil.getEntityManager();
        try { return em.find(Restaurant.class, id); }
        finally { em.close(); }
    }

    public java.util.List<MenuItem> findByRestaurant(Long restaurantId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT m FROM MenuItem m WHERE m.restaurant.id = :id", MenuItem.class)
                    .setParameter("id", restaurantId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void createAs(User me, MenuItem m, Long restaurantId){
        if (me.getRole() == User.Role.CLIENT || me.getRole() == User.Role.DRIVER)
            throw new SecurityException("Neturite teisės kurti meniu.");

        if (me.getRole() == User.Role.OWNER) {
            Restaurant r = (restaurantId != null) ? findRestaurant(restaurantId) : null;
            if (r == null || r.getOwner() == null || !r.getOwner().getId().equals(me.getId()))
                throw new SecurityException("Savininkas gali kurti tik savo restorano meniu.");
            m.setRestaurant(r);
        } else if (me.getRole() == User.Role.ADMIN) {
            m.setRestaurant(findRestaurant(restaurantId));
        }
        super.create(m);
    }

    public void updateAs(User me, MenuItem m, Long restaurantId){
        if (me.getRole() == User.Role.CLIENT || me.getRole() == User.Role.DRIVER)
            throw new SecurityException("Neturite teisės redaguoti meniu.");

        if (me.getRole() == User.Role.OWNER) {
            if (m.getRestaurant() == null || m.getRestaurant().getOwner() == null ||
                    !m.getRestaurant().getOwner().getId().equals(me.getId()))
                throw new SecurityException("Negalite redaguoti kito savininko meniu.");
        } else if (me.getRole() == User.Role.ADMIN) {
            if (restaurantId != null) m.setRestaurant(findRestaurant(restaurantId));
        }
        super.update(m);
    }

    private MenuItem findById(Long id){
        var em = JPAUtil.getEntityManager();
        try { return em.find(MenuItem.class, id); }
        finally { em.close(); }
    }

    public void deleteAs(User me, Long id){
        MenuItem m = findById(id);
        if (m == null) return;

        if (me.getRole() == User.Role.OWNER) {
            if (m.getRestaurant()==null || m.getRestaurant().getOwner()==null ||
                    !m.getRestaurant().getOwner().getId().equals(me.getId()))
                throw new SecurityException("Negalite šalinti kito savininko meniu.");
        } else if (me.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Neturite teisės šalinti meniu.");
        }

        super.delete(id);
    }
}
