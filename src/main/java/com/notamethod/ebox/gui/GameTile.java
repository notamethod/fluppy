package com.notamethod.ebox.gui;

import com.notamethod.ebox.app.ApplicationBean;
import com.notamethod.ebox.app.GameApp;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.Data;

@Data
public class GameTile extends VBox {
    GameApp game;

    public GameTile(double spacing, GameApp gameApp, Node... children) {
        super(spacing, children);
        this.game = gameApp;
    }
}
