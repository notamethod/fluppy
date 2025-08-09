
package com.notamethod.ebox.app;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "game")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    private String name;


    private String gamePath;
    private String imagePath;
    private String exePath;

    private String installer;
    private String extra;
    private String game;
    //
    private String cover;

    private Integer cycles = null;

    private String genre;

    private int gameYear;

    private String publisher;

    private boolean favorite = false;


}
