package com.notamethod.fluppy.core.preferences;

import com.notamethod.fluppy.core.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class PreferencesRepository {
    @Getter
    EntityManager entityManager;

    public PreferencesRepository(EntityManagerFactory emf ) {
        this.entityManager=emf.createEntityManager();
    }

    public PreferencesRepository() {
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
        this.entityManager=emf.createEntityManager();
    }
}



