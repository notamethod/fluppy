package com.notamethod.fluppy.gui.common;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class RetroAlert extends Alert {
    public RetroAlert(AlertType type) {
        super(type);
        getDialogPane().setGraphic(null);
        getDialogPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.initStyle(StageStyle.UNDECORATED);
            }
        });
    }
}
