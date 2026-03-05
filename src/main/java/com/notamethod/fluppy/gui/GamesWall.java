package com.notamethod.fluppy.gui;


import com.notamethod.fluppy.core.*;
import com.notamethod.fluppy.core.category.Category;
import com.notamethod.fluppy.core.category.CategoryManager;
import com.notamethod.fluppy.core.category.CategoryType;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameManager;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.dosbox.DosBoxException;
import com.notamethod.fluppy.dosbox.DosBoxManager;
import com.notamethod.fluppy.gui.common.DialogActionsJfx;
import com.notamethod.fluppy.gui.common.GameActions;
import com.notamethod.fluppy.core.game.GameManagerException;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.util.HelperClass;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
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

    private Stage stage;

    public enum TILES_VIEW {
        DEFAULT, YEARS;
    }

    private static final int ORIGINAL_WIDTH = 900;
    private static final int ORIGINAL_HEIGHT = 700;
    private static final int ROW_SIZE = 9;
    double midWidth;
    ApplicationDatabase applicationDatabase;
    PreferencesBean preferences;
    DosBoxManager dosBoxManager = new DosBoxManager();

    GameManager gameManager;
    CategoryManager categoryManager;

    StackPane mainPane;
    private Pane backOverlay;
    GameDetailPanel detailPane;
    List<Node> gamesBlocks = new ArrayList<>();
    VBox content;
    private double xOffset = 0;
    private double yOffset = 0;
    Effects effects;
    private Category expandCategory = null;
    List<Category> gameCategories = new ArrayList<>();
    private double width = ORIGINAL_WIDTH;
    private TILES_VIEW view = TILES_VIEW.DEFAULT;
    private boolean isFullScreen = false;

    @Override
    public void init() throws Exception {
        super.init();
        //DTP

        effects = new Effects();

        applicationDatabase = new ApplicationDatabase();
        preferences = PreferencesIO.load();
        try {
            Path directory = Paths.get(Configuration.tempFolder);
            HelperClass.cleanDirectory(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gameManager = new GameManager(applicationDatabase, preferences, dosBoxManager);
        categoryManager = new CategoryManager(applicationDatabase);
        detailPane = new GameDetailPanel(dosBoxManager, gameManager);
        gameCategories = categoryManager.getShownCategories(preferences.getViewFilter());

        FontUtils.loadCustomFont("retro-pixel-arcade.ttf", 8);
        FontUtils.loadCustomFont("MonkeyIsland-1991.ttf", 16);
        FontUtils.loadCustomFont("MonkeyIsland-1990.ttf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-menu-shadow.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-solid.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-outline.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-credits.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-subtitle-roman.otf", 16);
        FontUtils.loadCustomFont("Storyboo.ttf", 16);
        FontUtils.loadCustomFont("Retro Gaming.ttf", 16);
        FontUtils.loadCustomFont("PxPlus_IBM_VGA_8x16.ttf", 16);
    }

    @Override
    public void start(Stage stage) throws MalformedURLException, URISyntaxException {

        stage.initStyle(StageStyle.UNDECORATED);
        this.stage = stage;
        HBox topRibbon = createTopRibbon(stage);
        Animation bordureAnim = effects.getBordureAnim(topRibbon);

        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        content = new VBox();
        updateList();

        detailPane.setListener(new PanelListener() {
                                   public void onUpdate() {
                                       updateList();
                                       detailPane.hide();
                                   }

                                   public void onClose() {
                                       detailPane.hide();
                                   }

                                   public void onLaunchGame() {
                                       Platform.runLater(() -> darkenUI());
                                       // darkenUI();
                                   }

                                   public void onExitGame() {
                                       lightenUI();
                                   }
                               }
        );
        ScrollPane scrollPane = createScrollPane();

        // Label overlay
        Label dropLabel = new Label(Messages.getString("drop.here"));
        dropLabel.setTextFill(Color.GRAY);
        dropLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        dropLabel.setVisible(false); // caché par défaut


        VBox root = new VBox();
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        StackPane topRibbon0 = new StackPane(topRibbon, dropLabel);


        mainPane = new StackPane();

        SearchOverlay searchOverlay = new SearchOverlay();

        mainPane.getChildren().addAll(scrollPane, detailPane, searchOverlay);

        root.getChildren().addAll(/*titleBar, */topRibbon0, mainPane);


        backOverlay = initBackOverlay();
        backOverlay.prefWidthProperty().bind(root.widthProperty());
        backOverlay.prefHeightProperty().bind(root.heightProperty());
        mainPane.getChildren().add(backOverlay);

        Scene scene = new Scene(root, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);

        /*  drag&drop on top ribbon */
        topRibbon0.setOnDragOver(event -> {
            if (event.getGestureSource() != scrollPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        topRibbon0.setOnDragEntered(e -> {
            topRibbon.setStyle("-fx-background-color: green;");
            // topRibbon.s
        });
        topRibbon0.setOnDragExited(e -> topRibbon.setStyle("-fx-background-color: #141414;"));

        // Gérer le drop
        topRibbon0.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                importFiles(db.getFiles());
            }
            event.setDropCompleted(success);
            event.consume();
        });
        scene.widthProperty().addListener((obs, oldV, newV) -> {
            midWidth = newV.doubleValue();
            mainPane.setPrefWidth(midWidth);
            mainPane.setMaxWidth(midWidth);
            mainPane.setMinWidth(midWidth);

            //try request layuout on reduce
            mainPane.requestLayout();
        });
//
        scene.heightProperty().addListener((obs, oldV, newV) ->
                log.debug("Height = " + newV)
        );

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
            } else {
                if (q.isEmpty()) {
                    searchOverlay.hide();
                    expandCategory = null;
                    updateList();
                }
            }

        });

        scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        scrollPane.setStyle("-fx-background: #1A1E2E;"); // Fond du ScrollPane

        scrollPane.getStyleClass().add("main-pane");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/dosdog2.png")));
        stage.setScene(scene);

        stage.show();
    }

    private Pane initBackOverlay() {


        backOverlay = new Pane();
        backOverlay.setStyle("-fx-background-color: black;");
        backOverlay.setOpacity(0);
        backOverlay.setMouseTransparent(true); // ne bloque pas les clics
        backOverlay.setVisible(true);


        return backOverlay;


    }

    private ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPannable(true); // active le drag à la souris
        scrollPane.setFitToWidth(false);
        scrollPane.setId("scrollpane-tiles");
        //speed scrollpane
        scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            double deltaY = e.getDeltaY();
            double height = scrollPane.getContent().getBoundsInLocal().getHeight();
            double vValue = scrollPane.getVvalue();
            // facteur de vitesse (ici x3)
            scrollPane.setVvalue(vValue - deltaY / height * 3);
            e.consume();
        });
        return scrollPane;
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
        // topRibbon.getStyleClass().add("scanline");

        Animation biosAnim = effects.biosEffectAnim(topRibbon);
        biosAnim.play();
        Animation distortion = effects.distortionAnim(topRibbon);
        distortion.play();

        Label info = new Label("(c) 2026");
        info.setTextFill(Color.WHITE);
        ClassLoader classLoader = GamesWall.class.getClassLoader();
        URL logoUrl = classLoader.getResource("dosdog2.png");
        Image logo = new Image(logoUrl.toString(), 60, 60, false, true);
        ImageView logoView = new ImageView(logo);
        URL titleUrl = classLoader.getResource("fluppy3.png");
        Image titleImage = new Image(titleUrl.toString(), 90, 50, true, true);
        ImageView titleView = new ImageView(titleImage);
        titleView.setOnMouseClicked(event -> {
            AboutDialog dialog = new AboutDialog(stage);
            dialog.showAndWait();
        });
        Button gearButton = createRibbonButton("/images/gear2.png");
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
                    // updateList();
                }
            }
        });
        //plus
        ImageView plusImage = new ImageView();
        updateSizingImage(plusImage);
        Button plusButton = new Button();
        plusButton.setGraphic(plusImage);
        plusButton.setStyle("-fx-background-color: transparent;");
        plusButton.setOnAction(e -> {
            updateSizingImage(plusImage);
            if (isFullScreen) {

                stage.setMaximized(false);
                stage.setWidth(ORIGINAL_WIDTH);
                stage.setHeight(ORIGINAL_HEIGHT);
                stage.centerOnScreen();
                log.debug("reduce");
                isFullScreen = false;
                //midRoot.setPrefWidth(800);
                //  midRoot.requestLayout();

                //e.cons

            } else {
                isFullScreen = true;
                stage.setMaximized(true);
                mainPane.requestLayout();

            }
            updateSizingImage(plusImage);
            updateList();

        });

        //quite
        Button quitButton = createRibbonButton("/images/quit2.png");
        quitButton.setOnAction(e -> stage.close());

        Button calendarButton = createRibbonButton("/images/calendar1.png");
        calendarButton.setOnAction(e -> changeView());
        Region spacerRibbon = new Region();
        HBox.setHgrow(spacerRibbon, Priority.ALWAYS);
        topRibbon.setAlignment(Pos.CENTER_LEFT);
        topRibbon.getChildren().addAll(logoView, titleView, info, spacerRibbon, calendarButton, gearButton, plusButton, quitButton);

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

    private Button createRibbonButton(String imagePath) {
        Image img = new Image(getClass().getResourceAsStream(imagePath), 32, 32, false, false);
        ImageView imgView = new ImageView(img);
        Button button = new Button();
        button.setGraphic(imgView);
        button.setStyle("-fx-background-color: transparent;");
        return button;
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

    private void updateSizingImage(ImageView imageView) {
        if (isFullScreen) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/size_min.png"), 32, 32, false, false));

        } else {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/size_max.png"), 32, 32, false, false));
        }

    }

    private void updateList() {
        log.debug("update list");
        this.gamesBlocks.clear();
        content.getChildren().clear();
        Set<Long> gameIds = new HashSet<>();
        if (expandCategory != null) {
            Node box = createBlock(expandCategory, 25, gameIds);
            gamesBlocks.add(box);

        } else {
            for (int i = 0; i < gameCategories.size(); i++) {
                Node box = createBlock(gameCategories.get(i), ROW_SIZE, gameIds);
                if (box != null) {
                    gamesBlocks.add(box);
                }
            }
        }

        content.getChildren().addAll(this.gamesBlocks);
    }

    private Node createBlock(Category category, int count, Set<Long> gameIds) {
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
                    games = gameManager.getFromGenre(category.getId(), count, gameIds);
                }
                break;
            case YEAR:
                games = gameManager.getFromYear(Integer.valueOf(category.getId()), count);
                break;
            default:
                games = null;
        }
        if (games != null && (!category.getCategoryType().equals(CategoryType.FAVORITES) || category.getCategoryType().equals(CategoryType.FAVORITES) && !games.isEmpty())/* && !games.isEmpty()*/) {
            return createBlock(category, games, gameIds);
        }
        return null;
    }


    private Node createBlock(Category category, List<GameApp> games, Set<Long> gameIds) {
        TilePane tilePane = new TilePane();
        //TODO: maps of tilespane
        tilePane.setId("tilePane-" + category.getCategoryType());
        tilePane.setPadding(new Insets(20, 10, 30, 0)); // top, right, bottom, left
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        if (isFullScreen) {
            tilePane.setPrefColumns(9);
        } else {
            tilePane.setPrefColumns(5);
        }

        tilePane.setAlignment(Pos.TOP_LEFT);
        tilePane.widthProperty().addListener((obs, oldW, newW) -> {
            log.debug("width" + tilePane.getWidth());
            if (midWidth == 0) {
                midWidth = mainPane.getWidth();
            }


        });
        for (GameApp gameStr : games) {
            if (!gameIds.contains(gameStr.getId())) {
                gameIds.add(gameStr.getId());
                GameTile container = addGame(gameStr);

                tilePane.getChildren().add(container);
            }
            //explain
        }
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(20, 10, 10, 50)); // top, right, bottom, left
        // String expandedSymbol = category.isExpanded() ? "<" : ">";
        HBox blockTitle = new HBox();
        Label label = new Label(category.getLabel());
        label.getStyleClass().add("blockTitle");
        blockTitle.setAlignment(Pos.CENTER_LEFT);
        Button titleButton = new Button();
        ImageView moreReleased = new ImageView();
        moreReleased.setImage(new Image(getClass().getResourceAsStream("/images/plus_rel.png"), 46, 32, false, false));
        ImageView morePressed = new ImageView();
        morePressed.setImage(new Image(getClass().getResourceAsStream("/images/plus_press.png"), 46, 32, false, false));
        ImageView lessReleased = new ImageView();
        lessReleased.setImage(new Image(getClass().getResourceAsStream("/images/less_rel.png"), 46, 32, false, false));
        ImageView lessPressed = new ImageView();
        lessPressed.setImage(new Image(getClass().getResourceAsStream("/images/less_press.png"), 46, 32, false, false));

        titleButton.setGraphic(category.isExpanded() ? lessReleased : moreReleased);
        titleButton.setStyle("-fx-background-color: transparent;");
        titleButton.setOnMousePressed(e -> titleButton.setGraphic(category.isExpanded() ? lessPressed : morePressed));
        titleButton.setOnMouseReleased(e -> titleButton.setGraphic(category.isExpanded() ? lessReleased : moreReleased));
        playSparkles(titleButton, new StackPane(vBox));
        //titleButton.setOnMouseExited(e -> titleButton.setGraphic(normalIcon));
        titleButton.setOnAction(e -> {
            System.out.println("action");
            activateCategory(category, label);
            //  updateSizingImage(plusImage);


        });
        // Ajouter une action au clic
        blockTitle.setOnMouseClicked(event -> {
            //        activateCategory(category, label);
        });
        blockTitle.getChildren().addAll(titleButton, label);
        vBox.getChildren().addAll(blockTitle, tilePane);
        return vBox;
    }

    private void activateCategory(Category category, Label blockTitle) {

        if (category.getCategoryType().equals(CategoryType.GENRE) && "all".equals(category.getId()))
            return;
        if (expandCategory != null && expandCategory.getCategoryType().equals(category.getCategoryType())
                && category.getId().equals(expandCategory.getId())) {
            category.setExpanded(false);
            blockTitle.setText(category.getLabel() + ">");
            expandCategory = null;
        } else if (expandCategory == null || (expandCategory != null && !category.getId().equals(expandCategory.getId()))) {
            expandCategory = category;
            category.setExpanded(true);
            blockTitle.setText(category.getLabel() + "<");
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

        container.setEffect(Effects.getDropShadow2());

        // Création du menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #2c2c2c; -fx-text-fill: white;");
        MenuItem deleteItem = new MenuItem("Supprimer");

        deleteItem.setOnAction(e -> actionDelete(game));

// Ajout des items au menu
        contextMenu.getItems().addAll(deleteItem);

        PauseTransition hoverDelay = new PauseTransition(Duration.millis(600));
        hoverDelay.setOnFinished(e -> {
            Point2D point = caculatePosition(container);
            detailPane.show(game, point.getX(), point.getY(), getScreenRez());
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

                        long duration = gameManager.runGame(game, getScreenRez(), null);
//                            if (duration>0){
//                                gameManager.updateTime(game, returne);
//                            }
                        //  returne = dosBoxManager.runApplication(game.getGameExe(), game, listener);

                        //   returne = dosBoxManager.runApplication(game.getGameExe(), game, null,null );
                    } catch (DosBoxException ex) {
                        throw new RuntimeException(ex);
                    }
                    // gameManager.updateTime(game, returne);
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

    private Point2D caculatePosition1(GameTile container) {
        int detailPanelEstimatedWidth = 550;
        int detailPanelEstimatedHeight = 200;
        Bounds tileSceneBounds = container.localToScene(container.getBoundsInLocal());
        detailPane.applyCss();
        detailPane.layout();
        double fixWidth = mainPane.getWidth() - ORIGINAL_WIDTH > 0 ? (mainPane.getWidth() - 1) / 2 : 0;
        double fixHeight = fixWidth > 0 ? (mainPane.getHeight() - ORIGINAL_HEIGHT) / 2 : 0;

        Bounds screenBounds = container.localToScreen(container.getBoundsInLocal());


        Bounds tileParentBounds = mainPane.sceneToLocal(tileSceneBounds);
        Point2D point = container.getScene().getRoot().sceneToLocal(tileSceneBounds.getMinX(), tileSceneBounds.getMinY());
        //TODO recalculate midroot size
        Point2D fixedPoint2 = new Point2D(tileParentBounds.getMinX() - container.getWidth() - fixWidth - 40, tileParentBounds.getMinY() - container.getHeight() - fixHeight);
        point = point.add(-container.getWidth(), -container.getHeight());
        double diffx1 = (fixedPoint2.getX() + detailPanelEstimatedWidth) - mainPane.getWidth()/*screen.getMaxX()*/;
        double diffx = (point.getX() + detailPanelEstimatedWidth) - mainPane.getWidth()/*screen.getMaxX()*/;
        double diffy = (point.getY() + detailPanelEstimatedHeight) - mainPane.getHeight()/*screen.getMaxX()*/;
        log.debug("tile " + "point " + point.getX() + " / " + point.getY());
        log.debug("tile pt2 " + "point " + fixedPoint2.getX() + " / " + fixedPoint2.getY());
        log.debug("midroot " + +mainPane.getWidth() + " / " + mainPane.getHeight());
        log.debug("tileSceneBounds " + tileSceneBounds.getMinX());

        double decalRatio = -(Screen.getPrimary().getDpi() / 100);

        if (diffx > 0)
            fixedPoint2 = fixedPoint2.add(decalRatio * diffx, 0);

        if (diffy > 0)
            fixedPoint2 = fixedPoint2.add(0, decalRatio * diffy);
        // return fixedPoint2;
        return fixedPoint2;
    }

    private Point2D caculatePosition(GameTile container) {

        if (isFullScreen)
            return caculatePositionMax(container);
        int detailPanelEstimatedWidth = 550;
        int detailPanelEstimatedHeight = 200;
        Bounds tileSceneBounds = container.localToScene(container.getBoundsInLocal());
        detailPane.applyCss();
        detailPane.layout();
        double fixWidth = 0;
        double fixHeight = fixWidth > 0 ? (mainPane.getHeight() - ORIGINAL_HEIGHT) / 2 : 0;

        Bounds tileParentBounds = mainPane.sceneToLocal(tileSceneBounds);
        Point2D point = container.getScene().getRoot().sceneToLocal(tileSceneBounds.getMinX(), tileSceneBounds.getMinY());
        //TODO recalculate midroot size
        Point2D fixedPoint2 = new Point2D(tileParentBounds.getMinX() - container.getWidth() - fixWidth - 40, tileParentBounds.getMinY() - container.getHeight() - fixHeight);
        double diffx1 = (fixedPoint2.getX() + detailPanelEstimatedWidth) - width/*screen.getMaxX()*/;
        double diffx = (point.getX() + detailPanelEstimatedWidth) - mainPane.getWidth()/*screen.getMaxX()*/;
        log.debug("midroot " + +mainPane.getWidth() + "-" + mainPane.getHeight());
        double diffy = (point.getY() + detailPanelEstimatedHeight) - mainPane.getHeight()/*screen.getMaxX()*/;
        log.debug("tile " + "point " + point.getX() + " / " + point.getY());
        log.debug("tile " + "fixedpoint2 " + fixedPoint2.getX() + " / " + fixedPoint2.getY());
        log.debug("tile " + "container " + container.getWidth() + " / " + container.getHeight());
        log.debug("tileSceneBounds min " + tileSceneBounds.getMinX());

        double decalRatio = -(Screen.getPrimary().getDpi() / 100);
        log.debug("dpi:" + Screen.getPrimary().getDpi());
        if (diffx > 0) {

            fixedPoint2 = fixedPoint2.add(decalRatio * diffx, 0);
        }
        if (diffy > 0)
            fixedPoint2 = fixedPoint2.add(0, decalRatio * diffy);
        return fixedPoint2;
    }

    private Point2D caculatePositionMax(GameTile container) {

        int detailPanelEstimatedWidth = 550;
        int detailPanelEstimatedHeight = 200;
        Bounds tileSceneBounds = container.localToScene(container.getBoundsInLocal());
        detailPane.applyCss();
        detailPane.layout();
        double fixWidth = mainPane.getWidth() - ORIGINAL_WIDTH > 0 ? (mainPane.getWidth() - ORIGINAL_WIDTH) / 2 : 0;

        double fixHeight = mainPane.getHeight() - ORIGINAL_HEIGHT > 0 ? (mainPane.getHeight() - ORIGINAL_HEIGHT) / 2 : 0;

        Bounds tileParentBounds = mainPane.sceneToLocal(tileSceneBounds);
        Point2D point = container.getScene().getRoot().sceneToLocal(tileSceneBounds.getMinX(), tileSceneBounds.getMinY());
        //TODO recalculate midroot size
        Point2D fixedPoint2 = new Point2D(tileParentBounds.getMinX() - container.getWidth() - fixWidth - 40, tileParentBounds.getMinY() - container.getHeight() - fixHeight);

        double diffx = (point.getX() + detailPanelEstimatedWidth) - mainPane.getWidth()/*screen.getMaxX()*/;
        double diffy = (point.getY() + detailPanelEstimatedHeight) - mainPane.getHeight()/*screen.getMaxX()*/;

        double decalRatio = -(Screen.getPrimary().getDpi() / 100);
        if (diffx > 0) {
            fixedPoint2 = fixedPoint2.add(decalRatio * diffx, 0);
        }
        if (diffy > 0)
            fixedPoint2 = fixedPoint2.add(0, decalRatio * diffy);
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
                GameApp editedGame = controller.getResult();
                editedGame.setId(gameBean.getId());
                editedGame.merge(gameBean);
                if (!editedGame.equals(gameBean)) {
                    gameManager.save(editedGame);
                }
                updateList();
            }

        } catch (Exception e) {
            log.error("error", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    public void changeView() {
        if (view == TILES_VIEW.DEFAULT) {
            view = TILES_VIEW.YEARS;
            gameCategories = categoryManager.getShownCategories("years");
        } else {
            view = TILES_VIEW.DEFAULT;
            gameCategories = categoryManager.getShownCategories(null);
        }
        updateList();
    }

    private void fadeOverlay(double targetOpacity, int durationMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), backOverlay);
        ft.setToValue(targetOpacity);
        ft.play();
    }

    public void darkenUI() {
        System.out.println("coucou");
        fadeOverlay(0.95, 300); // assombrir
    }

    public void lightenUI() {
        fadeOverlay(0.0, 300); // éclaircir
    }


    private void playSparkles(Button button, Pane layer) {
        //Pane layer = (Pane) button.getParent(); // le parent doit être un Pane ou StackPane

        for (int i = 0; i < 12; i++) {
            Circle sparkle = new Circle(2, Color.WHITE);
            sparkle.setOpacity(0);

            // Position de départ : centre du bouton
            double startX = button.getLayoutX() + button.getWidth() / 2;
            double startY = button.getLayoutY() + button.getHeight() / 2;

            sparkle.setLayoutX(startX);
            sparkle.setLayoutY(startY);

            layer.getChildren().add(sparkle);

            // Destination aléatoire
            double angle = Math.random() * 360;
            double distance = 20 + Math.random() * 20;

            double endX = startX + Math.cos(Math.toRadians(angle)) * distance;
            double endY = startY + Math.sin(Math.toRadians(angle)) * distance;

            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(sparkle.opacityProperty(), 1),
                            new KeyValue(sparkle.scaleXProperty(), 1),
                            new KeyValue(sparkle.scaleYProperty(), 1)
                    ),
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(sparkle.opacityProperty(), 0),
                            new KeyValue(sparkle.scaleXProperty(), 0.1),
                            new KeyValue(sparkle.scaleYProperty(), 0.1),
                            new KeyValue(sparkle.layoutXProperty(), endX),
                            new KeyValue(sparkle.layoutYProperty(), endY)
                    )
            );

            tl.setOnFinished(e -> layer.getChildren().remove(sparkle));
            tl.play();
        }
    }

    private String getScreenRez() {
        Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()).get(0);
        //exclude task bar: Rectangle2D visualBounds = screen.getVisualBounds();
        Rectangle2D bounds = screen.getBounds();
        double scaleX = screen.getOutputScaleX();
        double scaleY = screen.getOutputScaleY();
        double physicalWidth = bounds.getWidth() * scaleX;
        double physicalHeight = bounds.getHeight() * scaleY;
        return (int)bounds.getWidth() + "x" + (int)bounds.getHeight() ;
    }
}

