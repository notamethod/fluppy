
package com.notamethod.fluppy.core;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "genre")
public class GenreEntity {

    @Id
    private String id;
    private String name;

    @ManyToMany(mappedBy = "genres")
    Set<GameEntity> games = new HashSet<>();
}
