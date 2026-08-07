package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.platform.dosbox.DosBoxManager;
import com.notamethod.fluppy.platform.dosbox.DosboxType;
import com.notamethod.fluppy.util.Installer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

@Slf4j
public class PreferencesDialog extends Stage {

    private TextField cheminField;
    private TextField fsuaePathField;
    private TextField ffmpegPathField;
    private TextField kickstartPathField;
    private CheckBox fullscreenCheck;
    private PreferencesBean preferences;
    private CheckBox nsfwCheck;
    private PreferencesBean result;

    public PreferencesDialog(Stage owner) {
        initModality(Modality.APPLICATION_MODAL);
       initStyle(StageStyle.UNDECORATED);
        initOwner(owner);
        setTitle("Préférences");
        preferences = PreferencesIO.load();
        cheminField = new TextField();
        cheminField.setPrefWidth(350);
        cheminField.setText(preferences.getDosBoxPath());
        fsuaePathField = new TextField();
        fsuaePathField.setPrefWidth(350);
        fsuaePathField.setText(preferences.getFsuaePath());
        ffmpegPathField = new TextField();
        ffmpegPathField.setPrefWidth(350);
        ffmpegPathField.setText(preferences.getFfmpegPath());
        kickstartPathField = new TextField();
        kickstartPathField.setPrefWidth(350);
        kickstartPathField.setText(preferences.getKickstartPath());
        Button installFSButton = new Button("<-");
        Button installDBButton = new Button("<-");
        Button browseButton = new Button("Parcourir...");
        Button fsBrowseButton = new Button("Parcourir...");
        Button ffBrowseButton = new Button("Parcourir...");
        Button ksBrowseButton = new Button("Parcourir...");
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner DOSBox.exe");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.exe"));
            File selectedFile = fileChooser.showOpenDialog(this);
            if (selectedFile != null) {
                cheminField.setText(selectedFile.getAbsolutePath());
            }
        });
        fsBrowseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner FSUAE.exe");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.exe"));
            File selectedFile = fileChooser.showOpenDialog(this);
            if (selectedFile != null) {
                fsuaePathField.setText(selectedFile.getAbsolutePath());
            }
        });
        ffBrowseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner exe");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.exe"));
            File selectedFile = fileChooser.showOpenDialog(this);
            if (selectedFile != null) {
                ffmpegPathField.setText(selectedFile.getAbsolutePath());
            }
        });
        ksBrowseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner rom kickstart");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.rom"));
            File selectedFile = fileChooser.showOpenDialog(this);
            if (selectedFile != null) {
                kickstartPathField.setText(selectedFile.getAbsolutePath());
            }
        });
        installFSButton.setOnAction(resultat -> {
            Installer installer = new Installer();
            String exeFile = installer.application(new ProgressCallback() {
                @Override
                public void onProgress(String message) {
                    // Update UI or log progress message
                    System.out.println("Progress: " + message);

                }
            }, Installer.fsuae);
            if (exeFile != null) {
                preferences.setFsuaePath(exeFile);
                fsuaePathField.setText(exeFile);
                PreferencesIO.save(preferences);
            }
        });
        installDBButton.setOnAction(resultat -> {
            Installer installer = new Installer();
            String exeFile = installer.application(new ProgressCallback() {
                @Override
                public void onProgress(String message) {
                    // Update UI or log progress message
                    System.out.println("Progress: " + message);

                }
            }, Installer.dosbox);
            if (exeFile != null) {
                preferences.setDosBoxPath(exeFile);
                cheminField.setText(exeFile);
                PreferencesIO.save(preferences);
            }
        });
        HBox cheminBox = new HBox(10, cheminField, browseButton, installDBButton);
        cheminBox.setAlignment(Pos.CENTER_LEFT);
        HBox fsuaePathBox = new HBox(10, fsuaePathField, fsBrowseButton, installFSButton);
        fsuaePathBox.setAlignment(Pos.CENTER_LEFT);
        HBox ksPathBox = new HBox(10, kickstartPathField, ksBrowseButton);
        ksPathBox.setAlignment(Pos.CENTER_LEFT);
        HBox ffmpegPathBox = new HBox(10, ffmpegPathField, ffBrowseButton);
        ffmpegPathBox.setAlignment(Pos.CENTER_LEFT);
        fullscreenCheck = new CheckBox("Plein écran");
        fullscreenCheck.setSelected(preferences.isFullScreen());

        nsfwCheck = new CheckBox("NSFW");
        nsfwCheck.setSelected(preferences.isNsfw());
        Button saveButton = new Button("Enregistrer");
        Button cancelButton = new Button("Annuler");

        saveButton.setOnAction(e -> {
            preferences.setDosBoxPath(cheminField.getText());
            preferences.setFsuaePath(fsuaePathField.getText());
            preferences.setKickstartPath(kickstartPathField.getText());
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
        Label title = new Label("Preferences");
        title.getStyleClass().add("title");
        VBox layout = new VBox(15,
                title,
                new Label("Chemin vers DOSBox:"), cheminBox,
                new Label("Chemin vers FS-UAE:"), fsuaePathBox,
                new Label("Chemin vers rom kickstart:"), ksPathBox,
                new Label("Options:"),   fullscreenCheck,nsfwCheck,
                buttonBox
        );
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        layout.getStyleClass().add("nightwish");
        setScene(scene);


    }

    public PreferencesBean showAndWaitForResult() {
        showAndWait(); // bloque jusqu'à fermeture
        return result;
    }


}

