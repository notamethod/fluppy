package com.notamethod.fluppy.gui;


import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.util.HelperClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class SameGameDialog extends Stage {


   public record ActionResult(SameGameAction type, String valeur) {}
    @Getter
    private ActionResult result;
    public SameGameDialog(GameApp first) {


        initModality(Modality.APPLICATION_MODAL);

        setTitle("Add Game");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(20));

        TextField varText= new TextField();
        HBox variantBox = new HBox(10, new Label("Variant"));
        variantBox.getChildren().add(varText);
        variantBox.setVisible(false);


        Button okButton = new Button("OK");

        Button cancelButton = new Button("Annuler");

        okButton.setOnAction(e -> {
            if (!varText.getText().isEmpty()) {
                ActionResult a = new ActionResult(SameGameAction.VARIANT, varText.getText());
                result=a;
            }

          //  result=actionResult;
            close();
        });

        cancelButton.setOnAction(e -> {
            result= new ActionResult(SameGameAction.NOTHING,"");

            close();
        });

        HBox buttonBox = new HBox(10, okButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        ToggleGroup group = new ToggleGroup();
        VBox radioBox = new VBox(10);

        for (SameGameAction action : SameGameAction.values()) {
            RadioButton rb = new RadioButton(action.getLabel()); // texte affiché
            rb.setUserData(action);                              // valeur enum stockée
            rb.setToggleGroup(group);
            radioBox.getChildren().add(rb);
        }

        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                SameGameAction selected = (SameGameAction ) newVal.getUserData();
                if (selected.equals(SameGameAction.VARIANT)){
                    variantBox.setVisible(true);
                }else{
                    variantBox.setVisible(false);
                }
            }
        });
        content.getChildren().addAll(radioBox, variantBox);
        VBox layout = new VBox(15,
                content,
                buttonBox
        );

        this.setOnCloseRequest(event -> {
         //   result.clear();
            try {
                Path directory = Paths.get(Configuration.tempFolder);
                HelperClass.cleanDirectory(directory);
            } catch (IOException ioe) {
                log.error("clean temp directory", ioe);
            }
            event.consume();
            close();
        });

        layout.setPadding(new Insets(20));

        layout.getStyleClass().add("sierra");
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        setScene(scene);

    }

//    public List<GameApp> showAndWaitForResult() {
//        showAndWait(); // bloque jusqu'à fermeture
//      //  return result;
//    }

   
}




