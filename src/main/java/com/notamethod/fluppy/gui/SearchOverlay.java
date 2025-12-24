package com.notamethod.fluppy.gui;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class SearchOverlay extends StackPane {

    private final TextField field = new TextField();
    private final StringProperty query = new SimpleStringProperty("");

    public SearchOverlay() {
        setPickOnBounds(false); // laisse passer les clics quand invisible
        setVisible(false);
        setOpacity(0);
        setFocusTraversable(false); // le container n’a pas besoin du focus
        field.setFocusTraversable(true);
        // Fond semi-transparent
        Rectangle backdrop = new Rectangle();
        backdrop.setFill(Color.rgb(0, 0, 0, 0.10));
        backdrop.widthProperty().bind(widthProperty());
       backdrop.heightProperty().bind(heightProperty());

        backdrop.setArcWidth(12);
        backdrop.setArcHeight(12); // Bind sur la taille de la searchBox, pas sur tout l’overlay

 backdrop.setOnMouseClicked(e -> hide());

        // Style du champ
        field.setPromptText("Rechercher…");
        field.setStyle("""
                    -fx-font-size: 16px;
                    -fx-background-radius: 8;
                    -fx-background-color: rgba(30,30,30,0.9);
                    -fx-text-fill: white;
                    -fx-prompt-text-fill: gray;
                   -fx-padding: 8 14 10 14; /* top right bottom left */
                """);
        field.setMinHeight(40);
        field.setPrefHeight(40);
        field.setMaxHeight(40);
        // Centrage


        SVGPath loupe = new SVGPath();
        loupe.setContent("M10 18a8 8 0 1 1 5.3-14l5.4 5.4-1.4 1.4-5.4-5.4A8 8 0 0 1 10 18z");
        loupe.setFill(Color.WHITE);
        loupe.setScaleX(0.8);
        loupe.setScaleY(0.8);


        HBox searchBox = new HBox(field);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(10));
        searchBox.setStyle(""" 
                -fx-background-color: rgba(30,30,30,0.4); 
                -fx-background-radius: 8; """);
        searchBox.setOnMouseClicked(e -> hide());
        StackPane box = new StackPane(searchBox);
        box.setPadding(new Insets(40));
        StackPane.setAlignment(box, Pos.TOP_CENTER);
        getChildren().addAll(backdrop,box);

        // Bind de la query
        query.bind(field.textProperty());

        // ESC pour fermer
        field.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) { //BACK_SPACE ?
                hide();
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                field.setText("");
                e.consume();
                hide();
            }
        });
    }

    public StringProperty queryProperty() {
        return query;
    }

    // --- Animations ---
    public void show() {
        if (isVisible()) return;
        setVisible(true);
        field.clear();
        field.requestFocus();

        FadeTransition ft = new FadeTransition(Duration.millis(150), this);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public void hide() {
        if (!isVisible()) return;

        FadeTransition ft = new FadeTransition(Duration.millis(150), this);
        ft.setFromValue(getOpacity());
        ft.setToValue(0);
        ft.setOnFinished(e -> setVisible(false));
        ft.play();
    }

    public void requestFocusOnField() {
        field.requestFocus();
        field.positionCaret(field.getText().length());
    }

    public void appendToQuery(String c) {
        field.appendText(c);
    }
}
