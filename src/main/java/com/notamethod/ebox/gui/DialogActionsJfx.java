package com.notamethod.ebox.gui;

import com.notamethod.ebox.gui.common.DialogActions;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class DialogActionsJfx implements DialogActions {
    @Override
    public Optional<String> showInputDialog(String s, String s1) {
        //TODO
        log.error("todo s s1");
        return Optional.empty();
    }

    @Override
    public Optional<String> showInputDialog(String s, String s1, String s2) {
        //TODO
        log.error("todo s2");
        return Optional.empty();
    }

    @Override
    public Optional<String> showListInputDialog(String title, String content, String[] options, String option) {

        ChoiceDialog<String> dialog = new ChoiceDialog<>(option, options);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText(content);

        return dialog.showAndWait();

    }

    @Override
    public boolean showConfirmDialog(String title, String s1) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
       // alert.setContentText(s1);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
           return true;
        } else {
            return false;
        }


    }

    @Override
    public void showMessageDialog(String s, String s1) {
        //TODO
        log.error("todo");

    }
}
