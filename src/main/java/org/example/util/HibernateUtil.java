package org.example.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {

    private static EntityManagerFactory emfactory;
    static {
        emfactory = Persistence.createEntityManagerFactory("messageriePU");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emfactory.isOpen()) emfactory.close();
        }));
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emfactory;
    }

}
