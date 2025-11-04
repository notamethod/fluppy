package com.notamethod.ebox.gui;

import com.notamethod.ebox.api.ApiCalls;
import com.notamethod.ebox.api.ApiException;
import com.notamethod.ebox.api.MappingException;
import com.notamethod.ebox.api.igdb.Game;
import com.notamethod.ebox.core.GameApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AddGameDialog extends Stage {

    private   VBox content;
    private GameApp metaGame;
    private ApiCalls apiCalls;
    private Game result;
    public AddGameDialog(GameApp metaGame, String searchString, ApiCalls apiCalls) {
        this.metaGame=metaGame;
        this.apiCalls=apiCalls;
        initModality(Modality.APPLICATION_MODAL);
        //initOwner(owner);
        setTitle("Add Game");
        content = new VBox(10);
        content.setPadding(new Insets(10));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        ComboBox<File> comboExeFiles = new ComboBox<>();

        StringConverter<File> converter = new StringConverter<>() {
            @Override
            public String toString(File person) {
                return person != null ? person.getName() : "";
            }
            @Override
            public File fromString(String string) {
                return new File(string); // ou une recherche dans une liste existante
            }

        };
        comboExeFiles.setConverter(converter);
        //comboBox.setEditable(true);
        if (metaGame.getExeFiles()==null && metaGame.getExePath()!=null){
        }else{
            List<File> sorted=sortRunners(searchString, metaGame.getExeFiles());
            comboExeFiles.getItems().addAll(sorted);
            comboExeFiles.setValue(sorted.getFirst());
        }
        comboExeFiles.setCellFactory(lv -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() );
                }
            }
        });


        Label titleGame = new Label(searchString);

//                    Messages.getString("dialog.select_executable.content", mgame.getGamePath()),
        Label label = new Label(Messages.getString("dialog.select_executable.header"));
        label.setMinWidth(112);
//                    HBox row = new HBox(10, label, comboBox); // 10 = espacement horizontal
//                    row.setAlignment(Pos.CENTER_LEFT);
        //comboBox.setTooltip(new Tooltip(p.getComments().toString()));
        Label labelSearch = new Label(Messages.getString("dialog.select_appname.text.short"));
        TextField nameSearch = new TextField(searchString);
        Button refreshButton = new Button("<>");

        ComboBox<Game> foundBox = new ComboBox<>();
        refreshButton.setOnAction(e -> {
            foundBox.getItems().clear();
            foundBox.getItems().addAll(findGame(nameSearch.getText(), apiCalls));
            if (!foundBox.getItems().isEmpty()){
                foundBox.setValue(foundBox.getItems().getFirst());
            }

        });

        grid.add(titleGame, 0, 0,2,1);
        grid.add(label, 0, 1);
        grid.add(comboExeFiles, 1, 1);
        grid.add(labelSearch, 2, 1);
        grid.add(nameSearch, 3, 1);
        grid.add(refreshButton, 4, 1);
        Label labelFound= new Label(Messages.getString("dialog.select_appname.text.short"));

        grid.add(labelFound, 5, 1);
        grid.add(foundBox, 6, 1);

        //HBox cheminBox = new HBox(10, cheminField, browseButton);

        if (searchString!=null){
            foundBox.getItems().addAll(findGame(nameSearch.getText(), apiCalls));
            if (!foundBox.getItems().isEmpty()){
                foundBox.setValue(foundBox.getItems().getFirst());
            }
        }
        foundBox.setCellFactory(lv -> new ListCell<Game>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getYear() + ")");
                }
            }
        });
        Button saveButton = new Button("Enregistrer");
        Button cancelButton = new Button("Annuler");

        saveButton.setOnAction(e -> {
            if (foundBox.getValue()!=null)
                result=foundBox.getValue();
            //TODO
            close();
        });

        cancelButton.setOnAction(e -> close());

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().add(grid);
        VBox layout = new VBox(15,
                content,
                buttonBox
        );
        layout.setPadding(new Insets(20));
        Scene scene=new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        setScene(scene);

    }

    private List<File> sortRunners(String mainName, List<File> exeFiles) {
        File mainFile = null;
        List<File> batFiles = new ArrayList<>();
        List<File> comFiles = new ArrayList<>();
        List<File> others = new ArrayList<>();

        for (File f : exeFiles) {
            String name = f.getName().toLowerCase();
            if (mainFile == null && name.toLowerCase().contains(mainName.toLowerCase())) {
                mainFile = f;
            } else if (name.endsWith(".bat")) {
                batFiles.add(f);
            } else if (name.endsWith(".com")) {
                comFiles.add(f);
            } else {
                others.add(f);
            }
        }

        List<File> resultat = new ArrayList<>();
        if (mainFile != null) resultat.add(mainFile);
        resultat.addAll(batFiles);
        resultat.addAll(comFiles);
        resultat.addAll(others);
        return resultat;
    }

    private List<Game> findGame(String name, ApiCalls apiCalls) {
        // searching game
        String response = "";
        final List<Game> games = new ArrayList<>();
        try {
            games.addAll(apiCalls.findGame(name));
        } catch (ApiException | MappingException e) {
            log.error("Internal Error", e);
        }
        if (!games.isEmpty()) {
            return games;
        }
        //no games found

//
        return games;
    }

    public Game showAndWaitForResult() {
        showAndWait(); // bloque jusqu'à fermeture
        return result;
    }
}


