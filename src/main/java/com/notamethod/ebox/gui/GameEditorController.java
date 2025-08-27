package com.notamethod.ebox.gui;



import com.notamethod.ebox.api.ApiCalls;
import com.notamethod.ebox.api.ApiException;
import com.notamethod.ebox.api.MappingException;
import com.notamethod.ebox.api.igdb.Game;
import com.notamethod.ebox.core.GameApp;

import com.notamethod.ebox.core.Machine;
import com.notamethod.ebox.gui.common.GameActions;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class GameEditorController {

    @FXML private TextField titleField;



    @FXML private TextField genreField;
    @FXML private TextField platformField;
    @FXML private TextField yearField;
    @FXML private TextField ratingField;
    @FXML private ImageView imageGame;
    @FXML private Label cyclesLabel;
    @FXML private Slider cyclesSpinner;
    @FXML private TextField exeFile;
    @FXML private TextField coverPath;
    @FXML private ComboBox<String> comboMachines;

    private Path exePath;
    @FXML
    public void initialize() {
        cyclesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(0.0)){
                cyclesLabel.setText("Valeur : auto");
            }else {
                cyclesLabel.setText("Valeur : " + newVal.intValue());
            }
        });

        comboMachines.setItems(FXCollections.observableArrayList(
                "hercules", "cga","ega","pcjr", "tandy", "svga_s3"

        ));

        comboMachines.setValue("svga_s3"); // Valeur sélectionnée par défaut
    }

    public GameApp getGame() {
        GameApp game = new GameApp();
        game.setName(titleField.getText());
        //game.setGenre(genreField.getText());
        //game.setPlatform(platformField.getText());
        game.setYear( Integer.parseInt(yearField.getText()));
        game.setExePath(this.exePath);
        game.setGameExe(exeFile.getText());
        game.setGenre(genreField.getText());
        game.setCycles((int) cyclesSpinner.getValue());
        game.setMachine(comboMachines.getValue());
        String imagePath=coverPath.getText();
        if (imagePath.isEmpty()){
            imagePath=null;
        }
        game.setImagePath(imagePath==null?null:Path.of(imagePath));
        //game.setRating(Double.parseDouble(ratingField.getText()));
        return game;
    }
    public void setGame(GameApp game){
        titleField.setText(game.getName());
        yearField.setText(game.getYear()!=null?String.valueOf(game.getYear()):"?");
        exePath=game.getExePath();
        ImageView imageView = new ImageView();
        if (game.getImagePath()!=null) {
            try {
                // URL url = game.getImagePath().toUri();
                imageGame.setImage(new Image(game.getImagePath().toUri().toString()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            coverPath.setText(game.getImagePath().toString());
        }
        cyclesSpinner.setValue(game.getCycles());
        genreField.setText(game.getGenre());
        exeFile.setText(game.getGameExe());
        comboMachines.setValue(game.getMachine());

    }

    public boolean updateAPI(){
        boolean updated=false;
        System.out.println("updateAPI()");
        ApiCalls apiCalls = new ApiCalls();
        GameActions gameActions = new GameActions(new DialogActionsJfx());
        List<Game> games = null;
        try {
            games = apiCalls.findGame(titleField.getText());
        } catch (ApiException | MappingException e) {
            log.error("Internal Error", e);
            return updated;
        }

        if (games.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("warning");
            alert.setContentText("Cant't update: no game found");
            alert.showAndWait();
            return updated;
        }

        Game game = games.size() > 1 ? gameActions.chooseGame(games) : games.get(0);
        GameApp gameApp = new GameApp();
        if (game != null) {
            try {
                apiCalls.findAndUpdateData(gameApp, 2, game);
                updated=true;
                //FIXME
                coverPath.setText(gameApp.getImagePath()==null?"":gameApp.getImagePath().toString().toString());
            } catch (ApiException | MappingException | IOException e) {
                log.error("Internal Error", e);
            }

        } else {
            System.out.println("game not found");
        }
        return updated;
    }

    public void selectExe() {
        System.out.println("updateAPI()");
        FileChooser fileChooser = new FileChooser();
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

}

