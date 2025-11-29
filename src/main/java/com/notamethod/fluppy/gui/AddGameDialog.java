package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.api.ApiCalls;
import com.notamethod.fluppy.api.ApiException;
import com.notamethod.fluppy.api.MappingException;
import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.api.igdb.Genre;
import com.notamethod.fluppy.core.*;
import com.notamethod.fluppy.gui.common.FileActions;
import com.notamethod.fluppy.util.ArchiveExtractor;
import com.notamethod.fluppy.util.HelperClass;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AddGameDialog extends Stage {

    private ApiCalls apiCalls;
    private final ObservableList<GameApp> result = FXCollections.observableArrayList();

    private FileActions fileActions;
    private boolean haErrors=false;
    public AddGameDialog(List<File> metaGamesFiles,  ApiCalls apiCalls) {

        this.apiCalls=apiCalls;
        initModality(Modality.APPLICATION_MODAL);
        fileActions = new FileActions();
        //initOwner(owner);
        setTitle("Add Game");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));



        List<ComboBox<GameApiBean>> foundBoxes=new ArrayList<>();
        List<ComboBox<File>> comboExeFilesList = new ArrayList<>();
        List <String> extractionErrors =new ArrayList<>();
        Map<String, List <String>> errorTypes = new HashMap<>();
        errorTypes.put("Error extracting the folowwing files:",extractionErrors);
        int row=0;
        for (File metaGameFile : metaGamesFiles){
            GameApp metaGame=null;
            try {
                 metaGame= processFile(metaGameFile);

            } catch (GameManagerException e) {
               log.error("archive extraction",e);
            } catch (OperationCanceledException e) {
                throw new RuntimeException(e);
            }
            if (metaGame==null){
                haErrors=true;
                extractionErrors.add(metaGameFile.getAbsolutePath());
                continue;
            }


            String searchString=metaGame.getSearchName();

            ComboBox<File> comboExeFiles = new ComboBox<>();
            comboExeFilesList.add(comboExeFiles);
            comboExeFiles.setConverter(getFileConverter());

            //comboBox.setEditable(true);
            if (metaGame.getExeFiles()==null && metaGame.getExePath()!=null){
                //
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
            Label labelExe = new Label(Messages.getString("dialog.select_executable.header"));
            labelExe.setMinWidth(112);
            Label labelSearch = new Label(Messages.getString("dialog.select_appname.text.short"));
            TextField nameSearch = new TextField(searchString);
            Button refreshButton = new Button("<>");

            ComboBox<GameApiBean> foundBox = new ComboBox<>();
            foundBox.setMaxWidth(400);
            foundBoxes.add(foundBox);
            refreshButton.setOnAction(e -> {
                foundBox.getItems().clear();
                foundBox.getItems().addAll(findGame(nameSearch.getText(), apiCalls));
                if (!foundBox.getItems().isEmpty()){
                    foundBox.setValue(foundBox.getItems().getFirst());
                }

            });

            grid.add(titleGame, 0, row++,6,1);
            grid.add(labelExe, 0, row);
            grid.add(comboExeFiles, 1, row);
            grid.add(labelSearch, 2, row);
            grid.add(nameSearch, 3, row);
            grid.add(refreshButton, 4, row);
            Label labelFound= new Label(Messages.getString("dialog.select_appname.text.short"));

            grid.add(labelFound, 5, row);
            grid.add(foundBox, 6, row);
            row++;
            //HBox cheminBox = new HBox(10, cheminField, browseButton);

            if (searchString!=null){
                foundBox.getItems().addAll(findGame(nameSearch.getText(), apiCalls));
                if (!foundBox.getItems().isEmpty()){
                    foundBox.setValue(foundBox.getItems().getFirst());
                }
            }
            foundBox.setCellFactory(lv -> new ListCell<GameApiBean>() {
                @Override
                protected void updateItem(GameApiBean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (" + item.getYear() + ")");
                    }
                }
            });
            result.add(metaGame);
        }

        Button saveButton = new Button("Enregistrer");
        saveButton.setDisable(true);
        Button cancelButton = new Button("Annuler");
        saveButton.disableProperty().bind(
                Bindings.isEmpty(result)
        );
        saveButton.setOnAction(e -> {
            updateGameList(foundBoxes, comboExeFilesList);
            close();
        });

        cancelButton.setOnAction(e -> {
            result.clear();
            try {
                Path directory = Paths.get(Configuration.tempFolder);
                HelperClass.cleanDirectory(directory);
            } catch (IOException ioe) {
                log.error("clean temp directory", ioe);
            }
            close();
        });
        Label labelError= new Label();
        if (haErrors) {
            for (String error : errorTypes.keySet()) {
                labelError.setText(error + String.join(", ", errorTypes.get(error)));
            }
        }
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().add(grid);
        VBox layout = new VBox(15,
                content,
                labelError,
                buttonBox
        );
        layout.setPadding(new Insets(20));
        Scene scene=new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        setScene(scene);

    }

    private void updateGameList(List<ComboBox<GameApiBean>> foundBoxes, List<ComboBox<File>> comboExeFilesList) {
        int i=0;
        for (GameApp metaGame:result){
            GameApiBean game=foundBoxes.get(i).getValue();
            try {
                if (game != null) {
                    metaGame.setName(game.getName());
                    //FIXME

                    if (game.getGenres() != null) {
                        for (Genre genre : game.getGenres()) {
                            GenreApp genraApp = new GenreApp();
                            genraApp.setId(genre.getSlug());
                            genraApp.setName(genre.getName());
                            metaGame.getGenres().add(genraApp);
                        }
                    }

                    //metaGame.getGenres().addAll(genres);
                    metaGame.setYear(game.getYear() == null ? 1970 : Integer.valueOf(game.getYear()));
                    if (game.getCover() != null) {
                        try {
                            String coverFilename = "cover_" + game.getName().replace(" ", "").toLowerCase() + game.getYear();
                            metaGame.setImagePath(Paths.get(apiCalls.getCover(Configuration.coverFolder, coverFilename, game.getCover(), 2)));
                        } catch (ApiException | MappingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    File exeFile=comboExeFilesList.get(i).getValue();
                    metaGame.setGameExe(exeFile.getName());
                    metaGame.setExePath(Paths.get(exeFile.getAbsolutePath().substring(0, exeFile.getAbsolutePath().lastIndexOf(File.separatorChar))));

                    //TODO: cover
                } else {
                    continue;
                }

//                if (!da.showConfirmDialog("null", "Adding game " + metaGame.getName() + " to the list ?")) {
//                    return Optional.empty();
//                }

              //  return Optional.of(metaGame);
            } catch (Exception e) {
             //   da.showMessageDialog("Something wrong happened. You have to add the application the hard way.", "Sorry...");
                log.error("error", e);
            }
           // return Optional.empty();


        }
    }

    private GameApp processFile(File inFile) throws GameManagerException, OperationCanceledException {

        GameApp metaGame = null;
        if (inFile.isDirectory()) {
            metaGame = fileActions.addDirectory(inFile);
        } else if (ArchiveExtractor.isArchive(inFile)) {
            metaGame = fileActions.addArchive(inFile);

        } else if (metaGame == null && (!inFile.getName().toLowerCase().endsWith("exe") && !inFile.getName().toLowerCase().endsWith("com") && !inFile.getName().toLowerCase().endsWith("bat") && !inFile.getName().toLowerCase().endsWith("pif"))) {
           //one file
        } else {
            metaGame = new GameApp();
        }
        calculateSearchString(metaGame, inFile.getName());
        return metaGame;
    }

    private String calculateSearchString(GameApp metaGame, String sourceFileName) {
        String guessSource = null;
        File exeFile = null;
        String searchString="";
        if (metaGame.getExePath() != null) {
            exeFile = metaGame.getExePath().toFile();
            guessSource = exeFile.getName();
            metaGame.setGameExe(exeFile.getName());
            metaGame.setExePath(Paths.get(exeFile.getAbsolutePath().substring(0, exeFile.getAbsolutePath().lastIndexOf(File.separatorChar))));
            searchString = exeFile.getParentFile().getAbsolutePath().substring(exeFile.getParentFile().getAbsolutePath().lastIndexOf(File.separator) + 1);
        }
        //TODO: set intallers


        if (sourceFileName != null) {
            String title = HelperClass.guessTitleFromFilename(sourceFileName);
            if (title != null) {

                searchString = title;
            }
        }
        String textSearch=HelperClass.fromCamelCase(searchString);
        metaGame.setSearchName(textSearch);
        return textSearch;
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

    private List<GameApiBean> findGame(String name, ApiCalls apiCalls) {
        // searching game
        String response = "";
        final List<GameApiBean> games = new ArrayList<>();
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

    public List<GameApp> showAndWaitForResult() {
        showAndWait(); // bloque jusqu'à fermeture
        return result;
    }

    private StringConverter<File> getFileConverter() {
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
        return converter;


    }
}




