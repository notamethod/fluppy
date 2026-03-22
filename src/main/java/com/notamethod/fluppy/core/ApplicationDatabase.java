package com.notamethod.fluppy.core;

import com.notamethod.fluppy.core.game.*;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.*;


@Slf4j
public class ApplicationDatabase {

    SessionFactory sessionFactory;


    public ApplicationDatabase() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    public ApplicationDatabase(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Writes the application database to a file
     *
     */
    public void saveGame(GameEntity entity) {

        sessionFactory.inTransaction(session -> {

            if (entity.getId() == null || entity.getId() == 0) {
                session.persist(entity);
            } else {
                session.merge(entity);
            }

        });

    }

    public void saveGame(GameApp gameApp) {
        GameEntity gameEntity = GameMapper.INSTANCE.toEntity(gameApp);

        sessionFactory.inTransaction(session -> {
            Company company = gameApp.getPublisher();
            if (company != null) {
                Optional<CompanyEntity> compEnt = findCompanyById(session, company.getId());
                if (compEnt.isPresent()) {
                    gameEntity.setPublisher(compEnt.get());

                } else {
                    CompanyEntity compEnt2 = new CompanyEntity(company.getId(), company.getName(), company.getImage());
                    //GameMapper.INSTANCE.toEntity(company);
                    session.persist(compEnt2);
                    gameEntity.setPublisher(compEnt2);
                }
            }
            for (GenreApp genre : gameApp.getGenres()) {
                Optional<GenreEntity> gent = findGenreByID(session, genre.getId());
                if (gent.isPresent()) {
                    gameEntity.getGenres().add(gent.get());

                } else {
                    GenreEntity getn2 = GameMapper.INSTANCE.toEntity(genre);
                    session.persist(getn2);
                    gameEntity.getGenres().add(getn2);
                }
            }
            for (GenreEntity genreEntity : gameEntity.getGenres()) {
                genreEntity.getGames().add(gameEntity);
            }
            if (gameEntity.getId() == null || gameEntity.getId() == 0) {
                session.persist(gameEntity);
            } else {
                session.merge(gameEntity);
            }

        });

    }

    public void saveGenre(GenreEntity entity) {

        sessionFactory.inTransaction(session -> {

            session.persist(entity);


        });

    }

    /**
     * Writes the application database to a file
     *
     */
    public void deleteGame(GameEntity entity) {
        sessionFactory.inTransaction(session -> {
            session.remove(entity);
        });
    }

    public GameEntity findGameById(long id) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery("SELECT p FROM GameEntity p where p.id=:id", GameEntity.class);

            q.setParameter("id", id);

            return q.getSingleResult();
        });

    }

    public GameEntity findFullGameById(long id) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery("SELECT game FROM GameEntity game left join fetch game.genres  left join fetch game.publisher where game.id=:id", GameEntity.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        });

    }

    public List<GameEntity> findGameByName(String name) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery("""
                    SELECT p FROM GameEntity p LEFT JOIN FETCH p.genres where p.name=:name
                    """, GameEntity.class);
            q.setParameter("name", name);

            return q.getResultList();
        });
    }

    public List<GameEntity> findGameByGenre(String genreId) {
        return findGameByGenre(genreId, -1, null);
    }

    public List<GameEntity> findGameByGenre(String genreId, int maxResult, Set<Long> gameIds) {

        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q;

            if (gameIds == null || gameIds.isEmpty()) {
                q = session.createQuery("""
                                 SELECT game FROM GameEntity game JOIN FETCH game.genres genre
                             WHERE genre.id = :genreId
                        """, GameEntity.class);
            } else {
                q = session.createQuery("""
                                 SELECT game FROM GameEntity game JOIN FETCH game.genres genre
                             WHERE genre.id = :genreId
                         AND game.id not in :ids
                        """, GameEntity.class);
                q.setParameterList("ids", gameIds);
            }
            q.setParameter("genreId", genreId);
            if (maxResult > 0)
                q.setMaxResults(maxResult);
            return q.getResultList();
        });
    }

    public List<GameEntity> findGameByYear(Integer year, boolean nsfw, int maxResult) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery("""
                    SELECT game FROM GameEntity game 
                    left join fetch game.genres
                    WHERE game.gameYear=:year
                    AND (:nsfw is true OR game.ageRating < 1)
                    """, GameEntity.class);
            q.setParameter("year", year);
            q.setParameter("nsfw", nsfw);
            if (maxResult > 0)
                q.setMaxResults(maxResult);

            return q.getResultList();
        });
    }

    public List<GameEntity> findGameByPublisher(String publisher, boolean nsfw, int maxResult) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery("""
                    SELECT game FROM GameEntity game 
                    left join fetch game.genres
                    WHERE game.publisher.id=:publisher
                    AND (:nsfw is true OR game.ageRating < 1)
                    """, GameEntity.class);
            q.setParameter("publisher", publisher);
            q.setParameter("nsfw", nsfw);
            if (maxResult > 0)
                q.setMaxResults(maxResult);

            return q.getResultList();
        });
    }

    public List<GameEntity> findGameByNameAndYear(String name, Integer year) {
        return sessionFactory.fromSession(session -> {
            String queryString = "SELECT p FROM GameEntity p where p.name=:name";
            if (year != null) {
                queryString = queryString + " and p.gameYear=:year";
            }
            Query<GameEntity> q = session.createQuery(queryString, GameEntity.class);

            q.setParameter("name", name);
            if (year != null) {
                q.setParameter("year", year);
            }
            return q.getResultList();
        });
    }

    public List<GameEntity> loadAllGames() {
        return sessionFactory.fromSession(session -> {
            List<GameEntity> games = session.createQuery("SELECT game FROM GameEntity game order by game.name", GameEntity.class).getResultList();

            return games;
        });

    }

    public List<GenreEntity> loadAllGenres() {
        return sessionFactory.fromSession(session -> {
            List<GenreEntity> genres = session.createQuery("SELECT genre FROM GenreEntity genre order by genre.id", GenreEntity.class).getResultList();
            return genres;
        });
    }

    public Map<String, Long> getTopGenres(int limit) {
        return sessionFactory.fromSession(session -> {
            int count = 0;
            List<Object[]> games = session.createQuery("SELECT g.id, COUNT(gm) FROM GenreEntity g JOIN g.games gm GROUP BY g.id ORDER BY COUNT(gm) DESC", Object[].class).getResultList();

            Map<String, Long> map = new LinkedHashMap<>();
            for (Object[] o : games) {
                map.put((String) o[0], (Long) o[1]);
                if (++count >= limit) {
                    break;
                }
            }
            return map;
        });
    }

    public Map<Integer, Long> getTopYears(int limit) {
        return sessionFactory.fromSession(session -> {
            int count = 0;
            List<Object[]> games = session.createQuery("""
                    SELECT game.gameYear, COUNT(game)
                    FROM GameEntity game where game.gameYear>1970 GROUP BY game.gameYear ORDER BY game.gameYear
                    """, Object[].class).getResultList();

            Map<Integer, Long> map = new LinkedHashMap<>();
            for (Object[] o : games) {
                map.put((Integer) o[0], (Long) o[1]);
                count++;
                if (++count >= limit) {
                    break;
                }
            }
            return map;
        });
    }

    public Map<Company, Long> getTopCompanies(int limit) {
        return sessionFactory.fromSession(session -> {
            int count = 0;
            List<Object[]> games = session.createQuery("""
                    SELECT game.publisher, COUNT(game)
                    FROM GameEntity game GROUP BY game.publisher ORDER BY game.publisher
                    """, Object[].class).getResultList();

            Map<Company, Long> map = new LinkedHashMap<>();
            for (Object[] o : games) {
                CompanyEntity companyEntity = (CompanyEntity) o[0];
                map.put(GameMapper.INSTANCE.toCompany(companyEntity), (Long) o[1]);
                count++;
                if (++count >= limit) {
                    break;
                }
            }
            log.debug("{} companies found", count);
            return map;
        });
    }

    public Optional<GenreEntity> findGenreByID(String id) {
        return sessionFactory.fromSession(session -> {
            Query<GenreEntity> q = session.createQuery("SELECT p FROM GenreEntity p where p.id=:id", GenreEntity.class);
            q.setParameter("id", id);

            return q.uniqueResultOptional();
        });
    }

    public Optional<GenreEntity> findGenreByID(Session session, String id) {
        Query<GenreEntity> q = session.createQuery("SELECT p FROM GenreEntity p where p.id=:id", GenreEntity.class);
        q.setParameter("id", id);
        return q.uniqueResultOptional();
    }

    public Optional<CompanyEntity> findCompanyById(Session session, String id) {
        Query<CompanyEntity> q = session.createQuery("SELECT p FROM CompanyEntity p where p.id=:id", CompanyEntity.class);
        q.setParameter("id", id);
        return q.uniqueResultOptional();
    }

    public List<GameEntity> loadAllGamesButNot(Set<Long> gameIds, boolean nsfw) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery("SELECT game FROM GameEntity game left join fetch game.genres where game.id not in :gameIds   AND (:nsfw is true OR game.ageRating < 1)  order by game.name", GameEntity.class);
            q.setParameter("gameIds", gameIds);
            q.setParameter("nsfw", nsfw);

            return q.getResultList();
        });
    }

    public List<GameEntity> runGameQuery(String query, int maxResult) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery(query, GameEntity.class);

            q.setMaxResults(maxResult);
            return q.getResultList();
        });
    }

    public List<GameEntity> runGameQuery(String query, boolean nsfw, int maxResult) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery(query, GameEntity.class);
            q.setParameter("nsfw", nsfw);

            q.setMaxResults(maxResult);
            return q.getResultList();
        });
    }

    public List<GameEntity> loadGames(String clause, String orderBy, boolean nsfw, int maxResult) {
        String where = (clause == null || clause.isEmpty()) ? "" : "AND " + clause + " ";

        //String orderBy=" order by game.added DESC";
        String query2 = "SELECT game FROM GameEntity game left join fetch game.genres where game.id in :ids " + orderBy;

        String query1 = "SELECT game.id FROM GameEntity game  where (:nsfw is true OR game.ageRating < 1) " + where + orderBy;
        return sessionFactory.fromSession(session -> {
            Query<Long> q1 = session.createQuery(query1, Long.class);
            q1.setParameter("nsfw", nsfw);

            q1.setMaxResults(maxResult);
            List<Long> ids = q1.getResultList();

            Query<GameEntity> q = session.createQuery(query2, GameEntity.class);
            q.setParameter("ids", ids);


            return q.getResultList();
        });
    }


    public void close() {
        sessionFactory.close();
    }

    public List<GameEntity> runGameQuery(String query, boolean nsfw, String paramFilter) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery(query, GameEntity.class);
            q.setParameter("nsfw", nsfw);
            q.setParameter("paramFilter", paramFilter);

            return q.getResultList();
        });
    }

    public List<GameApp> runGameQuerySelect(String query, boolean nsfw, String paramFilter) {
        return sessionFactory.fromSession(session -> {
            Query<GameEntity> q = session.createQuery(query, GameEntity.class);
            q.setParameter("nsfw", nsfw);
            q.setParameter("paramFilter", paramFilter);

            return GameMapper.INSTANCE.toGameApps(q.getResultList());
        });
    }


    public void clean() {
        sessionFactory.inTransaction(session -> {
            session.createMutationQuery("delete from GameEntity").executeUpdate();
            session.createMutationQuery("delete from GenreEntity").executeUpdate();
        });
    }

    public Long countGames(boolean nsfw) {
        return sessionFactory.fromSession(session -> {

            Query<Long> q  = session.createQuery("SELECT COUNT(game.id) FROM GameEntity game where (:nsfw is true OR game.ageRating < 1)", Long.class);
            q.setParameter("nsfw", nsfw);

            return q.getSingleResult();
        });
    }

    public Optional<CompanyEntity> findCompany(String id) {
        return sessionFactory.fromSession(session -> findCompanyById(session, id));

    }


    public void saveCompany(CompanyEntity entity) {
        sessionFactory.inTransaction(session -> {

            if (entity.getId() == null) {
                session.persist(entity);
            } else {
                session.merge(entity);
            }

        });
    }
}
