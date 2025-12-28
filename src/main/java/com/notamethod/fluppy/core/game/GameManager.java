package com.notamethod.fluppy.core.game;

import com.notamethod.fluppy.core.ApplicationDatabase;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class GameManager {
    private static final int MAX_GENRE = 3;
    private ApplicationDatabase applicationDatabase;
    private PreferencesBean preferences;

    public GameManager(ApplicationDatabase applicationDatabase, PreferencesBean preferences) {
        this.applicationDatabase = applicationDatabase;
        this.preferences=preferences;
    }

    public int deleteGame(GameApp gameApp) {
        int nbDeleted = 0;

        if (gameApp.getGamePath() != null) {
            try {
                HelperClass.deleteDirectory(gameApp.getGamePath());
            } catch (IOException e) {
                log.warn("deleteGame game", e);
            }
        }
        GameEntity entity = applicationDatabase.findGameById(gameApp.getId());
        applicationDatabase.deleteGame(entity);
        nbDeleted++;
        return nbDeleted;
    }


    public List<GameApp> loadAll() {
        List<GameApp> games = GameMapper.INSTANCE.toGameApps(applicationDatabase.loadAllGames());
        log.debug("Loaded {} games", games.size());
        return games;
    }

    public void save(GameApp gameApp) {
        GameEntity gameEntity = GameMapper.INSTANCE.toEntity(gameApp);
        for (GenreApp genre : gameApp.getGenres()) {
            GenreEntity gent = applicationDatabase.getGenre(genre.getId()).orElse(GameMapper.INSTANCE.toEntity(genre));
            gameEntity.getGenres().add(gent);
        }
        applicationDatabase.saveGame(gameEntity);
    }

    public GameApp getGame(String name) {
        if (name == null)
            return null;
        name = name.toLowerCase();
        List<GameEntity> entiites = applicationDatabase.findGameByName(name);
        if (!entiites.isEmpty()) {
            return GameMapper.INSTANCE.toGameApp(entiites.get(0));
        }
        return null;
    }

    public void addGame(GameApp game) throws GameManagerException {
        if (game.getName() == null) {
            log.error("game name is null");
            return;
        }

        log.debug("adding game ->{} <- to database", game.getName());
        List<GameEntity> entiites = applicationDatabase.findGameByNameAndYear(game.getName(), game.getYear());
        if (!entiites.isEmpty()) {
            StringBuilder b = new StringBuilder();
            for (GameEntity gamelog:entiites){
                b.append(gamelog.getId()).append("/").append(gamelog.getGame()).append("/")
                        .append(gamelog.getGameYear())
                        .append("/").append(gamelog.getGamePath())
                        .append(">>>");
            }
            throw new GameManagerException("game already in database: "+b.toString());
        }
        if (HelperClass.gameIsInTempDir(game)) {
            try {
                HelperClass.moveGameToGames(game);
            } catch (IOException e) {
                log.error("error", e);
                return;
            }
        }
        save(game);
    }

    public void updateTime(GameApp game, Long time) {
        GameEntity entiity= applicationDatabase.findGameById(game.getId());

        entiity.setTimePlayed(entiity.getTimePlayed()==null?time:entiity.getTimePlayed()+time);
        entiity.setLastPlayed(LocalDateTime.now());
        applicationDatabase.saveGame(entiity);
    }

    public List<GameApp> loadAllButNot(Set<Long> gameIds) {
        List<GameApp> games = GameMapper.INSTANCE.toGameApps(applicationDatabase.loadAllGamesButNot(gameIds, preferences.isNsfw()));
        log.debug("Loaded {} games", games.size());
        return games;
    }

    public  List<GameApp> getMostPlayedGames(int maxResult) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game where game.timePlayed>60 and (:nsfw is true OR game.ageRating < 1) order by game.timePlayed DESC",preferences.isNsfw(),maxResult));
    }

    public  List<GameApp> getFavoriteGames(int maxResult) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game where game.favorite=true order by game.name",maxResult));
    }

    public List<GameApp> getLastAdded(int maxResult) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game where (:nsfw is true OR game.ageRating < 1)  order by game.added DESC", preferences.isNsfw(), maxResult));
    }
    public List<GameApp> getLastPlayed() {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game where (:nsfw is true OR game.ageRating < 1) order by game.lastPlayed DESC",preferences.isNsfw(),5));
    }

    public List<GameApp> getFromGenre(String genre, int limit) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.findGameByGenre(genre, limit));

    }

    public List<GameApp> searchByName(String paramFilter) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game where LOWER(game.name) LIKE LOWER(CONCAT('%', :paramFilter, '%')) AND (:nsfw is true OR game.ageRating < 1) order by game.lastPlayed DESC",preferences.isNsfw(), paramFilter));
    }

    public List<GameApp> getFromYear(Integer year , int count) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.findGameByYear(year, preferences.isNsfw(), count));
    }
}
