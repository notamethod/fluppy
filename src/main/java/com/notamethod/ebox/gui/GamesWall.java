package com.notamethod.ebox.gui;


import com.notamethod.ebox.core.*;
import com.notamethod.ebox.gui.common.GameActions;
import com.notamethod.ebox.core.GameManagerException;
import com.notamethod.ebox.util.HelperClass;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import javafx.scene.layout.*;
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

    ApplicationDatabase applicationDatabase;
    DosBoxManager dosBoxManager = new DosBoxManager();
    URL unknownGame = null;
    GameManager gameManager;
    TilePane tilePane;

    @Override
    public void init() throws Exception {
        super.init();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ebox2_pu");
        applicationDatabase = new ApplicationDatabase(emf);
        ClassLoader classLoader = GamesWall.class.getClassLoader();
        unknownGame = classLoader.getResource("unknown.jpg");
        Configuration.pref.readConfig(Configuration.configFile);
        try {
            Path directory = Paths.get(Configuration.tempFolder);
            HelperClass.cleanDirectory(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gameManager = new GameManager(applicationDatabase);
        // Locale locale = Locale.getDefault();//new Locale("fr"); // ou "en", "de", etc.
        Locale locale = new Locale("fr"); // ou "en", "de", etc.


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


        System.out.println("loading..." + gameManager.loadAll().size());
        updateList();

        // stage.initStyle(StageStyle.UNDECORATED);
        ScrollPane scrollPane = new ScrollPane(tilePane);
//        HBox titleBar=getTitleBar();
//        VBox root = new VBox();
//        root.getChildren().addAll(titleBar, scrollPane);

        StackPane root = new StackPane(scrollPane);
        Scene scene = new Scene(root, 900, 700);

        // Autoriser le drop
        scene.setOnDragOver(event -> {
            if (event.getGestureSource() != scrollPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }


            event.consume();
        });
        scene.setOnDragExited(event -> {

        });
        // Gérer le drop
        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success=true;
                importFiles(db.getFiles());
            }
            event.setDropCompleted(success);
            event.consume();
        });
        //Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        scrollPane.setStyle("-fx-background: #121212;"); // Fond du ScrollPane
        stage.setTitle("Mur d'images cliquables");
        stage.setScene(scene);
        stage.show();
    }

    private void importFiles(List<File> files) {

        GameActions gameActions = new GameActions(new DialogActionsJfx());
        List<GameApp> gampeApps = gameActions.createFromFiles(files);
        List<String> errors = new ArrayList<>();
        for (GameApp gameApp : gampeApps) {
            try {
                gameManager.addGame(gameApp);
            } catch (GameManagerException e) {
                errors.add(e.getLocalizedMessage()+": "+gameApp.getGamePath());
                log.error("import error", e);
            }
        }
        if (!errors.isEmpty()) {
            gameActions.showErrors(errors);
        }
        updateList();
    }


    private void updateList() {
        List<GameApp> games = gameManager.loadAll();
        tilePane.getChildren().clear();
        for (GameApp gameStr : games) {
            GameTile container = addGame(gameStr);
            tilePane.getChildren().add(container);
        }
    }

    private GameTile addGame(GameApp game) {
        ImageView imageView=null;
        Image image;
        StackPane stackPane=null;
        if (game.getImagePath() != null) {
            image = new Image(game.getImagePath().toUri().toString());
            imageView = ImageUtils.resize(image);
        } else {
             stackPane = getNoCoverGame(game);

        }


        //**********************************
        // Panneau d'infos caché
        HBox infoPanelActions = new HBox();

        VBox infoPanel = new VBox();
        Button launchButton=new Button(">");

        launchButton.setOnAction(e -> {
            try {
                dosBoxManager.runApplication(game.getGameExe(), game);
            } catch (DosBoxException ex) {
                throw new RuntimeException(ex);
            }
        });
        infoPanel.setPrefWidth(150);
        infoPanel.setStyle("-fx-background-color: #2c2c2c; -fx-padding: 10px;");
        infoPanelActions.getChildren().add(launchButton); // column=1 row=0
        infoPanelActions.getChildren().add(new Button("y"));  // column=2 row=0
        infoPanel.getChildren().add(infoPanelActions);
        infoPanel.getChildren().add(new Label(game.getName()));
        infoPanel.getChildren().add(new Label(String.valueOf(game.getYear())));
        infoPanel.setVisible(false);
        infoPanel.setOpacity(0);
        GameTile container;
        // Empilement vertical : image puis panneau
        if (imageView!=null) {
             container = new GameTile(5, game, imageView, infoPanel);
        }else{
            container = new GameTile(5, game, stackPane, infoPanel);
        }

//250*330
        // Animation fade in/out
        FadeTransition fadePanelIn = new FadeTransition(Duration.millis(300), infoPanel);
        fadePanelIn.setFromValue(0);
        fadePanelIn.setToValue(1);

        FadeTransition fadePanelOut = new FadeTransition(Duration.millis(300), infoPanel);
        fadePanelOut.setFromValue(1);
        fadePanelOut.setToValue(0);

        //*****************************************

        container.setEffect(getDropShadow2());

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
        deleteItem.setOnAction(e -> actionDelete(game));

// Ajout des items au menu
        contextMenu.getItems().addAll(openItem, editItem, infoItem, deleteItem);

        container.setOnMouseEntered(e -> {
            //imageView.setOpacity(0.0); // démarre transparent
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), container);
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
            FadeTransition hoverFade = new FadeTransition(Duration.millis(300), container);
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



        container.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(container, e.getScreenX(), e.getScreenY());
            } else if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 1) {
                    int returne = 0;
                    log.debug(game.toString());
                    try {
                        returne = dosBoxManager.runApplication(game.getGameExe(), game);
                    } catch (DosBoxException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println(returne);
                }
            }
        });
        return container;
    }

    private StackPane getNoCoverGame(GameApp game) {

        ImageView imageView = null;

        try {
           Image image = new Image(unknownGame.toURI().toString());
             imageView = new ImageView(image);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        // Créer le texte
        Label label = new Label(game.getName()+"\n"+game.getYear());
        label.setStyle("-fx-text-fill: white; -fx-font-size: 8px; -fx-background-color: rgba(0,0,0,0.5);");

        // Empiler l'image et le texte
        StackPane stackPane = new StackPane();
        imageView.setFitWidth(150);
        imageView.setFitHeight(200);
        stackPane.getChildren().addAll(imageView, label);
        return stackPane;
    }

    private void actionDelete(GameApp game) {
        if (gameManager.deleteGame(game)>0){
            updateList();
        }

    }

    private void actionEdit(GameApp gameBean) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameEditor.fxml"));
            DialogPane dialogPane = loader.load();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Éditer un jeu");

            GameEditorController controller = loader.getController();


            controller.setGame(gameBean);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                // GameEditorController controller = loader.getController();
                GameApp editedGame = controller.getGame();
                editedGame.setId(gameBean.getId());
                editedGame.merge(gameBean);
                if (!editedGame.equals(gameBean)) {
                    gameManager.save(editedGame);
                }
                updateList();
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

    public HBox getTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: black; -fx-padding: 10;");
        Label title = new Label("SDOG-L");
        title.setTextFill(Color.WHITE);
        titleBar.getChildren().add(title);

        return titleBar;
    }

    public static void main(String[] args) {
        launch();
    }


}
