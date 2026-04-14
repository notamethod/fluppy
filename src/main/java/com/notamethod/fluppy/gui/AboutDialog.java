package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class AboutDialog extends Stage {

    static final String OURS="FluppyBox (c)2026 notamethod";
    private TextField workDirField;
    private TextField imgField;

    private PreferencesBean result;

    public AboutDialog(Stage owner) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        initOwner(owner);
        setTitle(Messages.getString("aboutdialog.titie"));

        workDirField = new TextField();
        workDirField.setPrefWidth(420);
        workDirField.setText(Configuration.dataFolder);
        workDirField.setEditable(false);

                imgField = new TextField();
        imgField.setPrefWidth(420);
        imgField.setText(Configuration.appFolder);
        imgField.setEditable(false);
        Button okButton = new Button("OK");

        Label oursLabel = new Label(OURS);

        okButton.setOnAction(e -> {
            result = null;
            close();
        });
        oursLabel.getStyleClass().add("title");
        HBox buttonBox = new HBox(10, okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(15,
               oursLabel,
               new HBox(new Label("appdir:"), workDirField),
                new HBox( new Label("appdir:"), imgField),
                buttonBox
        );
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        layout.getStyleClass().add("nightwish");
        setScene(scene);

    }


}

