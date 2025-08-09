package com.notamethod.ebox.app;

import lombok.Data;

import java.nio.file.Path;

@Data
public class GameApp {
    private long id;
    private String name;
    private String gameExe;
    private String genre;
    private String installer;
    private int cycles=0;
    private Path imagePath;
    private Integer year;
    private boolean favorite;
    private Path gamePath;
    private Path exePath;

}
