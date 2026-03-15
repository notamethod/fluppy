package com.notamethod.fluppy.gui;

import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.dosbox.DosBoxManager;
import com.notamethod.fluppy.dosbox.DosboxType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

@Slf4j
public class StoryDialog extends Stage {

    private TextField cheminField;
    StringProperty mainText = new SimpleStringProperty();
    String titleText ;

    public StoryDialog(Stage owner) {
        initModality(Modality.APPLICATION_MODAL);
       initStyle(StageStyle.UNDECORATED);
        initOwner(owner);
      //  setTitle("Coucou");



        Button cancelButton = new Button("Next");

        cancelButton.setOnAction(e -> {
         //   result = null;
            close();
        });
        VBox textBox = new VBox(10);
        Label label =new Label();
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.textProperty().bind(mainText);
        textBox.getChildren().addAll(label);
        HBox buttonBox = new HBox(10, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Label title = new Label(titleText);
        title.getStyleClass().add("title");
        VBox layout = new VBox(15
        );
        if (titleText!=null){
            layout.getChildren().add(
                    title
            );
        }
        layout.getChildren().addAll(
                textBox,
                buttonBox
        );
        layout.setPadding(new Insets(20));

        layout.setFillWidth(true);
       // layout.getStyleClass().add("scanline");
        VBox.setVgrow(title, Priority.ALWAYS);
        StackPane root = new StackPane(layout);
        root.getStyleClass().add("sierra");
      layout.setPrefWidth(400);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        setScene(scene);
        sizeToScene();

    }


    public void setText(String text) {
        mainText.set(text);
    }
}

