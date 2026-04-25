package com.example.kursinis.services;

import com.example.kursinis.dao.GenericDAO;
import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.Driver;
import com.example.kursinis.entities.Review;
import jakarta.persistence.EntityManager;
import java.util.List;

public class ReviewService extends GenericDAO<Review> {
    public ReviewService() {
        super(Review.class);
    }

    @Override
    public List<Review> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Review r " +
                            "LEFT JOIN FETCH r.author " +
                            "LEFT JOIN FETCH r.restaurant " +
                            "LEFT JOIN FETCH r.driver " +
                            "LEFT JOIN FETCH r.client",
                    Review.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void updateDriverRating(Long driverId){
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Driver d = em.find(Driver.class, driverId);
            if (d != null) {
                Double avg = em.createQuery(
                                "select avg(r.rating) from Review r where r.driver.id=:id", Double.class)
                        .setParameter("id", driverId)
                        .getSingleResult();
                d.setRating(avg == null ? null : Math.round(avg * 10.0) / 10.0);
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public void create(Review r){
        super.create(r);
        if (r.getDriver() != null) updateDriverRating(r.getDriver().getId());
    }

    @Override
    public void update(Review r){
        super.update(r);
        if (r.getDriver() != null) updateDriverRating(r.getDriver().getId());
    }

    @Override
    public boolean delete(Long id) {
        Long driverId = null;
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Review existing = em.find(Review.class, id);
            if (existing != null && existing.getDriver() != null) {
                driverId = existing.getDriver().getId();
            }
        } finally {
            em.close();
        }
        boolean ok = super.delete(id);
        if (ok && driverId != null) {
            updateDriverRating(driverId);
        }
        return ok;
    }
}