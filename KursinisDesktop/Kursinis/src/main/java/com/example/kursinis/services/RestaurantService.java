package com.example.kursinis.services;

import com.example.kursinis.dao.GenericDAO;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.Owner;
import com.example.kursinis.entities.Restaurant;
import com.example.kursinis.entities.User;

public class RestaurantService extends GenericDAO<Restaurant> {
    public RestaurantService(){ super(Restaurant.class); }

    public void createAs(User me, Restaurant r){
        if (me.getRole() == User.Role.OWNER) {
            r.setOwner((Owner) me);
        }
        if (me.getRole() != User.Role.ADMIN && me.getRole() != User.Role.OWNER)
            throw new SecurityException("Neturite teisės kurti restoranų.");
        super.create(r);
    }

    public void updateAs(User me, Restaurant r){
        if (me.getRole() == User.Role.OWNER) {
            if (r.getOwner() == null || !r.getOwner().getId().equals(me.getId()))
                throw new SecurityException("Negalite redaguoti kito savininko restorano.");
        } else if (me.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Neturite teisės redaguoti restoranų.");
        }
        super.update(r);
    }

    public void deleteAs(User me, Long id){
        Restaurant r = findById(id);
        if (r == null) return;
        if (me.getRole() == User.Role.OWNER) {
            if (r.getOwner() == null || !r.getOwner().getId().equals(me.getId()))
                throw new SecurityException("Negalite šalinti kito savininko restorano.");
        } else if (me.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Neturite teisės šalinti restoranų.");
        }
        super.delete(id);
    }

    public Restaurant findById(Long id){
        var em = JPAUtil.getEntityManager();
        try { return em.find(Restaurant.class, id); }
        finally { em.close(); }
    }
}