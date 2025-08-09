package com.notamethod.ebox.app;

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

    public int deleteGame(String name, ApplicationList bl) {
        int nbDeleted = 0;
        String gm =name;
        ApplicationBean game = bl.getGame(gm);
        if (game.getIcon() != null) {

        }
        if (game.getGamePath() != null) {
            try {
                HelperClass.deleteDirectory(Paths.get(game.getGamePath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        bl.removeGame(gm);
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

    public void addGame(GameApp d) {
        if (HelperClass.gameIsInTempDir(d)) {
            try {
                HelperClass.moveGameToGames(d);
            } catch (IOException e) {
                log.error("error", e);
                return;
            }
        }

        save(d);
    }
}
