package com.notamethod.fluppy.gui;


import com.notamethod.fluppy.api.ApiCalls;
import com.notamethod.fluppy.api.ApiException;
import com.notamethod.fluppy.api.MappingException;
import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;

import com.notamethod.fluppy.core.game.GameManager;
import com.notamethod.fluppy.core.game.GameMapper;
import com.notamethod.fluppy.gui.common.DialogActionsJfx;
import com.notamethod.fluppy.gui.common.GameActions;
import com.notamethod.fluppy.util.HelperClass;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GameEditorController {
    public enum EXTRA_TYPE {
        PROTECTION, MANUAL;
    }


    @FXML
    private TextField titleField;


    @FXML
    private TextField genreField;
    @FXML
    private TextField platformField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField ratingField;
    @FXML
    private ImageView imageGame;
    @FXML
    private Label cyclesLabel;
    @FXML
    private Slider cyclesSpinner;
    @FXML
    private TextField exeFile;
    @FXML
    private TextField coverPath;
    @FXML
    private CheckBox nsfwField;
    @FXML
    private CheckBox favoriteField;
    @FXML
    private ComboBox<String> comboMachines;
    @FXML
    private Button okButton;
    @FXML
    private TextField protectionPathField;
    @FXML
    private TextField manualPathField;
    @FXML
    private TextArea commentField;
    private DialogActionsJfx da;
    @FXML
    private VBox dropZone;
    @FXML
    private Button cancelButton;
    private GameApp originalGame;
    private GameManager gameManager;
    private GameApp result;

    @FXML
    private void handleOk(ActionEvent event) {
        saveGame();
        closeDialogWithResult(true, event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        result = null;
        closeDialogWithResult(false, event);
    }

    private Path exePath;
    private Path manualPath;
    private Path protectionPath;

    @FXML
    public void initialize() {
        da = new DialogActionsJfx();
        cyclesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(0.0)) {
                cyclesLabel.setText("Valeur : auto");
            } else {
                cyclesLabel.setText("Valeur : " + newVal.intValue());
            }
        });

        comboMachines.setItems(FXCollections.observableArrayList(
                "hercules", "cga", "ega", "pcjr", "tandy", "svga_s3"

        ));

        comboMachines.setValue("svga_s3"); // Valeur sélectionnée par défaut


        dropZone.setPrefSize(300, 150);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setStyle("""
                    -fx-border-color: #888;
                    -fx-border-width: 2;
                    -fx-border-style: dashed;
                    -fx-background-color: rgba(255,255,255,0.05);
                """);

        Label label = new Label("Déposez un fichier ici");
        dropZone.getChildren().add(label);

// Quand un fichier entre dans la zone
        dropZone.setOnDragOver(event -> {

            if (event.getGestureSource() != dropZone &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

// Quand le fichier est déposé
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                log.debug("Dropped file : " + file.getAbsolutePath());
                label.setText("Fichier : " + file.getName());
                try {
                    Path p = addExtra(file, originalGame.getName());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
        dropZone.setOnDragEntered(event -> {
            dropZone.setStyle("""
                        -fx-border-color: #4CAF50;
                        -fx-border-width: 2;
                        -fx-border-style: solid;
                        -fx-background-color: rgba(76,175,80,0.1);
                    """);
        });

        dropZone.setOnDragExited(event -> {
            dropZone.setStyle("""
                        -fx-border-color: #888;
                        -fx-border-width: 2;
                        -fx-border-style: dashed;
                        -fx-background-color: rgba(255,255,255,0.05);
                    """);
        });
    }

    private Path addExtra(File file, String name) throws IOException {
        Map<String, GameApiBean> map = new HashMap<>();
        List<String> choices = new ArrayList<>();
        choices.add(EXTRA_TYPE.PROTECTION.toString());
        choices.add(EXTRA_TYPE.MANUAL.toString());

        choices.add("Something else...");
        String[] choiceArray = choices.toArray(String[]::new);
        Optional<String> chosen = da.showListInputDialog(
                "What's the name of the game?",
                "What is the title of the application? Select one of the proposals,\n" +
                        "or select \"Something else...\" to type your own.",
                choiceArray, choiceArray[0]);
        if (!chosen.isPresent()) {
            return null;
        }
        String choice = chosen.get();
        String extraFilename = "extra_" + HelperClass.sanitizeName(name) + "_" + choice +"."+ HelperClass.getExtension(file);

        Path outputPath = Paths.get(Configuration.extraFolder);
        Files.createDirectories(outputPath);
        Path target = outputPath.resolve(extraFilename);
        Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        if (choice.equals(EXTRA_TYPE.PROTECTION.toString())) {
            protectionPath=target;
            protectionPathField.setText(target.toFile().getCanonicalPath());
        } else {
            manualPath=target;
            manualPathField.setText(target.toFile().getCanonicalPath());
        }
        return target;


    }

    private void closeDialogWithResult(boolean ok, ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public GameApp getResult() {

        return result;
    }

    public void saveGame() {
        GameApp editedGame = GameMapper.INSTANCE.copyGameApp(originalGame);
        editedGame.setName(titleField.getText());

        //game.setPlatform(platformField.getText());
        editedGame.setYear(Integer.parseInt(yearField.getText()));
        editedGame.setExePath(this.exePath);
        editedGame.setGameExe(exeFile.getText());

        editedGame.setCycles((int) cyclesSpinner.getValue());
        editedGame.setMachine(comboMachines.getValue());
        String imagePath = coverPath.getText();
        editedGame.setAgeRating(nsfwField.isSelected() ? 1 : 0);
        editedGame.setFavorite(favoriteField.isSelected());
        editedGame.setProtectionPath(this.protectionPath);
        editedGame.setManualPath(this.manualPath);
        if (imagePath.isEmpty()) {
            imagePath = null;
        }
        editedGame.setImagePath(imagePath == null ? null : Path.of(imagePath));
        editedGame.setComment(commentField.getText());
        editedGame.setId(originalGame.getId());
      //  editedGame.merge(originalGame);
        if (!editedGame.equals(originalGame)) {
            gameManager.save(editedGame);
            result = editedGame;
        } else {
            result = null;
        }

    }


    public void setGame(GameApp game) {
        this.originalGame = game;
        commentField.setWrapText(true);
        commentField.setText(game.getComment());
        titleField.setText(game.getName());
        yearField.setText(game.getYear() != null ? String.valueOf(game.getYear()) : "?");
        exePath = game.getExePath();
        ImageView imageView = new ImageView();
        if (game.getImagePath() != null) {
            try {
                // URL url = game.getImagePath().toUri();
                imageGame.setImage(new Image(game.getImagePath().toUri().toString()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            coverPath.setText(game.getImagePath().toString());
        }
        nsfwField.setSelected(game.getAgeRating() > 0);
        favoriteField.setSelected(game.isFavorite());
        game.setAgeRating(nsfwField.isSelected() ? 1 : 0);
        cyclesSpinner.setValue(game.getCycles());
        String genres = game.getGenres().stream().map(x -> x.getName()).collect(Collectors.joining(","));
        genreField.setText(genres);
        exeFile.setText(game.getGameExe());
        comboMachines.setValue(game.getMachine());
        protectionPath=game.getProtectionPath();
        if (game.getProtectionPath()!=null)
            protectionPathField.setText(game.getProtectionPath().toFile().getAbsolutePath());
        if (game.getManualPath()!=null)
            manualPathField.setText(game.getManualPath().toFile().getAbsolutePath());

    }

    public boolean updateAPI() {
        boolean updated = false;
        ApiCalls apiCalls = new ApiCalls();
        GameActions gameActions = new GameActions(new DialogActionsJfx());
        List<GameApiBean> games = null;
        try {
            games = apiCalls.findGame(titleField.getText());
            updated = true;
        } catch (ApiException | MappingException e) {
            log.error("Internal Error", e);
            return updated;
        }

        if (games.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("warning");
            alert.setContentText("Cant't update: no game found");
            alert.showAndWait();
            return updated;
        }

        GameApiBean game = games.size() > 1 ? gameActions.chooseGame(games) : games.get(0);
        GameApp gameApp = new GameApp();
        if (game != null) {
            try {
                apiCalls.findAndUpdateData(gameApp, 2, game);
                updated = true;
                //FIXME
                coverPath.setText(gameApp.getImagePath() == null ? "" : gameApp.getImagePath().toString().toString());
            } catch (ApiException | MappingException | IOException e) {
                log.error("Internal Error", e);
            }

        } else {
            log.warn("game not found");
        }
        return updated;
    }

    public void selectExe() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.exe","*.bat","*.com"));
        fileChooser.setInitialDirectory(exePath.toFile());
        fileChooser.setTitle("Choisir un fichier");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null && selectedFile.isFile()) {
            exeFile.setText(selectedFile.getAbsoluteFile().getName());
            exePath = selectedFile.getAbsoluteFile().getParentFile().toPath();
        }
    }

    public void selectCover() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(coverPath.getText()).getParentFile());
        fileChooser.setTitle("Choisir un fichier");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null && selectedFile.isFile()) {
            coverPath.setText(selectedFile.getAbsolutePath());

        }
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
}


