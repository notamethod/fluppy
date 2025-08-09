package com.notamethod.ebox.core;

import com.notamethod.ebox.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class GameManager {
    private ApplicationDatabase applicationDatabase;

    public GameManager(ApplicationDatabase applicationDatabase) {
        this.applicationDatabase = applicationDatabase;
    }

    public int deleteGame(GameApp gameApp) {
        int nbDeleted = 0;

        if (gameApp.getGamePath() != null) {
            try {
                HelperClass.deleteDirectory(gameApp.getGamePath());
            } catch (IOException e) {
                log.warn("delete game", e);
            }
        }
        GameEntity entity = applicationDatabase.findById(gameApp.getId());
      applicationDatabase.delete(entity);
        nbDeleted++;
        return nbDeleted;
    }


    public List<GameApp> loadAll() {
        return  GameMapper.INSTANCE.toGameApps(applicationDatabase.loadAll());
    }

    public void save(GameApp game) {
        applicationDatabase.save(GameMapper.INSTANCE.toEntity(game));
    }

    public GameApp getGame(String name) {
        if(name == null)
            return null;

        name = name.toLowerCase();
        List<GameEntity> entiites=applicationDatabase.findByName(name);
        if (!entiites.isEmpty()){
            return GameMapper.INSTANCE.toGameApp(entiites.get(0));
        }
        return null;
    }

    public void addGame(GameApp game) throws GameManagerException {
        List<GameEntity> entiites=applicationDatabase.findByNameAndYear(game.getName(), game.getYear());
        if (!entiites.isEmpty()){
            throw new GameManagerException("game already in database");
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
}
