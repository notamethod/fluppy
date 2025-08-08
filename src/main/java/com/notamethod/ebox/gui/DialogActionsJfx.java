package com.notamethod.ebox.gui;

import com.notamethod.ebox.gui.common.DialogActions;
import javafx.scene.control.ChoiceDialog;

import java.util.Optional;

public class DialogActionsJfx implements DialogActions {
    @Override
    public Optional<String> showInputDialog(String s, String s1) {
        return Optional.empty();
    }

    @Override
    public Optional<String> showInputDialog(String s, String s1, String s2) {
        return Optional.empty();
    }

    @Override
    public Optional<String> showListInputDialog(String title, String content, String[] options, String option) {

        ChoiceDialog<String> dialog = new ChoiceDialog<>(option, options);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        return result;
    }

    @Override
    public boolean showConfirmDialog(String s, String s1) {
        return false;
    }

    @Override
    public void showMessageDialog(String s, String s1) {

    }
}
