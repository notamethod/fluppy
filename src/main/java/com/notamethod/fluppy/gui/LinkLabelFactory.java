package com.notamethod.fluppy.gui;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.Cursor;
import lombok.extern.slf4j.Slf4j;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

@Slf4j
public class LinkLabelFactory {

    public static Label createFileLink(String text, File file) {
        Label link = new Label(text);

        // Style "lien"
        link.setTextFill(Color.web("#1a73e8"));
        link.setUnderline(true);
        link.setCursor(Cursor.HAND);

        link.setOnMouseClicked(e -> {
            try {
                openFile(file);
            } catch (Exception ex) {
                log.error("error", e);
            }
        });

        return link;
    }

    public static void openFile(File file) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", file.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
