package com.notamethod.fluppy.core;

import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameEntity;
import com.notamethod.fluppy.core.game.GenreEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersistTest {

    private EntityManagerFactory emf;
    private ApplicationDatabase applicationDatabase;

    @BeforeEach
    void setup() {
        emf = Persistence.createEntityManagerFactory("ebox2_pu");
        applicationDatabase = new ApplicationDatabase(emf);
        System.out.println("adb: "+applicationDatabase);
    }

    @AfterEach
    void tearDown() {
        applicationDatabase.close();
    }

    @Test
    void saveGame() {

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

    @Test
    public void testGroupByYear(){

        GameEntity gameEntity = new GameEntity();
        gameEntity.setName("zzz");
        gameEntity.setGameYear(1990);
        GameEntity gameEntity2 = new GameEntity();
        gameEntity2.setName("aaa");
        gameEntity2.setGameYear(1990);
        GameEntity gameEntity3 = new GameEntity();
        gameEntity3.setName("bbb");
        gameEntity3.setGameYear(1994);
        GameEntity gameEntity4 = new GameEntity();
        gameEntity4.setName("bbb");


        applicationDatabase.saveGame(gameEntity);
        applicationDatabase.saveGame(gameEntity2);
        applicationDatabase.saveGame(gameEntity3);
        applicationDatabase.saveGame(gameEntity4);
        Map<Integer,Long> o = applicationDatabase.getTopYears(20);
        assertEquals(2, o.size());

    }
}
