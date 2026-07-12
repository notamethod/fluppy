package com.notamethod.fluppy.core.game;

import com.notamethod.fluppy.core.ApplicationDatabase;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.dosbox.DosBoxException;
import com.notamethod.fluppy.dosbox.DosBoxManager;
import com.notamethod.fluppy.dosbox.UAEManager;
import com.notamethod.fluppy.gui.PanelListener;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class GameManager {
    private static final int MAX_GENRE = 3;
    private ApplicationDatabase applicationDatabase;
    private PreferencesBean preferences;
    private DosBoxManager dosBoxManager;
    private UAEManager uaeManager;
    public GameManager(ApplicationDatabase applicationDatabase, PreferencesBean preferences, DosBoxManager dosBoxManager) {
        this.applicationDatabase = applicationDatabase;
        this.preferences=preferences;
        this.dosBoxManager=dosBoxManager;
    }

    public int deleteGame(GameApp gameApp) {
        int nbDeleted = 0;

        if (gameApp.getGamePath() != null && HelperClass.gameIsInGameDir(gameApp)) {
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
        return null;
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
    public GameApp loadFullGame(long id) {
        GameEntity gameEntity = applicationDatabase.findFullGameById(id);

            return GameMapper.INSTANCE.toFullGameApp(gameEntity);
    }

    /**
     * 
     * @param game game to add in database
     * @throws GameAlreadyPresentException
     */
    public void addGame(GameApp game) throws GameAlreadyPresentException {
        if (game.getName() == null) {
            log.error("game name is null");
            return;
        }

        log.debug("adding game ->{} <- to database", game.getName());
        List<GameEntity> entiites = applicationDatabase.findGameByUnique(game.getName(), game.getYear(),game.getLanguage());
        if (!entiites.isEmpty()) {
            if (entiites.size()==1){
                if (game.getDiskNumber()>0){
                    GameEntity storedGame = entiites.getFirst();
                    if (!isDiskPresent(game,storedGame)){
                        applicationDatabase.saveGame(storedGame);
                        return;
                    }
                }
            }
            StringBuilder b = new StringBuilder();
            for (GameEntity gamelog:entiites){
                b.append(gamelog.getId()).append("/").append(gamelog.getGame()).append("/")
                        .append(gamelog.getGameYear())
                        .append("/").append(gamelog.getGamePath())
                        .append(">>>");
            }
            throw new GameAlreadyPresentException("game already in database: "+b.toString());
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

    private Boolean isDiskPresent(GameApp game, GameEntity storedGame) {
        List<String> diskList = new ArrayList<>();
        if (storedGame.getExtraDisks()!=null){
            String[] disks = storedGame.getExtraDisks().split(";");
            diskList = new ArrayList<>(Arrays.asList(disks));
        }

       // Map<String,String> diskSet = new HashMap<>();
        Boolean found = null;

       if (game.getDiskNumber()>1){
           found=false;
           for (String disk:diskList){
               String[] diskInfo=disk.split("#");
             //  diskSet.put(diskInfo[0],diskInfo[1]);
               if (String.valueOf(game.getDiskNumber()).equals(diskInfo[0])){
                   found=true;
                   break;
               }
           }
           if (!found){
               String newDisk = game.getDiskNumber()+"#"+game.getGamePath().toAbsolutePath().toString();
               diskList.add(newDisk);
               storedGame.setExtraDisks(String.join( ";",diskList));
           }
       }else{
           if (storedGame.getExePath()==null || storedGame.getExePath().equals("")){
               storedGame.setExePath(game.getExePath().toAbsolutePath().toString());
               found= false;
           }

       }
       return found;
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
        //return GameMapper.INSTANCE.toGameApps(applicationDatabase.runGameQuery("SELECT game FROM GameEntity game left join fetch game.genres where game.timePlayed>60 and (:nsfw is true OR game.ageRating < 1) order by game.timePlayed DESC",preferences.isNsfw(),maxResult));
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.loadGames("game.timePlayed>60","order by game.timePlayed DESC" ,preferences.isNsfw(),maxResult));

    }

    public  List<GameApp> getFavoriteGames(int maxResult) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.loadGames("game.favorite=true", "order by game.name",preferences.isNsfw(),maxResult));
    }
    public List<GameApp> getLastAdded(int maxResult) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.loadGames("","order by game.added DESC", preferences.isNsfw(), maxResult));
    }
    public List<GameApp> getLastPlayed() {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.loadGames("","order by game.lastPlayed DESC",preferences.isNsfw(),5));
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
    public List<GameApp> getFromPublisher(String publisher , int count) {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.findGameByPublisher(publisher, preferences.isNsfw(), count));
    }

    public long runGame(GameApp game, String screenRez, PanelListener listener) throws DosBoxException {

        AtomicReference<Long> duration= new AtomicReference<>(0L);
        if (game.getPlatform()!=null && game.getPlatform().contains("amiga")){
            if (uaeManager==null){
                uaeManager = new UAEManager();
            }
            uaeManager.runApplication(
                    game.getGameExe(),
                    game,
                    screenRez,
                    listener,
                    line -> log.info("[FSUAE] " + line),
                    err -> log.error("[FSUAE] " + err),
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
                        if (duration.get() > 0) {
                            updateTime(game, duration.get() / 1000);
                        }
                        System.out.println("Exit code : " + result.exitCode);
                    }
            );

            return duration.get();

        }else {
            dosBoxManager.runApplication(
                    game.getGameExe(),
                    game,
                    screenRez,
                    listener,
                    line -> log.info("[DOSBOX] " + line),
                    err -> log.error("[DOSBOX] " + err),
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
                        if (duration.get() > 0) {
                            updateTime(game, duration.get() / 1000);
                        }
                        System.out.println("Exit code : " + result.exitCode);
                    }
            );

            return duration.get();
        }

    }

    public long countGames() {
        return applicationDatabase.countGames(preferences.isNsfw());
    }
    public Statistics getStatistics() {
        return applicationDatabase.statistics(preferences.isNsfw());
    }

    public CompanyEntity loadCompany(String id) {
        return applicationDatabase.findCompany(id).orElse(null);
    }
    public CompanyEntity loadCompany(GameApp game ) {
        if (game.getPublisher()!=null) {
            return applicationDatabase.findCompany(game.getPublisher().getId()).orElse(null);
        }
        GameEntity gameEntity = applicationDatabase.findFullGameById(game.getId());
        return gameEntity.getPublisher();
    }

    public void save(CompanyEntity company) {
         applicationDatabase.saveCompany(company);
    }

    public DosBoxManager getDosBoxManager() {
        return dosBoxManager;
    }
}
