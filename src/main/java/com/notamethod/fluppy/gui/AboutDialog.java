package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AboutDialog extends Stage {

    static final String OURS = """
            FluppyBox version 0.2
            
            (c)2026 notamethod""";
    private TextField dataFolder;
    private TextField appFolder;
    private TextField configFolder;

    private PreferencesBean result;

    public AboutDialog(Stage owner) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        initOwner(owner);
        setTitle(Messages.getString("aboutdialog.titie"));

        configFolder = new TextField(Configuration.configFolder);
        configFolder.setPrefWidth(420);
        configFolder.setEditable(false);

        dataFolder = new TextField(Configuration.dataFolder);
        dataFolder.setPrefWidth(420);
        dataFolder.setEditable(false);

        appFolder = new TextField(Configuration.appFolder);
        appFolder.setPrefWidth(420);
        appFolder.setEditable(false);
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
                new HBox(new Label("config:"), configFolder),
               new HBox(new Label("data:"), dataFolder),
                new HBox( new Label("app:"), appFolder),
                buttonBox
        );
        layout.setPadding(new Insets(20));
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        layout.getStyleClass().add("nightwish");
        setScene(scene);

    }


}

