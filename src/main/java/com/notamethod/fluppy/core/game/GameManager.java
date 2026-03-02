package com.notamethod.fluppy.core.game;

import com.notamethod.fluppy.core.ApplicationDatabase;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.dosbox.DosBoxException;
import com.notamethod.fluppy.dosbox.DosBoxManager;
import com.notamethod.fluppy.gui.PanelListener;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class GameManager {
    private static final int MAX_GENRE = 3;
    private ApplicationDatabase applicationDatabase;
    private PreferencesBean preferences;
    private DosBoxManager dosBoxManager;
    public GameManager(ApplicationDatabase applicationDatabase, PreferencesBean preferences, DosBoxManager dosBoxManager) {
        this.applicationDatabase = applicationDatabase;
        this.preferences=preferences;
        this.dosBoxManager=dosBoxManager;
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

        applicationDatabase.saveGame(gameApp);
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
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game left join fetch game.genres where game.timePlayed>60 and (:nsfw is true OR game.ageRating < 1) order by game.timePlayed DESC",preferences.isNsfw(),maxResult));
    }

    public  List<GameApp> getFavoriteGames(int maxResult) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game left join fetch game.genres where game.favorite=true order by game.name",maxResult));
    }

    public List<GameApp> getLastAdded(int maxResult) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game left join fetch game.genres where (:nsfw is true OR game.ageRating < 1)  order by game.added DESC", preferences.isNsfw(), maxResult));
    }
    public List<GameApp> getLastPlayed() {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game left join fetch game.genres where (:nsfw is true OR game.ageRating < 1) order by game.lastPlayed DESC",preferences.isNsfw(),5));
    }

    public List<GameApp> getFromGenre(String genre, int limit, Set<Long> gameIds) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.findGameByGenre(genre, limit, gameIds));

    }

    public List<GameApp> searchByName(String paramFilter) {
        return applicationDatabase.runGameQuerySelect("SELECT game FROM GameEntity game where LOWER(game.name) LIKE LOWER(CONCAT('%', :paramFilter, '%')) AND (:nsfw is true OR game.ageRating < 1) order by game.lastPlayed DESC",preferences.isNsfw(), paramFilter);
    }

    public List<GameApp> getFromYear(Integer year , int count) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.findGameByYear(year, preferences.isNsfw(), count));
    }

    public long runGame(GameApp game, String screenRez, PanelListener listener) throws DosBoxException {

        AtomicReference<Long> duration= new AtomicReference<>(0L);
        dosBoxManager.runApplication(
                game.getGameExe(),
                game,
                screenRez,
                listener,
                line -> log.info("[DOSBOX] "+line),
                err -> log.error("[DOSBOX] " + err ),
                result -> {

                    if (listener != null) {

                        listener.onExitGame();
                    }
                    if (result.success) {
                        System.out.println("DOSBox OK");
                    } else {
                        System.out.println("Erreur : " + result.error);
                    }

                    System.out.println("Durée : " + result.durationMillis + " ms");
                    duration.set(result.durationMillis);
                    if (duration.get()>0){
                        updateTime(game, duration.get()/1000);
                    }
                    System.out.println("Exit code : " + result.exitCode);
                }
        );
        return duration.get();

    }
}
