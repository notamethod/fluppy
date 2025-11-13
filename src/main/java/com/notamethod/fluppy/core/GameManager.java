package com.notamethod.fluppy.core;

import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
                log.warn("deleteGame game", e);
            }
        }
        GameEntity entity = applicationDatabase.findGameById(gameApp.getId());
        applicationDatabase.deleteGame(entity);
        nbDeleted++;
        return nbDeleted;
    }


    public List<GameApp> loadAll() {
        return GameMapper.INSTANCE.toGameApps(applicationDatabase.loadAllGames());
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
        List<GameEntity> entiites = applicationDatabase.findGameByNameAndYear(game.getName(), game.getYear());
        if (!entiites.isEmpty()) {
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
