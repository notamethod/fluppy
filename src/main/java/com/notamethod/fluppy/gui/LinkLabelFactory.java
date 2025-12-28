package com.notamethod.fluppy.gui;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.Cursor;

import java.awt.Desktop;
import java.io.File;

public class LinkLabelFactory {

    public static Label createFileLink(String text, File file) {
        Label link = new Label(text);

        // Style "lien"
        link.setTextFill(Color.web("#1a73e8"));
        link.setUnderline(true);
        link.setCursor(Cursor.HAND);

        link.setOnMouseClicked(e -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return link;
    }
}
