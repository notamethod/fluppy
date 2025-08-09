package com.notamethod.ebox.gui;

import com.notamethod.ebox.gui.common.DialogActions;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class DialogActionsJfx implements DialogActions {

    private static String TITLE="Old Dos Games Launcher";
    @Override
    public Optional<String> showInputDialog(String title, String content) {
        return showInputDialog(title, content, "");

    }

    @Override
    public Optional<String> showInputDialog(String title, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
        dialog.setTitle("Old Dos Games Launcher");
        dialog.setHeaderText(null);

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        Label label = new Label(content);
        TextField textField = dialog.getEditor();
        vbox.getChildren().addAll(label, textField);
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(vbox);

        return dialog.showAndWait();
    }

    @Override
    public Optional<String> showListInputDialog(String title, String content, String[] options, String option) {

        ChoiceDialog<String> dialog = new ChoiceDialog<>(option, options);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
        dialog.setTitle(TITLE);
        dialog.setHeaderText(null);

        // Récupérer le ComboBox interne
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(options);
        comboBox.setValue(option);
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        Label label = new Label(content);

        vbox.getChildren().addAll(label, comboBox);
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(vbox);
        //dialog.setContentText(content);
        // Gérer la sélection
        dialog.setResultConverter(button -> {
            if (button == dialog.getDialogPane().getButtonTypes().get(0)) {
                return comboBox.getValue();
            }
            return null;
        });
        return dialog.showAndWait();

    }

    @Override
    public boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
      dialogPane.getStyleClass().add("custom-dialog");

        alert.setTitle(TITLE);
        alert.setHeaderText(null);
       // alert.setHeaderText(title);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
           return true;
        } else {
            return false;
        }


    }

    @Override
    public void showMessageDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
        alert.setTitle(title);
        alert.getDialogPane().setPrefSize(500, 220);

        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void showErrorDialog(List<String> content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-dialog");
        alert.setTitle(TITLE);
        alert.getDialogPane().setPrefSize(500, 220);

        alert.setHeaderText("multiple errors on import");
        alert.setContentText(String.join("\n", content));
        alert.showAndWait();
    }
}
