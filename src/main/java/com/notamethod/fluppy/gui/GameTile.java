package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.GameApp;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.Data;

@Data
public class GameTile extends VBox {
    GameApp game;

    public GameTile(double spacing, GameApp gameApp, Node... children) {
        super(spacing, children);
        getStyleClass().add("blockGame");
        this.game = gameApp;
    }
}
