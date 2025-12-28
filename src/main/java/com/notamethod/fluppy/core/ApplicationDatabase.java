package com.notamethod.fluppy.core;

import com.notamethod.fluppy.core.game.GameEntity;
import com.notamethod.fluppy.core.game.GenreEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


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

    public List<GameEntity> findGameByGenre(String genreId) {
        return findGameByGenre(genreId, -1);
    }
    public List<GameEntity> findGameByGenre(String genreId, int maxResult) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery ("""
                SELECT game FROM GameEntity game JOIN game.genres genre
                WHERE genre.id = :genreId
                """);
        q.setParameter ("genreId", genreId);
        if (maxResult>0)
            q.setMaxResults(maxResult);
        List<GameEntity> results = q.getResultList ();
        entityManager.getTransaction().commit();
        return results;
    }
    public List<GameEntity> findGameByYear(Integer year, boolean nsfw, int maxResult) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery ("""
                SELECT game FROM GameEntity game WHERE game.gameYear=:year
                AND (:nsfw is true OR game.ageRating < 1)
                """);
        q.setParameter ("year", year);
        q.setParameter ("nsfw", nsfw);
        if (maxResult>0)
            q.setMaxResults(maxResult);
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
    public List<GenreEntity> loadAllGenres() {
        entityManager.getTransaction().begin();
        List<GenreEntity> genres = entityManager.createQuery("SELECT genre FROM GenreEntity genre order by genre.id", GenreEntity.class).getResultList();
        entityManager.getTransaction().commit();
        return genres;
    }

    public  Map<String, Long> getTopGenres(int limit) {
        entityManager.getTransaction().begin();
        int count=0;
        List<Object[]> games = entityManager.createQuery("SELECT g.id, COUNT(gm) FROM GenreEntity g JOIN g.games gm GROUP BY g.id ORDER BY COUNT(gm) DESC").getResultList();
        entityManager.getTransaction().commit();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] o : games){
            map.put((String) o[0], (Long) o[1]);
            count++;
            if (count>=limit)
                return map;
        }
        return map;
    }

    public  Map<Integer, Long> getTopYears(int limit) {
        entityManager.getTransaction().begin();
        int count=0;
        List<Object[]> games = entityManager.createQuery("SELECT game.gameYear, COUNT(game) FROM GameEntity game where game.gameYear>1970 GROUP BY game.gameYear ORDER BY game.gameYear").getResultList();
        entityManager.getTransaction().commit();
        Map<Integer, Long> map = new LinkedHashMap<>();
        for (Object[] o : games){
            map.put((Integer) o[0], (Long) o[1]);
            count++;
            if (count>=limit)
                return map;
        }
        return map;
    }

    public Optional<GenreEntity> getGenre(String id) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery ("SELECT p FROM GenreEntity p where p.id=:id",GenreEntity.class);
        q.setParameter ("id", id);
        Optional<GenreEntity> genre = q.getResultStream().findFirst();
        entityManager.getTransaction().commit();
        return genre;
    }

    public List<GameEntity> loadAllGamesButNot(Set<Long> gameIds, boolean nsfw) {
        entityManager.getTransaction().begin();

        Query q = entityManager.createQuery("SELECT game FROM GameEntity game where game.id not in :gameIds   AND (:nsfw is true OR game.ageRating < 1)  order by game.name", GameEntity.class);
        q.setParameter ("gameIds", gameIds);
        q.setParameter ("nsfw", nsfw);
        entityManager.getTransaction().commit();
        return q.getResultList ();
    }

    public List<GameEntity> runGameQuery(String query, int maxResult) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery(query, GameEntity.class);

        entityManager.getTransaction().commit();
        q.setMaxResults(maxResult);
        return q.getResultList ();
    }

    public List<GameEntity> runGameQuery(String query, boolean nsfw, int maxResult) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery(query, GameEntity.class);
        q.setParameter ("nsfw", nsfw);
        entityManager.getTransaction().commit();
        q.setMaxResults(maxResult);
        return q.getResultList ();
    }

    public void close(){
        entityManager.close();
        emf.close();

    }

    public List<GameEntity> runGameQuery(String query, boolean nsfw, String paramFilter) {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery(query, GameEntity.class);
        q.setParameter ("nsfw", nsfw);
        q.setParameter ("paramFilter", paramFilter);
        entityManager.getTransaction().commit();

        return q.getResultList ();
    }


}
