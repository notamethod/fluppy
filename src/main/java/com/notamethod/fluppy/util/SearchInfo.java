package com.notamethod.fluppy.util;

import lombok.Data;

@Data
public class SearchInfo {
    private String title;
    private String year;
    private Integer disk;
    private Integer totalDisk;

    public SearchInfo(String titleGame) {
        this.title=titleGame;
    }

    public SearchInfo(String titleGame, String year) {
        this.title=titleGame;
        this.year=year;
    }
}

