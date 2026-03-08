package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameManager;
import com.notamethod.fluppy.core.game.GenreApp;
import com.notamethod.fluppy.dosbox.DosBoxException;
import com.notamethod.fluppy.dosbox.DosBoxManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class GameDetailPanel extends StackPane {
    private final StackPane imageView;
    private final Label descriptionLabel;
    private final Label genre;
    private final Label timePlayed;
    private final Label year;
    private final Label name;
    private final Button launchButton;
    private final Button editButton;
    private final DosBoxManager dosBoxManager;
    private final GameManager gameManager;
    private GameApp game;
    private PanelListener listener;
    private final VBox extraFiles;
    private final Rating rating;
    private String  screenRez;
    VBox detailContent;

    public GameDetailPanel(DosBoxManager dosBoxManager, GameManager gameManager) {
        this.dosBoxManager = dosBoxManager;
        this.gameManager = gameManager;
        setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-padding: 10; -fx-background-radius: 8;");
        setVisible(false);

        imageView = new StackPane();

        extraFiles = new VBox();
        descriptionLabel = new Label();
        genre = new Label();
        name = new Label();
        timePlayed = new Label();
        year = new Label();
        descriptionLabel.setStyle("-fx-text-fill: white; -fx-wrap-text: true;");
        name.getStyleClass().add("game-title");
        name.setWrapText(true);

        launchButton = createRButton("/images/play1.png");

        editButton = createRButton("/images/edit1.png");
        editButton.setOnMouseClicked(event -> {
            try {
                editAction();
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

        rating.ratingProperty().addListener((obs, oldV, newV) -> {
            log.debug("Note modifiée : " + newV);
        });
        HBox ratbox = new HBox(rating);
        ratbox.setPadding(new Insets(10));
        detailContent = new VBox(10, name, year, genre, timePlayed, extraFiles);

        launchButton.setDisable(!dosBoxManager.isDosboxPresent());
        HBox buttonBox = new HBox(2, launchButton, editButton);
        buttonBox.setPadding(new Insets(40, 0, 0, 0)); // top, right, bottom, left
        VBox infoContent = new VBox(detailContent, buttonBox);

        HBox content = new HBox(10, imageView, infoContent);
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
                } catch (DosBoxException ex) {
                    throw new RuntimeException(ex);
                }
                //   gameManager.updateTime(game, returne);
                if (listener != null) {
                    listener.onClose();
                    //   listener.onExitGame();
                }
            }
        });
        getChildren().add(content);
    }

    private long runGame() throws DosBoxException {


        AtomicReference<Long> duration = new AtomicReference<>(0L);
        dosBoxManager.runApplication(
                game.getGameExe(),
                game,
                screenRez,
                listener,
                line -> log.info("[DOSBOX] " + line),
                err -> log.error("[DOSBOX] " + err),
                result -> {

                    if (listener != null) {

                        listener.onExitGame();
                    }
                    if (result.success) {
                        System.out.println("DOSBox OK");
                    } else {
                        System.out.println("Erreur : " + result.error);
                    }

                    System.out.println("Durée : " + result.durationMillis + " ms");
                    duration.set(result.durationMillis);
                    if (duration.get() > 0) {
                        gameManager.updateTime(game, duration.get() / 1000);
                    }
                    System.out.println("Exit code : " + result.exitCode);
                }
        );
        return duration.get();

    }

    private void editAction() throws IOException {

        GameEditorView view = new GameEditorView();
        view.setGameManager(gameManager);
        view.setGame(game);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setDialogPane(view);
        dialog.showAndWait();

        GameApp result = view.getResult();
        GameApp editedGame = view.getResult();
        if (editedGame != null && listener != null) {
            listener.onUpdate();

        }

    }


    public void show(GameApp game, double x, double y, String screenRez) {
        this.game = game;
        this.screenRez=screenRez;
        imageView.getChildren().clear();
        imageView.getChildren().add(ImageFactory.getMedium(game));
        descriptionLabel.setText(game.getName());
        name.setText(game.getName());
        year.setText(game.getYear() == null ? "" : String.valueOf(game.getYear()));
        String genres = String.join(" ■ ",
                game.getGenres().stream().map(GenreApp::getName).toArray(String[]::new)
        );
        genre.setText(genres);
        long played = game.getTimePlayed() / 60;
        if (played > 60) {
            timePlayed.setText(Messages.getString("game.timeplayed.hour", String.valueOf(played / 60)));
        } else if (played > 1) {
            timePlayed.setText(Messages.getString("game.timeplayed.min", String.valueOf(played)));

        } else {
            timePlayed.setText(Messages.getString("game.neverplayed"));
        }
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

    private Button createRButton(String imagePath) {
        Image img = new Image(getClass().getResourceAsStream(imagePath), 32, 32, false, false);
        ImageView imgView = new ImageView(img);
        Button button = new Button();
        button.setGraphic(imgView);
        button.setStyle("-fx-background-color: transparent;");
        return button;
    }
    public void hide() {
        setVisible(false);
    }


    public void setListener(PanelListener listener) {
        this.listener = listener;
    }

    public GameApp getCurrentGame() {
        return game;
    }
}



