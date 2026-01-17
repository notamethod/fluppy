package com.notamethod.fluppy.gui;

import javafx.geometry.Pos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;

public class RatingSkin extends SkinBase<RatingControl> {

    private static final int STAR_COUNT = 5;

    public RatingSkin(RatingControl control) {
        super(control);

        HBox box = new HBox(0);
        box.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < STAR_COUNT; i++) {
            StarNode star = new StarNode(i, control);
            box.getChildren().add(star);
        }

        getChildren().add(box);
    }
}
