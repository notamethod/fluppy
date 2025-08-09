package com.notamethod.ebox.gui;


import com.notamethod.ebox.app.*;
import com.notamethod.ebox.gui.common.GameActions;
import com.notamethod.ebox.gui.common.GameManagerException;
import com.notamethod.ebox.util.HelperClass;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class GamesWall extends Application {

    public ApplicationList bl;
    ApplicationDatabase applicationDatabase;
    DosBoxManager dosBoxManager = new DosBoxManager();
    URL unknownGame = null;
    ResourceBundle resourceBundle;
    GameManager gameManager;
    TilePane tilePane;

    @Override
    public void init() throws Exception {
        super.init();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ebox2_pu");
        applicationDatabase = new ApplicationDatabase(emf);
        ClassLoader classLoader = GamesWall.class.getClassLoader();
        unknownGame = classLoader.getResource("unknown.png");
        Configuration.pref.readConfig(Configuration.configFile);
        try {
            Path directory = Paths.get(Configuration.tempFolder);
            HelperClass.cleanDirectory(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bl = applicationDatabase.load(Configuration.gameFile);
        gameManager = new GameManager(applicationDatabase);
        // Locale locale = Locale.getDefault();//new Locale("fr"); // ou "en", "de", etc.
        Locale locale = new Locale("fr"); // ou "en", "de", etc.
        resourceBundle = ResourceBundle.getBundle("language", locale);
        System.out.println(resourceBundle.getString("confirmation.tile"));
        //System.out.println( Messages.getString("confirmation.tile"));

        Font font = FontUtils.loadCustomFont("retro-pixel-arcade.ttf", 8);
        System.out.println(font.getName());

    }

    @Override
    public void start(Stage stage) throws MalformedURLException, URISyntaxException {


        tilePane = new TilePane();
        tilePane.setPadding(new Insets(20, 10, 10, 10)); // top, right, bottom, left
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        tilePane.setPrefColumns(5);

        // Liste d’URLs d’image
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


      System.out.println("loading..."+gameManager.loadAll().size());
        updateList();


        ScrollPane scrollPane = new ScrollPane(tilePane);
        Scene scene = new Scene(scrollPane, 900, 600);


        // Autoriser le drop
        scrollPane.setOnDragOver(event -> {
            if (event.getGestureSource() != scrollPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Gérer le drop
        scrollPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                for (File file : files) {
                    System.out.println("Fichier déposé : " + file.getAbsolutePath());
                }
                success = true;
                if (files.size() > 1) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle(Messages.getString("confirmation.title"));
                    alert.setContentText(Messages.getString("confirmation.import.multifiles"));

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {

                    } else {

                        success = false;
                    }
                }
                GameActions gameActions = new GameActions(new DialogActionsJfx());
                for (File f : files) {
                    GameApp beanGame= null;
                    try {
                        beanGame = gameActions.createFromFile(f.getAbsoluteFile());
                    } catch (GameManagerException e) {
                        JOptionPane.showMessageDialog(null, e.getMessage(), null, JOptionPane.INFORMATION_MESSAGE);
                        continue;
                    }
                    gameManager.addGame(beanGame);

                }
                try {
                    updateList();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

            }
            event.setDropCompleted(success);
            event.consume();
        });
        //Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
//        pane.setStyle("-fx-background-color: #121212;"); // Fond sombre du conteneur
        scrollPane.setStyle("-fx-background: #121212;"); // Fond du ScrollPane
        stage.setTitle("Mur d'images cliquables");
        stage.setScene(scene);
        stage.show();
    }

    private void updateList() throws MalformedURLException, URISyntaxException {
        List<GameApp> games = gameManager.loadAll();
        //List<String> gameStrList = List.of(bl.getGameList());
        for (GameApp gameStr : games) {

            //ApplicationBean gameBean = bl.getGame(gameStr);
            //System.out.println("name " + gameBean.getName());
            // GameApp game = GameMapper.INSTANCE.toGameApp( gameBean );
            GameTile container = addGame(gameStr);
            tilePane.getChildren().add(container);

        }
    }

    private GameTile addGame(GameApp game) throws URISyntaxException, MalformedURLException {
        ImageView imageView;
        if (game.getImagePath() != null) {
            //URL url = new File(game.getImagePath()).toURL();
            imageView = new ImageView(new Image(game.getImagePath().toUri().toString()));
        } else {
            imageView = new ImageView(new Image(unknownGame.toURI().toString()));
        }

        imageView.setFitWidth(150);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
//**********************************
        // Panneau d'infos caché
        VBox infoPanel = new VBox();
        infoPanel.setPrefWidth(150);
        infoPanel.setStyle("-fx-background-color: #2c2c2c; -fx-padding: 10px;");
        infoPanel.getChildren().add(new Label(game.getName()));
        infoPanel.getChildren().add(new Label(String.valueOf(game.getYear())));
        infoPanel.setVisible(false);
        infoPanel.setOpacity(0);

        // Empilement vertical : image puis panneau
        GameTile container = new GameTile(5, game, imageView, infoPanel);

//250*330
        // Animation fade in/out
        FadeTransition fadePanelIn = new FadeTransition(Duration.millis(300), infoPanel);
        fadePanelIn.setFromValue(0);
        fadePanelIn.setToValue(1);

        FadeTransition fadePanelOut = new FadeTransition(Duration.millis(300), infoPanel);
        fadePanelOut.setFromValue(1);
        fadePanelOut.setToValue(0);

        //*****************************************

        imageView.setEffect(getDropShadow2());

        // Création du menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white;");
        MenuItem openItem = new MenuItem(Messages.getString("game.action.launch"));
        MenuItem infoItem = new MenuItem("Infos");
        MenuItem editItem = new MenuItem(Messages.getString("game.action.edit"));
        MenuItem deleteItem = new MenuItem("Supprimer");

// Actions des items
        openItem.setOnAction(e -> System.out.println("Ouvrir : " + game.getName()));
        infoItem.setOnAction(e -> System.out.println("Infos : " + game.getName()));
        editItem.setOnAction(e -> actionEdit(game));
        deleteItem.setOnAction(e -> System.out.println("Supprimer : " + game.getName()));

// Ajout des items au menu
        contextMenu.getItems().addAll(openItem, editItem, infoItem, deleteItem);

        container.setOnMouseEntered(e -> {
            //imageView.setOpacity(0.0); // démarre transparent
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), imageView);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            ScaleTransition zoomIn = new ScaleTransition(Duration.millis(300), container);
            zoomIn.setToX(1.05);
            zoomIn.setToY(1.05);
            zoomIn.play();
            infoPanel.setVisible(true);
            fadePanelIn.play();
        });

        container.setOnMouseExited(e -> {
            FadeTransition hoverFade = new FadeTransition(Duration.millis(300), imageView);
            hoverFade.setFromValue(0.7);
            hoverFade.setToValue(1.0);
            hoverFade.play();
            ScaleTransition zoomOut = new ScaleTransition(Duration.millis(200), container);
            zoomOut.setToX(1.0);
            zoomOut.setToY(1.0);
            zoomOut.play();
            fadePanelOut.play();
            fadePanelOut.setOnFinished(ev -> infoPanel.setVisible(false));

        });


        // Clic : afficher une action

        // Affichage du menu sur clic droit
        imageView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(imageView, e.getScreenX(), e.getScreenY());
            } else if (e.getButton() == MouseButton.PRIMARY) {
                System.out.println("Image cliquée : " + game.getName());
                if (e.getClickCount() == 2) {
                    System.out.println("Double clicked");
                    int returne = 0;
                    log.debug(game.toString());
                    try {
                        returne = dosBoxManager.runApplication(game.getGameExe(), GameMapper.INSTANCE.toAppBean(game));
                    } catch (DosBoxException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println(returne);
                }
            }
        });
        return container;
    }

    private void actionEdit(GameApp gameBean) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameEditor.fxml"));
            DialogPane dialogPane = loader.load();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Éditer un jeu");

            GameEditorController controller = loader.getController();
            GameApp game = new GameApp();

            controller.setGame(gameBean);

                    Optional < ButtonType > result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
               // GameEditorController controller = loader.getController();
                GameApp editedGame = controller.getGame();
                editedGame.setId(gameBean.getId());
                //editedGame.merge(game);
                // Utiliser l'objet Game ici
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Effect getDropShadow1() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setColor(Color.web("#333"));
        return shadow;
    }

    private Effect getDropShadow2() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(10);
        shadow.setColor(Color.color(0.1, 0.1, 0.1, 0.7)); // ombre gris-noir transparente


        return shadow;
    }


    public static void main(String[] args) {
        launch();
    }


}
