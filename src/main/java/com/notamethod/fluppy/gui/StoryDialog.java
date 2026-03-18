package com.notamethod.fluppy.gui;

import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.dosbox.DosBoxManager;
import com.notamethod.fluppy.dosbox.DosboxType;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

@Slf4j
public class StoryDialog extends Stage {

    private Button nextButton;
    private Button okButton;
    private Button actionButton;
    private Scene scene;
    private Consumer<String> onAction;
    StringProperty mainText = new SimpleStringProperty();
    String titleText;

    public StoryDialog(Stage owner) {
        initStyle(StageStyle.UNDECORATED);
        initOwner(owner);

        nextButton = new Button("Next");
        nextButton.setId(StoryButton.NEXT.toString());
        okButton = new Button("OK");
        okButton.setId(StoryButton.OK.toString());
        actionButton = new Button();
        actionButton.setId(StoryButton.ACTION.toString());
        nextButton.setVisible(false);
        okButton.setVisible(false);
        actionButton.setVisible(false);
        actionButton.setOnAction(e -> {
            if (onAction != null) onAction.accept("action1");
        });
        nextButton.setOnAction(e -> {
            close();
        });
        okButton.setOnAction(e -> {
            close();
        });
        VBox textBox = new VBox(10);
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.textProperty().bind(mainText);
        textBox.getChildren().addAll(label);
        HBox buttonBox = new HBox(10, okButton, nextButton, actionButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Label title = new Label(titleText);
        title.getStyleClass().add("title");
        VBox layout = new VBox(15
        );
        if (titleText != null) {
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
        VBox.setVgrow(title, Priority.ALWAYS);
        StackPane root = new StackPane(layout);
        root.getStyleClass().add("sierra");
        layout.setPrefWidth(400);

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        setScene(scene);
        sizeToScene();

    }


    public void setText(String text) {
        mainText.set(text);
    }

    public void activate(StoryButton... buttons) {
        nextButton.setVisible(false);
        okButton.setVisible(false);
        actionButton.setVisible(false);
        for (StoryButton button : buttons) {
            Button btn = (Button) scene.lookup("#"+button.toString());
            btn.setVisible(true);
        }

    }

    public void setOnAction(Consumer<String> callback) {
        this.onAction = callback;
    }

    public void setTextButton(StoryButton storyButton, String label) {
        Button btn = (Button) scene.lookup("#"+storyButton.toString());
        btn.setText(label);
    }

    public void showAndWait(String event) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> EventBus.publish(event));
        pause.play();
        super.showAndWait();

    }
}

