package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.game.*;
import com.notamethod.fluppy.gui.common.DialogActionsJfx;
import com.notamethod.fluppy.platform.dosbox.EmulatorException;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.Rating;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class GameDetailPanel extends StackPane {
    private final StackPane imageView;
    private final Label description;
    private final VBox editorBox;
    private final VBox platformBox;
    private final Label genre;
    private final Label editor;
    private final ImageView editorImage;
    private final ImageView platformImage;
    private final Label timePlayed;
    private final Label year;
    private final Label language;
    private final ImageView  languageFlag;
    private final Label name;
    private  final HBox buttonBox;
    private final Button launchButton;
    private final Button editButton;
    private final Button deleteButton;
    private final VBox infoContent;
    private final GameManager gameManager;
    private GameApp game;
    private PanelListener listener;
    private final VBox extraFiles;
    private final Rating rating;
    private final VBox fullContent;
    private String  screenRez;
    VBox detailContent;
    private boolean isBigView=false;
    private final DialogActionsJfx da = new DialogActionsJfx();
    private final String THEME = "fluppy-theme";
    public GameDetailPanel(GameManager gameManager) {

        this.gameManager = gameManager;
        setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-padding: 10; -fx-background-radius: 8;");
        setVisible(false);

        imageView = new StackPane();
        imageView.setOnMouseClicked(event -> {
            if (!isBigView) {
                toBigView();
            }


        });
        extraFiles = new VBox();
        description = new Label();
        description.setWrapText(true);
        description.setPrefWidth(540);
        description.setVisible(false);
        editor  = new Label();

        editorImage = new ImageView();
        editorImage.setFitHeight(150);
        editorImage.setFitWidth(200);
        editorImage.setPickOnBounds(true);
        editorImage.setPreserveRatio(true);

        editorBox = new VBox(10, editor, editorImage);
        platformImage = new ImageView();
        platformImage.setFitHeight(80);
        platformImage.setFitWidth(140);
        platformImage.setPickOnBounds(true);
        platformImage.setPreserveRatio(true);


        platformBox = new VBox(10, platformImage);
        editorBox.managedProperty().bind(editorBox.visibleProperty());
        genre = new Label();
        genre.setWrapText(true);

        name = new Label();
        timePlayed = new Label();
        year = new Label();
        language=new Label();
        language.setVisible(false);
        languageFlag = new ImageView();
        languageFlag.setFitWidth(100);
        name.setStyle("-fx-text-fill: white; -fx-wrap-text: true;");
        name.getStyleClass().add("game-title");
        name.setWrapText(true);

        launchButton = createRButton("play.png", THEME);

        editButton = createRButton("edit.png", THEME);
        editButton.setOnMouseClicked(event -> {
            try {
                editAction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        deleteButton = createRButton("trashbin.png", THEME);
        deleteButton.setOnMouseClicked(event -> {
            try {
                deleteAction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        rating = new Rating();
        rating.setMax(5); // 5 étoiles
        rating.setPartialRating(true);

        rating.setRating(0); // valeur initiale
        rating.setScaleX(0.6);
        rating.setScaleY(0.6);
        rating.setPadding(new Insets(10));
        HBox ratbox = new HBox(rating);
        ratbox.setPadding(new Insets(10));
        detailContent = new VBox(10, year, genre, languageFlag, platformBox, editorBox, timePlayed, extraFiles);
        detailContent.setAlignment(Pos.TOP_LEFT);
        launchButton.setDisable(game == null ? false : !gameManager.isLauncherPresent(game.getPlatform()));
        buttonBox = new HBox(2, launchButton, editButton, deleteButton);
        buttonBox.setPadding(new Insets(40, 0, 0, 0)); // top, right, bottom, left
        infoContent = new VBox(detailContent);
        infoContent.getChildren().add(buttonBox);
        HBox compactContent = new HBox(10, imageView, infoContent);
        setMaxSize(550, 200);
        launchButton.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                long returne = 0;
                log.debug(game.toString());
                try {
                    long duration = runGame();
                    if (duration > 0) {
                        gameManager.updateTime(game, returne);
                    }
                    //  returne = dosBoxManager.runApplication(game.getGameExe(), game, listener);
                } catch (EmulatorException ex) {
                    throw new RuntimeException(ex);
                }
                //   gameManager.updateTime(game, returne);
                if (listener != null) {
                    listener.onClose();
                    //   listener.onExitGame();
                }
            }
        });

        fullContent = new VBox(10, name,compactContent, description);
        getChildren().add(fullContent);

    }

    private void toBigView() {
        setMaxHeight(500);
        CompanyEntity companyEntity=gameManager.loadCompany(game);
        if (companyEntity!=null) {

            editor.setText(Messages.getString("game.company",companyEntity.getName() == null ? "" : companyEntity.getName()));
            editorImage.setFitHeight(150);
            this.editorImage.setImage(ImageUtils.buildImageFromBytes(companyEntity.getImage()));
        }
        editorBox.setVisible(true);
        deleteButton.setVisible(true);
        description.setVisible(true);
        fullContent.getChildren().add(buttonBox);
        isBigView=true;
    }
    private void toCompactView(){
        setMaxSize(550, 200);
        if (buttonBox.getParent() == fullContent){
            infoContent.getChildren().add(buttonBox);
        }
        deleteButton.setVisible(false);
        editorBox.setVisible(false);
        description.setVisible(false);
        isBigView=false;
    }

    private long runGame() throws EmulatorException {

        return gameManager.runGame(
                game,
                screenRez,
                listener);
    }

    private void editAction() throws IOException {

        GameEditorView view = new GameEditorView();
        view.setGameManager(gameManager);
        view.setGame(game);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setDialogPane(view);
        dialog.showAndWait();

        GameApp editedGame = view.getResult();
        if (editedGame != null && listener != null) {
            listener.onUpdate();
        }

    }

    private void deleteAction() throws IOException {

        boolean doAction = true;

        doAction = da.showConfirmDialog(Messages.getString("confirmation.delete.header"),
                Messages.getString("confirmation.delete.game", game.getName()));

        if (!doAction)
            return;

        if (gameManager.deleteGame(game) > 0) {
            if (listener != null) {
                listener.onUpdate();
            }
        }


    }


    public void show(GameApp game, double x, double y, String screenRez) {
        this.game = game;
        this.screenRez=screenRez;
        imageView.getChildren().clear();
        imageView.getChildren().add(ImageFactory.getMedium(game));

        description.setText("dklsdjgkl jsdgksdjgklmsdj gkjsdlgjsdgkj ksdgjksdjglds sdkjgklsdj kgdsjgklsd jsdl jkgjsgklsdg j" +
                "dksjhgklsdhg jdskghdshgjsdhg hgs");
        name.setText(game.getName());
        platformImage.setImage(getPlatformImage(game.getPlatform()));

        year.setText(Messages.getString("game.year",game.getYear() == null ? "" : String.valueOf(game.getYear())));
        String genres = String.join(" ■ ",
                game.getGenres().stream().map(GenreApp::getName).toArray(String[]::new)
        );
        BooleanBinding hasText = Bindings.createBooleanBinding(
                () -> language.getText() != null && !language.getText().isEmpty(),
                language.textProperty()
        );

        languageFlag.visibleProperty().bind(hasText);
        languageFlag.managedProperty().bind(languageFlag.visibleProperty());
        language.setText(game.getLanguage() == null ? "" : game.getLanguage());


        genre.setText(Messages.getString("game.genre",genres));
        timePlayed.setText(Statistics.getTimePlayed(game.getTimePlayed(), false));
        extraFiles.getChildren().clear();
        if (game.getManualPath() != null) {
            extraFiles.getChildren().add(LinkLabelFactory.createFileLink("Manual", game.getManualPath().toFile()));
        }
        if (game.getProtectionPath() != null) {
            extraFiles.getChildren().add(LinkLabelFactory.createFileLink("protection", game.getProtectionPath().toFile()));
        }
        rating.setRating(2.5); // valeur initiale
        setTranslateX(x);
        setTranslateY(y);

        setScaleX(0.8);
        setScaleY(0.8);
        setOpacity(0);
        setVisible(true);

        ScaleTransition scale = new ScaleTransition(Duration.millis(250), this);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fade = new FadeTransition(Duration.millis(250), this);
        fade.setToValue(1.0);

        new ParallelTransition(scale, fade).play();

    }

    //TODO
    private Image getPlatformImage(Platform platform) {
        if (Platform.AMIGA.equals(platform)) {
            return new Image(getClass().getResourceAsStream("/images/Amiga-Logo-1985.png"));

        } else {
            return new Image(getClass().getResourceAsStream("/images/pcgame.png"));

        }
    }

    private Button createRButton(String image, String theme) {
        String imagePath = "/images/" + theme + "/" + image;
        try (InputStream is = getClass().getResourceAsStream(imagePath)) {
            if (is == null) {
                throw new IOException(imagePath + " not found");
            }
            Image img = new Image(is, 32, 32, false, false);
            ImageView imgView = new ImageView(img);
            Button button = new Button();
            button.setGraphic(imgView);
            button.setStyle("-fx-background-color: transparent;");
            return button;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void hide() {
        toCompactView();
        setVisible(false);
    }


    public void setListener(PanelListener listener) {
        this.listener = listener;
    }

    public GameApp getCurrentGame() {
        return game;
    }

    public Image getCountryFlag(String countryCode){
       if (countryCode!=null && countryCode.length()==2){
           Image image = new Image(getClass().getResourceAsStream("/images/country-flag"+countryCode.toLowerCase()+".png"));
            return image;
        }
       return null;
    }
}



