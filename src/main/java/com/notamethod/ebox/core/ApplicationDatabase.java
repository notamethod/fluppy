package com.notamethod.ebox.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class ApplicationDatabase {

    EntityManagerFactory emf;
    EntityManager em;

    public ApplicationDatabase(EntityManagerFactory emf) {
        this.emf = emf;
        em = emf.createEntityManager();
    }




    /**
     * Writes the application database to a file
     *
     */
    public void save(GameEntity entity) {
        em.getTransaction().begin();
        if (entity.getId() == null|| entity.getId()==0) {
            em.persist(entity);
        } else {
            em.merge(entity);
        }

        em.getTransaction().commit();
    }

    /**
     * Writes the application database to a file
     *
     */
    public void delete(GameEntity entity) {
        em.getTransaction().begin();
        em.remove(entity);
        em.getTransaction().commit();
    }

    public GameEntity findById(long id) {
        em.getTransaction().begin();
        Query q = em.createQuery ("SELECT p FROM GameEntity p where p.id=:id");

        q.setParameter ("id", id);
        GameEntity game = (GameEntity) q.getSingleResult();
        em.getTransaction().commit();
        return game;
    }

    public List<GameEntity> findByName(String name) {
        em.getTransaction().begin();
        Query q = em.createQuery ("SELECT p FROM GameEntity p where p.name=:name");
        q.setParameter ("name", name);
        List<GameEntity> results = q.getResultList ();

        em.getTransaction().commit();
        return results;
    }

    public List<GameEntity> findByNameAndYear(String name, Integer year) {
        em.getTransaction().begin();
        String queryString="SELECT p FROM GameEntity p where p.name=:name";
        if (year!=null) {
            queryString=queryString+ " and p.gameYear=:year";
        }
        Query q = em.createQuery (queryString);

        q.setParameter ("name", name);
        if (year!=null) {
            q.setParameter("year", year);
        }
        List<GameEntity> results = q.getResultList ();

        em.getTransaction().commit();
        return results;
    }

    public List<GameEntity> loadAll() {
        em.getTransaction().begin();
        List<GameEntity> games = em.createQuery("SELECT game FROM GameEntity game order by game.name", GameEntity.class).getResultList();
        em.getTransaction().commit();
        return games;
    }

}
