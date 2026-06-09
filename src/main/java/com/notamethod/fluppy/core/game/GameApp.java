package com.notamethod.fluppy.core.game;

import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;


@Data
@EqualsAndHashCode
public class GameApp {
    private Long id;
    private String name;
    private String gameExe;
    private String installer;
    private int cycles = 0;
    private Integer year;
    private boolean favorite;
    private Path gamePath;
    private Path exePath;
    private Path protectionPath;
    private Path manualPath;
    private String extra;
    private String platform;
    private int frameskip=0;
    private String cdrom;
    private String cdromLetter;
    private String cdromLabel;
    private String machine;
    private String searchName;
    private List<File> exeFiles = new ArrayList<>();
    private List<File> installers = new ArrayList<>();
    private Set<GenreApp> genres = new HashSet<>();
    private int ageRating;

    private LocalDateTime added;
    private LocalDateTime lastPlayed;
    private Long timePlayed;
    private String comment;
    private String language;
    private Company publisher;
    private byte[] coverImage;
    private Long edition;

    public void addGenre(String id, String name) {
        GenreApp genre = new GenreApp();
        genre.setId(id);
        genre.setName(name);
        this.genres.add(genre);
    }
}
