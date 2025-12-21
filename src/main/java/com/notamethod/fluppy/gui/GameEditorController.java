package com.notamethod.fluppy.gui;



import com.notamethod.fluppy.api.ApiCalls;
import com.notamethod.fluppy.api.ApiException;
import com.notamethod.fluppy.api.MappingException;
import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.core.GameApp;

import com.notamethod.fluppy.core.GameManager;
import com.notamethod.fluppy.gui.common.GameActions;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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
    @FXML private CheckBox nsfwField;
    @FXML private CheckBox favoriteField;
    @FXML private ComboBox<String> comboMachines;
    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;
    private GameApp originalGame;
    private GameManager gameManager;
    private GameApp result;

    @FXML
    private void handleOk(ActionEvent event) {
        System.out.println("OK cliqué");
        saveGame();
        closeDialogWithResult(true, event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        System.out.println("Annuler cliqué");
        result=null;
        closeDialogWithResult(false, event);
    }
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

    private void closeDialogWithResult(boolean ok, ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public GameApp getResult() {

        return result;
    }

    public void saveGame(){
        GameApp editedGame = new GameApp();
        editedGame.setName(titleField.getText());
        //game.setGenre(genreField.getText());
        //game.setPlatform(platformField.getText());
        editedGame.setYear( Integer.parseInt(yearField.getText()));
        editedGame.setExePath(this.exePath);
        editedGame.setGameExe(exeFile.getText());
        //game.setGenre(genreField.getText());
        editedGame.setCycles((int) cyclesSpinner.getValue());
        editedGame.setMachine(comboMachines.getValue());
        String imagePath=coverPath.getText();
        editedGame.setAgeRating(nsfwField.isSelected()?1:0);
        if (imagePath.isEmpty()){
            imagePath=null;
        }
        editedGame.setImagePath(imagePath==null?null:Path.of(imagePath));


        editedGame.setId(originalGame.getId());
        editedGame.merge(originalGame);
        if (!editedGame.equals(originalGame)) {
            gameManager.save(editedGame);
            result=editedGame;
        }else{
            result=null;
        }

    }


    public void setGame(GameApp game){
        this.originalGame = game;
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
        nsfwField.setSelected(game.getAgeRating()>0);
        game.setAgeRating(nsfwField.isSelected()?1:0);
        cyclesSpinner.setValue(game.getCycles());
        String genres = game.getGenres().stream().map(x->x.getName()).collect(Collectors.joining(","));
        genreField.setText(genres);
        exeFile.setText(game.getGameExe());
        comboMachines.setValue(game.getMachine());

    }

    public boolean updateAPI(){
        boolean updated=false;
        System.out.println("updateAPI()");
        ApiCalls apiCalls = new ApiCalls();
        GameActions gameActions = new GameActions(new DialogActionsJfx());
        List<GameApiBean> games = null;
        try {
            games = apiCalls.findGame(titleField.getText());
            updated=true;
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

        GameApiBean game = games.size() > 1 ? gameActions.chooseGame(games) : games.get(0);
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

    public void setGameManager(GameManager gameManager) {
        this.gameManager=gameManager;
    }
}

