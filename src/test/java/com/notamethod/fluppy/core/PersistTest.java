package com.notamethod.fluppy.core;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersistTest {

    @Test
    void saveGame() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ebox2_pu");
        ApplicationDatabase applicationDatabase = new ApplicationDatabase(emf);


        GameManager gameManager = new GameManager(applicationDatabase);


        GameEntity gameEntity = new GameEntity();
        gameEntity.setGame("xxx");
        gameEntity.setName("xxx");
        applicationDatabase.saveGame(gameEntity);
        List<GameEntity> games = applicationDatabase.findGameByName("xxx");
        assertEquals(1, games.size(), "list with one element");

        gameEntity = new GameEntity();
        gameEntity.setName("yyy");
        applicationDatabase.saveGame(gameEntity);
        games = applicationDatabase.loadAllGames();
        assertEquals(2, games.size(), "list with two element");

        gameEntity = new GameEntity();
        gameEntity.setName("zzz");
        GenreEntity gent = new GenreEntity();
        gent.setId("puzzle");
        gent.setName("Puzzle");
        gameEntity.getGenres().add(gent);
        applicationDatabase.saveGame(gameEntity);
        games = applicationDatabase.findGameByName("zzz");
        assertEquals(1, games.getFirst().getGenres().size(), "list with two element");


        GameApp gameApp = new GameApp();
        gameApp.setName("qqq");
        gameApp.addGenre("puzzle", "Puzzle");
        gameEntity = new GameEntity();
        gameEntity.setName("qqq");
        GenreEntity genreEntityTmp=new GenreEntity();
        genreEntityTmp.setId("puzzle");
        genreEntityTmp.setName("Puzzle");
        gent = applicationDatabase.getGenre("puzzle").orElse(genreEntityTmp);

        gameEntity.getGenres().add(gent);
        applicationDatabase.saveGame(gameEntity);
        games = applicationDatabase.findGameByName("zzz");
        assertEquals(1, games.getFirst().getGenres().size(), "list with two element");
    }
    @Test
    public void testGroupByGenre(){
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ebox2_pu");
        ApplicationDatabase applicationDatabase = new ApplicationDatabase(emf);
        GameEntity gameEntity = new GameEntity();
        gameEntity.setName("zzz");
        GenreEntity gent = new GenreEntity();
        gent.setId("puzzle");
        gent.setName("Puzzle");
        GenreEntity gent2 = new GenreEntity();
        gent2.setId("action");
        gent2.setName("action");
        gameEntity.getGenres().add(gent);
        gameEntity.getGenres().add(gent2);
        applicationDatabase.saveGame(gameEntity);
        GameApp gameApp = new GameApp();
        gameApp.setName("qqq");
        gameApp.addGenre("puzzle", "Puzzle");
        gameEntity = new GameEntity();
        gameEntity.setName("qqq");
        GenreEntity genreEntityTmp=new GenreEntity();
        genreEntityTmp.setId("puzzle");
        genreEntityTmp.setName("Puzzle");
        gent = applicationDatabase.getGenre("puzzle").orElse(genreEntityTmp);

        gameEntity.getGenres().add(gent);
        applicationDatabase.saveGame(gameEntity);
        Map<String,Long> o = applicationDatabase.getTopGenres(2);
        assertEquals(2, o.size());

    }
}
