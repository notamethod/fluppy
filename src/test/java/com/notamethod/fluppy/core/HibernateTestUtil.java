package com.notamethod.fluppy.core;

import com.notamethod.fluppy.core.game.GameEntity;
import com.notamethod.fluppy.core.game.GenreEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateTestUtil {

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            StandardServiceRegistry registry =
                    new StandardServiceRegistryBuilder()
                            .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
                            .applySetting("hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
                            .applySetting("hibernate.connection.username", "sa")
                            .applySetting("hibernate.connection.password", "")
                            .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                            .applySetting("hibernate.show_sql", "false")
                            .applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                            .build();

            MetadataSources sources = new MetadataSources(registry)
                    .addAnnotatedClass(GameEntity.class)
                    .addAnnotatedClass(GenreEntity.class);

            sessionFactory = sources.buildMetadata().buildSessionFactory();
        }
        return sessionFactory;
    }
}
