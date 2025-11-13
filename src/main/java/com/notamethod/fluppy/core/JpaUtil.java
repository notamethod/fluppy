package com.notamethod.fluppy.core;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    private static final String PERSISTENCE_UNIT_NAME = "ebox2_pu";
    private static EntityManagerFactory emf;

    //EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
    private JpaUtil() {
        // Constructeur privé pour empêcher l'instanciation
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return emf;
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
