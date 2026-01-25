package com.notamethod.fluppy.core;


import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
                    .applySetting("hibernate.connection.url", "jdbc:h2:~/.fluppy/data/ebdb2")
                    .applySetting("hibernate.connection.username", "sa")
                    .applySetting("hibernate.connection.password", "")
                    .applySetting("hibernate.hbm2ddl.auto", "update")
                    .applySetting("hibernate.show_sql", "false")
                    .applySetting("hibernate.connection.pool_size", "1")
                    .build();

            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(com.notamethod.fluppy.core.game.GameEntity.class)
                    .addAnnotatedClass(com.notamethod.fluppy.core.game.GenreEntity.class)
                    .buildMetadata()
                    .buildSessionFactory();
        }
        return sessionFactory;
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}