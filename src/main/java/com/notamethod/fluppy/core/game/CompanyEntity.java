
package com.notamethod.fluppy.core.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "company")
public class CompanyEntity {

    @Id
    private String id;
    private String name;

    @Lob
    @Column(name = "image")
    private byte[] image;


    @OneToMany(mappedBy = "publisher", orphanRemoval = true)
    private List<GameEntity> games = new ArrayList<>();

    public CompanyEntity() {
    }
    public CompanyEntity(String id, String name, byte[] image) {
        this.id=id;
        this.name=name;
        this.image=image;
    }
}
