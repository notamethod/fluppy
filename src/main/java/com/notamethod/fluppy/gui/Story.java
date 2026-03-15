package com.notamethod.fluppy.gui;

import javafx.stage.Stage;


public class Story {

    private Stage stage;
    private String model;
    public Story(Stage stage, String model) {
        this.stage=stage;
        this.model=model;
    }

    public void start() {

        StoryDialog storyDialog = new StoryDialog(stage);
        storyDialog.setText(Messages.getString("story.dialog1"));
        storyDialog.showAndWait();
        storyDialog.setText(Messages.getString("story.dialog2"));
        storyDialog.showAndWait();
    }
}
