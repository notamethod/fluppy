package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.util.Installer;
import javafx.stage.Stage;


public class Story {

    public enum StoryType {WELCOME}

    private Stage stage;
    private String model;
    private StoryDialog storyDialog;
    private PreferencesBean preferences;

    public Story(Stage stage, String model, PreferencesBean preferences) {
        this.stage = stage;
        this.model = model;
        this.preferences = preferences;
    }

    public void start() {
        storyDialog = new StoryDialog(stage);
        if (model.equals("firstrun")){
            launch(StoryType.WELCOME);
        }
    }

    private void launch(StoryType storyType) {

        switch (storyType) {

            case WELCOME:
                EventBus.subscribe("game-added", () -> {
                    storyDialog.hide();
                    storyDialog.setText(Messages.getString("story.dialog.happyend"));
                    storyDialog.activate(StoryButton.OK);
                    storyDialog.showAndWait();
                });
                int step=0;
                storyDialog.setText(Messages.getString("story.dialog.welcome"));
                storyDialog.activate(StoryButton.OK, StoryButton.NEXT);
                storyDialog.setTextButton(StoryButton.OK, "Passer");
                storyDialog.showAndWait();
                if (preferences.getDosBoxPath().isEmpty()) {
                    storyDialog.setText(Messages.getString("story.dialog.dosbox"));
                    storyDialog.activate(StoryButton.OK, StoryButton.ACTION);
                    storyDialog.setTextButton(StoryButton.ACTION, "automatique");
                    storyDialog.setOnAction(resultat -> {
                        Installer installer = new Installer();
                        String exeFile = installer.application(new ProgressCallback() {
                            @Override
                            public void onProgress(String message) {
                                // Update UI or log progress message
                                System.out.println("Progress: " + message);
                                storyDialog.setText(message);
                            }
                        }, Installer.emu1);
                        if (exeFile != null) {
                            preferences.setDosBoxPath(exeFile);
                            PreferencesIO.save(preferences);
                        }
                    });
                    step++;
                    storyDialog.showAndWait();
                }
                if (preferences.getFsuaePath().isEmpty()) {
                    storyDialog.setText(Messages.getString("story.dialog.fsuae"));
                    storyDialog.activate(StoryButton.OK, StoryButton.ACTION);
                    storyDialog.setTextButton(StoryButton.ACTION, "automatique");
                    storyDialog.setOnAction(resultat -> {
                        Installer installer = new Installer();
                        String exeFile = installer.application(new ProgressCallback() {
                            @Override
                            public void onProgress(String message) {
                                // Update UI or log progress message
                                System.out.println("Progress: " + message);
                                storyDialog.setText(message);
                            }
                        }, Installer.emu2);
                        if (exeFile != null) {
                            preferences.setFsuaePath(exeFile);
                            PreferencesIO.save(preferences);
                        }
                    });
                    step++;
                    storyDialog.showAndWait();
                }
                if (preferences.getGamesCount()==0){
                    storyDialog.setText(getAdverb(step)+Messages.getString("story.dialog.addgame"));
                    storyDialog.activate(StoryButton.OK, StoryButton.NEXT);
                    storyDialog.showAndWait("highlight-ribbon");
                }
                break;
            default:
                break;
        }

    }

    private String getAdverb(int step) {
        if (step>=1){
           return Messages.getString("story.dialog.advnext");
        }else{
            return Messages.getString("story.dialog.advstart");
        }
    }
}
