package com.notamethod.ebox.core;

import lombok.Data;
import lombok.EqualsAndHashCode;


import java.nio.file.Path;
import java.util.Collection;


@Data
@EqualsAndHashCode
public class GameApp {
    private Long id;
    private String name;
    private String gameExe;
    private String genre;
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


    public void merge(GameApp srcGame) {
        if (name == null && srcGame.name != null) {
            this.name = srcGame.name;
        }
        if (gameExe == null && srcGame.gameExe != null) {
            this.gameExe = srcGame.gameExe;
        }
        if (genre == null && srcGame.genre != null) {
            this.genre = srcGame.genre;
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
    }


}
