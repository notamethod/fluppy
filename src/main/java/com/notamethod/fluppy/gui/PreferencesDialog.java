package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.PreferencesBean;
import com.notamethod.fluppy.io.PreferencesIO;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import java.io.File;

public class PreferencesDialog extends Stage {

    private TextField cheminField;
    private CheckBox fullscreenCheck;
    private ListView<String> typeList;
    private PreferencesBean preferences;

    public PreferencesDialog(Stage owner) {
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        setTitle("Préférences");
        preferences = PreferencesIO.load("prefs.json");
        cheminField = new TextField();
        cheminField.setPrefWidth(250);
        cheminField.setText(preferences.getDosBoxPath());
        Button browseButton = new Button("Parcourir...");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner DOSBox.exe");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.exe"));
            File selectedFile = fileChooser.showOpenDialog(this);
            if (selectedFile != null) {
                cheminField.setText(selectedFile.getAbsolutePath());
            }
        });

        HBox cheminBox = new HBox(10, cheminField, browseButton);
        cheminBox.setAlignment(Pos.CENTER_LEFT);

        fullscreenCheck = new CheckBox("Plein écran");
        fullscreenCheck.setSelected(preferences.isFullScreen());
        typeList = new ListView<>();
        typeList.getItems().addAll("dosbox", "dosbox-x");
        typeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        typeList.setPrefHeight(80);

        Button saveButton = new Button("Enregistrer");
        Button cancelButton = new Button("Annuler");

        saveButton.setOnAction(e -> {
            System.out.println("Chemin DOSBox: " + cheminField.getText());
            System.out.println("Plein écran: " + fullscreenCheck.isSelected());
            System.out.println("Types sélectionnés: " + typeList.getSelectionModel().getSelectedItems());
            preferences.setDosBoxPath(cheminField.getText());
            preferences.setFullScreen(fullscreenCheck.isSelected());
            PreferencesIO.save(preferences, "prefs.json");
            close();
        });

        cancelButton.setOnAction(e -> close());

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15,
                new Label("Chemin vers DOSBox:"), cheminBox,
                fullscreenCheck,
                new Label("Type de DOSBox:"), typeList,
                buttonBox
        );
        layout.setPadding(new Insets(20));
        Scene scene=new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        setScene(scene);


    }
}

