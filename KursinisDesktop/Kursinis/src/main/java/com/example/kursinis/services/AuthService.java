package com.example.kursinis.services;

import com.example.kursinis.dao.JPAUtil;
import com.example.kursinis.entities.User;

public class AuthService {
    public static String hash(String raw){ return org.springframework.security.crypto.bcrypt.BCrypt.hashpw(raw, org.springframework.security.crypto.bcrypt.BCrypt.gensalt()); }
    public static boolean check(String raw, String hash){ return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(raw, hash); }

    public User findByUsername(String username){
        var em = JPAUtil.getEntityManager();
        try { return em.createQuery("select u from User u where u.username=:u", User.class)
                .setParameter("u", username).getResultStream().findFirst().orElse(null);
        } finally { em.close(); }
    }}
