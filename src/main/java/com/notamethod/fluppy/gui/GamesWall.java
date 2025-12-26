package com.notamethod.fluppy.gui;


import com.notamethod.fluppy.core.*;
import com.notamethod.fluppy.gui.common.GameActions;
import com.notamethod.fluppy.core.GameManagerException;
import com.notamethod.fluppy.io.PreferencesIO;
import com.notamethod.fluppy.util.HelperClass;
import jakarta.persistence.EntityManagerFactory;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

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

    private static final int WIDTH = 900;
    private static final int HEIGHT = 700;
    private static final int ROW_SIZE = 9;
    ApplicationDatabase applicationDatabase;
    PreferencesBean preferences;
    DosBoxManager dosBoxManager = new DosBoxManager();

    GameManager gameManager;
    CategoryManager categoryManager;

    StackPane midRoot;
    GameDetailPanel detailPane;
    List<VBox> gamesBlocks = new ArrayList<>();
    VBox content;
    private double xOffset = 0;
    private double yOffset = 0;
    Effects effects;
    private Category expandCategory = null;
    List<Category> gameCategories = new ArrayList<>();
    private double width = WIDTH;

    @Override
    public void init() throws Exception {
        super.init();
        //DTP

        effects = new Effects();
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

        applicationDatabase = new ApplicationDatabase(emf);
        preferences = PreferencesIO.load();
        try {
            Path directory = Paths.get(Configuration.tempFolder);
            HelperClass.cleanDirectory(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gameManager = new GameManager(applicationDatabase, preferences);
        categoryManager = new CategoryManager(applicationDatabase);
        detailPane = new GameDetailPanel(dosBoxManager, gameManager);
        gameCategories = categoryManager.getShownCategories();
        //FIXME:remove locale test
        // Locale locale = Locale.getDefault();//new Locale("fr"); // ou "en", "de", etc.
        Locale locale = new Locale("fr"); // ou "en", "de", etc.
        FontUtils.loadCustomFont("retro-pixel-arcade.ttf", 8);
        FontUtils.loadCustomFont("MonkeyIsland-1991.ttf", 16);
        FontUtils.loadCustomFont("MonkeyIsland-1990.ttf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-menu-shadow.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-solid.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-outline.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-credits.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-subtitle-roman.otf", 16);
    }

    @Override
    public void start(Stage stage) throws MalformedURLException, URISyntaxException {

        stage.initStyle(StageStyle.UNDECORATED);

        HBox topRibbon = createTopRibbon(stage);
        //StackPane topRibbon = effects.noiseEffectWrapper(topRibbon0);
        Animation bordureAnim = effects.getBordureAnim(topRibbon);

        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        content = new VBox();
        updateList();


        ScrollPane scrollPane = new ScrollPane(content);

        scrollPane.setPannable(true); // active le drag à la souris
        //speed scrollpane
        scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            double deltaY = e.getDeltaY();
            double height = scrollPane.getContent().getBoundsInLocal().getHeight();
            double vValue = scrollPane.getVvalue();
            // facteur de vitesse (ici x3)
            scrollPane.setVvalue(vValue - deltaY / height * 3);
            e.consume();
        });

        // Label overlay
        Label dropLabel = new Label(Messages.getString("drop.here"));
        dropLabel.setTextFill(Color.GRAY);
        dropLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        dropLabel.setVisible(false); // caché par défaut

        scrollPane.setFitToWidth(true); // Pour que le contenu prenne toute la largeur
        scrollPane.setStyle("-fx-background: transparent;");
        scrollPane.setId("scrollpane-tiles");

        VBox root0 = new VBox();
        // Cette ligne est cruciale
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        StackPane topRibbon0 = new StackPane(topRibbon, dropLabel);


        detailPane.setListener(new PanelListener() {
                                   public void onUpdate() {
                                       updateList();
                                       detailPane.hide();
                                   }

                                   public void onClose() {
                                       detailPane.hide();
                                   }
                               }
        );
        midRoot = new StackPane();
        midRoot.setId("midRoot");

        SearchOverlay searchOverlay = new SearchOverlay();

        midRoot.getChildren().addAll(scrollPane, detailPane, searchOverlay);

        root0.getChildren().addAll(/*titleBar, */topRibbon0, midRoot);
        midRoot.setId("realRoot");
        Scene scene = new Scene(root0, WIDTH, HEIGHT);

        /*  drag&drop on top ribbon */
        topRibbon.setOnDragOver(event -> {
            if (event.getGestureSource() != scrollPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        topRibbon.setOnDragEntered(e -> {
            topRibbon.setStyle("-fx-background-color: green;");
            // topRibbon.s
        });
        topRibbon.setOnDragExited(e -> topRibbon.setStyle("-fx-background-color: #141414;"));

        // Gérer le drop
        topRibbon.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                importFiles(db.getFiles());
            }
            event.setDropCompleted(success);
            event.consume();
        });
        scene.setOnDragExited(event -> {
        });
        // Écoute globale du drag
        scene.setOnDragEntered(event -> {
            if (event.getDragboard().hasFiles()) {
                bordureAnim.play();
                dropLabel.setVisible(true);
                topRibbon.getStyleClass().add("ribbon-highlight");
            }
        });

        scene.setOnDragExited(event -> {
            bordureAnim.stop();
            dropLabel.setVisible(false);
            topRibbon.getStyleClass().remove("ribbon-highlight");
            topRibbon.setStyle("-fx-border-color: transparent"); // Fond du ScrollPane
        });

        scene.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String c = e.getCharacter();
            if (!searchOverlay.isVisible()) {
                searchOverlay.show();
                Platform.runLater(() -> {
                    searchOverlay.requestFocusOnField();
                    searchOverlay.appendToQuery(c);
                });
                //e.consume();
            }

        });
        PauseTransition debounce = new PauseTransition(Duration.millis(200));
        searchOverlay.queryProperty().addListener((obs, old, q) -> {
            if (q.length() > 2) {
                debounce.stop();
                debounce.setOnFinished(e -> applyFilter(q));
                debounce.playFromStart();
            }else{
                if (q.isEmpty()) {
                    searchOverlay.hide();
                    expandCategory = null;
                    updateList();
                }
            }

        });

        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        scrollPane.setStyle("-fx-background: #121212;"); // Fond du ScrollPane
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/dosdog2.png")));
        stage.setScene(scene);
        stage.show();
    }

    private void applyFilter(String q) {
        expandCategory = categoryManager.searchCategory(q);
        updateList();
    }


    private HBox createTopRibbon(Stage stage) {

        HBox topRibbon = new HBox();
        topRibbon.setPrefHeight(60);
        topRibbon.setSpacing(15);
        topRibbon.getStyleClass().add("ribbon");
        topRibbon.getStyleClass().add("scanline");

        Animation biosAnim = effects.biosEffectAnim(topRibbon);
        biosAnim.play();
        Animation distortion = effects.distortionAnim(topRibbon);
        distortion.play();

        Label info = new Label("(c) 2025");
        info.setTextFill(Color.WHITE);
        ClassLoader classLoader = GamesWall.class.getClassLoader();
        URL logoUrl = classLoader.getResource("dosdog2.png");
        Image logo = new Image(logoUrl.toString(), 60, 60, false, true);
        ImageView logoView = new ImageView(logo);
        URL titleUrl = classLoader.getResource("fluppy3.png");
        Image titleImage = new Image(titleUrl.toString(), 90, 50, true, true);
        ImageView titleView = new ImageView(titleImage);
        Image gear = new Image(getClass().getResourceAsStream("/images/gear2.png"), 32, 32, false, false);
        ImageView gearIcon = new ImageView(gear);
        Button gearButton = new Button();
        gearButton.setGraphic(gearIcon);
        gearButton.setStyle("-fx-background-color: transparent;");
        gearButton.setOnAction(e -> {
            PreferencesDialog dialog = new PreferencesDialog(stage);
            PreferencesBean neawBean = dialog.showAndWaitForResult();
            if (neawBean != null) {
                if (preferences.isNsfw() != neawBean.isNsfw()) {
                    preferences.setNsfw(!preferences.isNsfw());
                    updateList();
                }
                if (preferences.isFullScreen() != neawBean.isFullScreen()) {
                    preferences.setFullScreen(!preferences.isFullScreen());
                    //updateList();
                }
            }
        });
        //plus
        ImageView plusImage = new ImageView(new Image(getClass().getResourceAsStream("/images/sizing2.png"), 32, 32, false, false));
        Button plusButton = new Button();
        plusButton.setGraphic(plusImage);
        plusButton.setStyle("-fx-background-color: transparent;");
        plusButton.setOnAction(e -> {
            if (stage.isFullScreen()) {
                stage.setFullScreen(false);
                stage.setMaximized(false);
                stage.setWidth(WIDTH);
                stage.setHeight(HEIGHT);
                stage.centerOnScreen();
            } else {
                stage.setFullScreen(true);
                width = stage.getWidth();
            }
            // AddGameDialog dialog = new AddGameDialog(null, null);
            //dialog.showAndWait();
        });

        //quite
        ImageView quitImg = new ImageView(new Image(getClass().getResourceAsStream("/images/quit2.png"), 32, 32, false, false));
        Button quitButton = new Button();
        quitButton.setGraphic(quitImg);
        quitButton.setStyle("-fx-background-color: transparent;");
        quitButton.setOnAction(e -> stage.close());
        Region spacerRibbon = new Region();
        HBox.setHgrow(spacerRibbon, Priority.ALWAYS);
        topRibbon.setAlignment(Pos.CENTER_LEFT);
        topRibbon.getChildren().addAll(logoView, titleView, info, spacerRibbon, gearButton, plusButton, quitButton);

        topRibbon.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topRibbon.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        return topRibbon;
    }

    private void importFiles(List<File> files) {
        GameActions gameActions = new GameActions(new DialogActionsJfx());
        List<GameApp> gampeApps = gameActions.createFromFiles(files);
        List<String> errors = new ArrayList<>();
        for (GameApp gameApp : gampeApps) {
            try {
                gameManager.addGame(gameApp);
            } catch (GameManagerException e) {
                errors.add(e.getLocalizedMessage() + ": " + gameApp.getGamePath());
                log.error("import error", e);
            }
        }
        if (!errors.isEmpty()) {
            gameActions.showErrors(errors);
        }
        updateList();
    }

    private void updateList() {
        log.debug("update list");
        this.gamesBlocks.clear();
        content.getChildren().clear();
        Set<Long> gameIds = new HashSet<>();
        if (expandCategory != null) {
            VBox box = createBlock(expandCategory, 25, gameIds);
            gamesBlocks.add(box);

        } else {
            for (int i = 0; i < gameCategories.size(); i++) {
                VBox box = createBlock(gameCategories.get(i), ROW_SIZE, gameIds);
                if (box != null) {
                    gamesBlocks.add(box);
                }
            }
        }

        content.getChildren().addAll(this.gamesBlocks);
    }

    private VBox createBlock(Category category, int count, Set<Long> gameIds) {
        List<GameApp> games;
        switch (category.getCategoryType()) {
            case SEARCH:
                games = gameManager.searchByName(category.getFilter());
                break;
            case RECENTLY_ADDED:
                games = gameManager.getLastAdded(count);
                break;
            case MOST_PLAYED:
                games = gameManager.getMostPlayedGames(count);
                break;
            case FAVORITES:
                games = gameManager.getFavoriteGames(count);
                break;
            case GENRE:
                if ("all".equals(category.getId())) {
                    games = gameManager.loadAllButNot(gameIds);
                } else {
                    games = gameManager.getFromGenre(category.getId(), count);
                }
                break;
            default:
                games = null;
        }
        if (games != null/* && !games.isEmpty()*/) {
            return createBlock(category, games, gameIds);
        }
        return null;
    }


    private VBox createBlock(Category category, List<GameApp> games, Set<Long> gameIds) {
        TilePane tilePane = new TilePane();
        tilePane.setId("tilePane-" + category.getCategoryType());
        tilePane.setPadding(new Insets(20, 10, 30, 0)); // top, right, bottom, left
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        tilePane.setPrefColumns(5);
        tilePane.setAlignment(Pos.TOP_LEFT);
        for (GameApp gameStr : games) {
            if (!gameIds.contains(gameStr.getId())) {
                gameIds.add(gameStr.getId());
                GameTile container = addGame(gameStr);

                tilePane.getChildren().add(container);
            }
        }
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(20, 10, 10, 50)); // top, right, bottom, left
        Label blockTitle = new Label(category.getLabel());
        blockTitle.getStyleClass().add("blockTitle");
        // Ajouter une action au clic
        blockTitle.setOnMouseClicked(event -> {
            activateCategory(category);
        });
        vBox.getChildren().addAll(blockTitle, tilePane);
        return vBox;
    }

    private void activateCategory(Category category) {

        if (category.getCategoryType().equals(CategoryType.GENRE) && "all".equals(category.getId()))
            return;
        if (expandCategory != null && expandCategory.getCategoryType().equals(category.getCategoryType())
                && category.getId().equals(expandCategory.getId())) {
            expandCategory = null;
        } else if (expandCategory == null || (expandCategory != null && !category.getId().equals(expandCategory.getId()))) {
            expandCategory = category;
        } else {
            expandCategory = null;
        }
        updateList();
    }

    private GameTile addGame(GameApp game) {
        StackPane imagePane = ImageFactory.getThumb(game);
        GameTile container;

        container = new GameTile(5, game, imagePane);

        container.setId("container-" + game.getName());

        //*****************************************

        container.setEffect(getDropShadow2());

        // Création du menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white;");
        MenuItem deleteItem = new MenuItem("Supprimer");

        //editItem.setOnAction(e -> actionEdit(game));
        deleteItem.setOnAction(e -> actionDelete(game));

// Ajout des items au menu
        contextMenu.getItems().addAll(deleteItem);

        PauseTransition hoverDelay = new PauseTransition(Duration.millis(600));
        hoverDelay.setOnFinished(e -> {
            Point2D point = caculatePosition(container);
            detailPane.show(game, point.getX(), point.getY());
        });
        PauseTransition hoverDelayExit = new PauseTransition(Duration.millis(50));
        hoverDelayExit.setOnFinished(e -> {
            if (!detailPane.isHover()) {
                detailPane.hide();

            } else {
                //nothing
            }

        });

        container.setOnMouseEntered(e -> {
            if (detailPane.isVisible())
                detailPane.hide();
            //imageView.setOpacity(0.0); // démarre transparent
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), container);
            fadeIn.setFromValue(1.0);
            fadeIn.setToValue(0.9);
            fadeIn.play();
            ScaleTransition zoomIn = new ScaleTransition(Duration.millis(300), container);
            zoomIn.setToX(1.01);
            zoomIn.setToY(1.01);
            zoomIn.play();
            hoverDelay.playFromStart();

        });

        container.setOnMouseExited(e -> {
            FadeTransition hoverFade = new FadeTransition(Duration.millis(100), container);
            hoverFade.setFromValue(0.9);
            hoverFade.setToValue(1.0);
            hoverFade.play();
            ScaleTransition zoomOut = new ScaleTransition(Duration.millis(100), container);
            zoomOut.setToX(1.0);
            zoomOut.setToY(1.0);
            zoomOut.play();
            hoverDelay.stop();
            hoverDelayExit.playFromStart();


        });


        container.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(container, e.getScreenX(), e.getScreenY());
            } else if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 1) {
                    long returne = 0;
                    log.debug(game.toString());
                    try {
                        returne = dosBoxManager.runApplication(game.getGameExe(), game);
                    } catch (DosBoxException ex) {
                        throw new RuntimeException(ex);
                    }
                    gameManager.updateTime(game, returne);
                }
            }
        });

        detailPane.hoverProperty().addListener((obs, wasHover, isHover) -> {
            if (!isHover && !container.isHover()) {
                detailPane.hide();
            }
        });
        return container;
    }

    private Point2D caculatePosition(GameTile container) {
        int prevWidth = 600;
        Bounds tileSceneBounds = container.localToScene(container.getBoundsInLocal());
        detailPane.applyCss();
        detailPane.layout();
        double fixWidth = midRoot.getWidth() - WIDTH > 0 ? (midRoot.getWidth() - WIDTH) / 2 : 0;
        double fixHeight = fixWidth > 0 ? (midRoot.getHeight() - HEIGHT) / 2 : 0;

        Bounds screenBounds = container.localToScreen(container.getBoundsInLocal());


        Bounds tileParentBounds = midRoot.sceneToLocal(tileSceneBounds);
        Point2D point = container.getScene().getRoot().sceneToLocal(tileSceneBounds.getMinX(), tileSceneBounds.getMinY());

        Point2D fixedPoint2 = new Point2D(tileParentBounds.getMinX() - container.getWidth() - fixWidth - 40, tileParentBounds.getMinY() - container.getHeight() - fixHeight);
        double diffx = (fixedPoint2.getX() + prevWidth) - width/*screen.getMaxX()*/;
        log.debug("tile" + tileSceneBounds.getMinX() + prevWidth);
        log.debug("tile" + screenBounds.getMinX() + prevWidth);
        log.debug("diff:" + diffx);
        double decalRatio = -1.1;
        if (diffx > 0)
            fixedPoint2 = fixedPoint2.add(decalRatio * diffx, 0);
        return fixedPoint2;
    }


    private void actionDelete(GameApp game) {
        if (gameManager.deleteGame(game) > 0) {
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
                GameApp editedGame = controller.getResult();
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

    public static void main(String[] args) {
        launch();
    }
}

