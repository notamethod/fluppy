package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.*;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class GameDetailPanel extends StackPane {
    private final StackPane imageView;
    private final Label descriptionLabel;
    private final Label genre;
    private final Label timePlayed;
    private final Label name;
    private final Button launchButton;
    private final Button editButton;
    private final DosBoxManager dosBoxManager;
    private final GameManager gameManager;
    private GameApp game;
    private PanelListener listener;

    public GameDetailPanel(DosBoxManager dosBoxManager, GameManager gameManager) {
        this.dosBoxManager=dosBoxManager;
        this.gameManager=gameManager;
        setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-padding: 10; -fx-background-radius: 8;");
        setVisible(false);

        imageView = new StackPane();


        descriptionLabel = new Label();
        genre = new Label();
        name = new Label();
        timePlayed = new Label();

        descriptionLabel.setStyle("-fx-text-fill: white; -fx-wrap-text: true;");
        name.getStyleClass().add("game-title");
        name.setWrapText(true);
        launchButton = new Button("Lancer");
        editButton = new Button("Éditer");
        editButton.setOnMouseClicked(event -> editAction());
        VBox leftContent = new VBox(10, name, genre, timePlayed, new VBox(5, launchButton, editButton));
        HBox content = new HBox(10, imageView, leftContent);
        setMaxSize(550, 200);
        launchButton.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                long returne = 0;
                log.debug(game.toString());
                try {
                    returne = dosBoxManager.runApplication(game.getGameExe(), game);
                } catch (DosBoxException ex) {
                    throw new RuntimeException(ex);
                }
                gameManager.updateTime(game, returne);
                if (listener != null) listener.onClose();
            }
        });
        getChildren().add(content);
    }

    private void editAction() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GameEditor.fxml"));
                DialogPane dialogPane = loader.load();
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Éditer un jeu");

                GameEditorController controller = loader.getController();
                controller.setGameManager(gameManager);
                controller.setGame(game);

                dialog.showAndWait();
                GameApp editedGame = controller.getResult();
                if (editedGame!=null){
                    if (listener != null) listener.onUpdate();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

    }


    public void show(GameApp game, double x, double y) {
        this.game=game;
        imageView.getChildren().clear();
        imageView.getChildren().add(ImageFactory.getMedium(game));
        descriptionLabel.setText(game.getName());
        name.setText(game.getName());
        String genres = String.join(" ■ ",
                game.getGenres().stream().map(GenreApp::getName).toArray(String[]::new)
        );
        genre.setText(genres);
        long played=game.getTimePlayed()/60;
        if (played>60) {
            timePlayed.setText(Messages.getString("game.timeplayed.hour",String.valueOf(played / 60)));
        }
            else if (played>1) {
            timePlayed.setText(Messages.getString("game.timeplayed.min",String.valueOf(played)));

        }else{
            timePlayed.setText(Messages.getString("game.neverplayed"));
        }
      setTranslateX(x);
     setTranslateY(y);

//        setTranslateY(y);
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

    public void hide() {
        setVisible(false);
    }


    public void setListener(PanelListener listener) {
        this.listener = listener;
    }
}



