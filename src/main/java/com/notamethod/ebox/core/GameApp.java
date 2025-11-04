package com.notamethod.ebox.core;

import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.File;
import java.nio.file.Path;
import java.util.*;


@Data
@EqualsAndHashCode
public class GameApp {
    private Long id;
    private String name;
    private String gameExe;
    private String installer;
    private int cycles = 0;
    private Path imagePath;
    private Integer year;
    private boolean favorite;
    private Path gamePath;
    private Path exePath;
    private String extra;
    private int frameskip=0;
    private String cdrom;
    private String cdromLetter;
    private String cdromLabel;
    private String machine;

    private List<File> exeFiles = new ArrayList<>();
    private List<File> installers = new ArrayList<>();
    private Set<GenreApp> genres = new HashSet<>();


    /**
     * Update game info
     * @param srcGame
     */
    public void merge(GameApp srcGame) {
        if (name == null && srcGame.name != null) {
            this.name = srcGame.name;
        }
        if (gameExe == null && srcGame.gameExe != null) {
            this.gameExe = srcGame.gameExe;
        }

        if (installer == null && srcGame.installer != null) {
            this.installer = srcGame.installer;
        }
        if (imagePath == null && srcGame.imagePath != null) {
          //  this.imagePath = srcGame.imagePath;
        }
        if (gamePath == null && srcGame.gamePath != null) {
            this.gamePath = srcGame.gamePath;
        }
        if (exePath == null && srcGame.exePath != null) {
            this.exePath = srcGame.exePath;
        }
        if (genres.isEmpty() && !srcGame.genres.isEmpty()) {
            this.genres.addAll(srcGame.genres);
        }
    }


    public void addGenre(String id, String name) {
        GenreApp genre = new GenreApp();
        genre.setId(id);
        genre.setName(name);
        this.genres.add(genre);
    }
}
