package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.dosbox.DosBoxManager;
import com.notamethod.fluppy.dosbox.DosboxType;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

@Slf4j
public class PreferencesDialog extends Stage {

    private TextField cheminField;
    private CheckBox fullscreenCheck;
    private ListView<String> typeList;
    private PreferencesBean preferences;
    private CheckBox nsfwCheck;
    private PreferencesBean result;

    public PreferencesDialog(Stage owner) {
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        setTitle("Préférences");
        preferences = PreferencesIO.load();
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
        nsfwCheck = new CheckBox("NSFW");
        nsfwCheck.setSelected(preferences.isNsfw());
        Button saveButton = new Button("Enregistrer");
        Button cancelButton = new Button("Annuler");

        saveButton.setOnAction(e -> {
            log.debug("Chemin DOSBox: " + cheminField.getText());
            log.debug("Plein écran: " + fullscreenCheck.isSelected());
            log.debug("Types sélectionnés: " + typeList.getSelectionModel().getSelectedItems());
            preferences.setDosBoxPath(cheminField.getText());
            DosboxType dosboxType =  DosBoxManager.detectDosboxType(Path.of(preferences.getDosBoxPath()));
            preferences.setDosBoxType(dosboxType.toString());
            preferences.setFullScreen(fullscreenCheck.isSelected());
            preferences.setNsfw(nsfwCheck.isSelected());
            PreferencesIO.save(preferences, "prefs.json");
            result = preferences;
            close();
        });

        cancelButton.setOnAction(e -> {
            result = null;
            close();
        });

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15,
                new Label("Chemin vers DOSBox:"), cheminBox,
                fullscreenCheck,
                new Label("Type de DOSBox:"), typeList, nsfwCheck,
                buttonBox
        );
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        setScene(scene);


    }

    public PreferencesBean showAndWaitForResult() {
        showAndWait(); // bloque jusqu'à fermeture
        return result;
    }
}

