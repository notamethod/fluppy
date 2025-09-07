package com.notamethod.ebox.core;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;


@Slf4j
public class ApplicationDatabase {

    EntityManagerFactory emf;
    @Getter
    EntityManager entityManager;

    public ApplicationDatabase(EntityManagerFactory emf) {
        this.emf = emf;
        entityManager = emf.createEntityManager();
    }

    /**
     * Writes the application database to a file
     *
     */
    public void saveGame(GameEntity entity) {
        for (GenreEntity genreEntity:entity.getGenres()){
           genreEntity.getGames().add(entity);
        }
        entityManager.getTransaction().begin();
        if (entity.getId() == null|| entity.getId()==0) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }

        entityManager.getTransaction().commit();
    }

    /**
     * Writes the application database to a file
     *
     */
    public void deleteGame(GameEntity entity) {
        entityManager.getTransaction().begin();
        entityManager.remove(entity);
        entityManager.getTransaction().commit();
    }

    public GameEntity findGameById(long id) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery ("SELECT p FROM GameEntity p where p.id=:id");

        q.setParameter ("id", id);
        GameEntity game = (GameEntity) q.getSingleResult();
        entityManager.getTransaction().commit();
        return game;
    }

    public List<GameEntity> findGameByName(String name) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery ("SELECT p FROM GameEntity p where p.name=:name");
        q.setParameter ("name", name);
        List<GameEntity> results = q.getResultList ();

        entityManager.getTransaction().commit();
        return results;
    }

    public List<GameEntity> findGameByNameAndYear(String name, Integer year) {
        entityManager.getTransaction().begin();
        String queryString="SELECT p FROM GameEntity p where p.name=:name";
        if (year!=null) {
            queryString=queryString+ " and p.gameYear=:year";
        }
        Query q = entityManager.createQuery (queryString);

        q.setParameter ("name", name);
        if (year!=null) {
            q.setParameter("year", year);
        }
        List<GameEntity> results = q.getResultList ();

        entityManager.getTransaction().commit();
        return results;
    }

    public List<GameEntity> loadAllGames() {
        entityManager.getTransaction().begin();
        List<GameEntity> games = entityManager.createQuery("SELECT game FROM GameEntity game order by game.name", GameEntity.class).getResultList();
        entityManager.getTransaction().commit();
        return games;
    }
    public List<GenreEntity> loadAllGanres() {
        entityManager.getTransaction().begin();
        List<GenreEntity> genres = entityManager.createQuery("SELECT genre FROM GenreEntity genre order by genre.id", GenreEntity.class).getResultList();
        entityManager.getTransaction().commit();
        return genres;
    }


    public Optional<GenreEntity> getGenre(String id) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery ("SELECT p FROM GenreEntity p where p.id=:id");

        q.setParameter ("id", id);
        GenreEntity genre = (GenreEntity) q.getSingleResult();
        entityManager.getTransaction().commit();
        return Optional.ofNullable(genre);
    }
}
