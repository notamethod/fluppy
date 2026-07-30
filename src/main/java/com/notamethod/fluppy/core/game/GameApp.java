package com.notamethod.fluppy.core.game;

import com.notamethod.fluppy.gui.common.FileFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Data
@EqualsAndHashCode
@ToString
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
    private Platform platform;
    private FileFormat format;
    private int frameskip=0;
    private String cdrom;
    private String cdromLetter;
    private String cdromLabel;
    private String machine;
    private String searchName;
    private int diskNumber;
    private int numberOfDisks;
    private String extraDisks;
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
    @ToString.Exclude
    private byte[] coverImage;
    private Long edition;

    public void addGenre(String id, String name) {
        GenreApp genre = new GenreApp();
        genre.setId(id);
        genre.setName(name);
        this.genres.add(genre);
    }

    public Map<Integer, String> toExtraDiskList() {

        if (extraDisks == null) {
            return new HashMap<>();
        }
        return Arrays.stream(extraDisks.split(";"))
                .map(part -> part.split("#", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(
                        arr -> Integer.parseInt(arr[0]),
                        arr -> arr[1],
                        (v1, v2) -> v1, // en cas de clé dupliquée, garde la première valeur
                        TreeMap::new
                ));
    }
}
