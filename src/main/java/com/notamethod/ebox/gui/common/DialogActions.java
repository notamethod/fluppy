package com.notamethod.ebox.gui.common;

import java.util.List;
import java.util.Optional;

public interface DialogActions {
    Optional<String> showInputDialog(String title, String content);

    Optional<String> showInputDialog(String title, String content, String value);

    Optional<String> showListInputDialog(String title, String content, String[] choices, String choice);

    boolean showConfirmDialog(String title, String content);

    void showMessageDialog(String s, String s1);

    void showErrorDialog(List<String> content);
}
