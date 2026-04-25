package com.example.kursinis.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GenericDAO<T> {
    private final Class<T> clazz;

    public GenericDAO(Class<T> clazz) {
        this.clazz = clazz;
    }

    public List<T> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<T> q = em.createQuery("from " + clazz.getSimpleName(), clazz);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public T find(Long id){
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(clazz, id);
        } finally {
            em.close();
        }
    }


    public void create(T entity) {
        executeInTx(em -> em.persist(entity));
    }

    public void update(T entity) {
        executeInTx(em -> em.merge(entity));
    }


    public boolean delete(Long id) {
        return supplyInTx(em -> {
            T managed = em.find(clazz, id);
            if (managed == null) return false;
            em.remove(managed);
            return true;
        });
    }

    private void executeInTx(Consumer<EntityManager> work) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            work.accept(em);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    private <R> R supplyInTx(Function<EntityManager, R> work) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            R result = work.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
