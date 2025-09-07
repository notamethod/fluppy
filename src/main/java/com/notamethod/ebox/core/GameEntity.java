
package com.notamethod.ebox.core;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "game")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
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

    private int gameYear;
    private String machine;

    private String publisher;

    private boolean favorite = false;


    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "game_genre",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<GenreEntity> genres= new HashSet<>();
}
