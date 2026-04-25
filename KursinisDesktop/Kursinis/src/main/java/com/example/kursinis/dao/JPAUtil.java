package com.example.kursinis.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static EntityManagerFactory emf;
    private static boolean connected = false;

    public static synchronized void connect() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("Kursinis");
        }
        connected = true;
    }

    public static synchronized void disconnect() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
        connected = false;
    }

    public static boolean isConnected() {
        return connected && emf != null;
    }

    public static EntityManager getEntityManager() {
        if (emf == null) {
            connect();
        }
        return emf.createEntityManager();
    }

    public static void close() {
        disconnect();
    }
}
