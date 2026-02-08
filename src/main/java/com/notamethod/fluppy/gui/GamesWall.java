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

import static java.awt.image.ImageObserver.WIDTH;

@Slf4j
public class GamesWall extends Application {

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

    StackPane midRoot;
    GameDetailPanel detailPane;
    List<Node> gamesBlocks = new ArrayList<>();
    VBox content;
    private double xOffset = 0;
    private double yOffset = 0;
    Effects effects;
    private Category expandCategory = null;
    List<Category> gameCategories = new ArrayList<>();
    private double width = ORIGINAL_WIDTH;
    private TILES_VIEW view= TILES_VIEW.DEFAULT;

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

        gameManager = new GameManager(applicationDatabase, preferences);
        categoryManager = new CategoryManager(applicationDatabase);
        detailPane = new GameDetailPanel(dosBoxManager, gameManager);
        gameCategories  = categoryManager.getShownCategories(preferences.getViewFilter());

        FontUtils.loadCustomFont("retro-pixel-arcade.ttf", 8);
        FontUtils.loadCustomFont("MonkeyIsland-1991.ttf", 16);
        FontUtils.loadCustomFont("MonkeyIsland-1990.ttf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-menu-shadow.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-solid.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-outline.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-credits.otf", 16);
        FontUtils.loadCustomFont("lucasarts-scumm-subtitle-roman.otf", 16);
        FontUtils.loadCustomFont("Storyboo.ttf", 16);

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
        ScrollPane scrollPane = createScrollPane();

        // Label overlay
        Label dropLabel = new Label(Messages.getString("drop.here"));
        dropLabel.setTextFill(Color.GRAY);
        dropLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        dropLabel.setVisible(false); // caché par défaut


        VBox root0 = new VBox();
        // Cette ligne est cruciale
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);
        StackPane topRibbon0 = new StackPane(topRibbon, dropLabel);



        midRoot = new StackPane();
        midRoot.setId("midRoot");

        SearchOverlay searchOverlay = new SearchOverlay();

        midRoot.getChildren().addAll(scrollPane, detailPane, searchOverlay);

        root0.getChildren().addAll(/*titleBar, */topRibbon0, midRoot);
        midRoot.setId("realRoot");
        Scene scene = new Scene(root0, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);
        //midRoot.prefWidthProperty().bind(scene.widthProperty());
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
                    log.debug("Width = " + newV+"-"+midWidth+'-'+midRoot.getWidth());
            midWidth=newV.doubleValue();
            midRoot.setPrefWidth(midWidth);
            midRoot.setMaxWidth(midWidth);
            midRoot.setMinWidth(midWidth);
            log.debug("after: Width = " + newV+"-"+midWidth+'-'+midRoot.getWidth());
                 midRoot.requestLayout();
                }

        );

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
        scrollPane.setStyle("-fx-background: #121212;"); // Fond du ScrollPane
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/dosdog2.png")));
        stage.setScene(scene);

        stage.show();
    }

    private ScrollPane createScrollPane() {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setPannable(true); // active le drag à la souris
        //FIXME: problem with fullscreen
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
        updateSizingImage(plusImage, stage.isFullScreen());
        Button plusButton = new Button();
        plusButton.setGraphic(plusImage);
        plusButton.setStyle("-fx-background-color: transparent;");
        plusButton.setOnAction(e -> {
            updateSizingImage(plusImage, !stage.isFullScreen());
            if (stage.isFullScreen()) {

                stage.setFullScreen(false);
                //stage.setMaximized(false);
                stage.setWidth(ORIGINAL_WIDTH);
                stage.setHeight(ORIGINAL_HEIGHT);
                stage.centerOnScreen();
                log.debug("reduce");

                //midRoot.setPrefWidth(800);


                //e.cons

            } else {
                stage.setFullScreen(true);
             //   width = stage.getWidth();
            }
         //   width = stage.getWidth();
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

    private void updateSizingImage(ImageView imageView, boolean isFullscreen) {
        if (isFullscreen) {
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
                    games = gameManager.getFromGenre(category.getId(), count);
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
        tilePane.setId("tilePane-" + category.getCategoryType());
        tilePane.setPadding(new Insets(20, 10, 30, 0)); // top, right, bottom, left
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        //tilePane.setPrefColumns(5);
        if (midWidth>ORIGINAL_WIDTH){
            tilePane.setPrefColumns(9);
        }else{
            tilePane.setPrefColumns(5);
        }
        //tilePane.setPrefColumns(-1);
        tilePane.setAlignment(Pos.TOP_LEFT);
        tilePane.widthProperty().addListener((obs, oldW, newW) -> {
            log.debug("width"+tilePane.getWidth());
            if (midWidth==0){
                midWidth=midRoot.getWidth();
            }


        });
        for (GameApp gameStr : games) {
            if (!gameIds.contains(gameStr.getId())) {
                gameIds.add(gameStr.getId());
                GameTile container = addGame(gameStr);

                tilePane.getChildren().add(container);
            }
        }
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(20, 10, 10, 50)); // top, right, bottom, left
        String expandedSymbol = category.isExpanded() ? "<" : ">";
        Label blockTitle = new Label(expandedSymbol + category.getLabel());
        blockTitle.getStyleClass().add("blockTitle");
        // Ajouter une action au clic
        blockTitle.setOnMouseClicked(event -> {
            activateCategory(category, blockTitle);
        });

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

    private Point2D caculatePosition1(GameTile container) {
        int detailPanelEstimatedWidth = 550;
        int detailPanelEstimatedHeight = 200;
        Bounds tileSceneBounds = container.localToScene(container.getBoundsInLocal());
        detailPane.applyCss();
        detailPane.layout();
        double fixWidth = midRoot.getWidth() - ORIGINAL_WIDTH > 0 ? (midRoot.getWidth() - WIDTH) / 2 : 0;
        double fixHeight = fixWidth > 0 ? (midRoot.getHeight() - ORIGINAL_HEIGHT) / 2 : 0;

        Bounds screenBounds = container.localToScreen(container.getBoundsInLocal());


        Bounds tileParentBounds = midRoot.sceneToLocal(tileSceneBounds);
        Point2D point = container.getScene().getRoot().sceneToLocal(tileSceneBounds.getMinX(), tileSceneBounds.getMinY());
        //TODO recalculate midroot size
        Point2D fixedPoint2 = new Point2D(tileParentBounds.getMinX() - container.getWidth() - fixWidth - 40, tileParentBounds.getMinY() - container.getHeight() - fixHeight);
        point = point.add(-container.getWidth(), -container.getHeight());
        double diffx1 = (fixedPoint2.getX() + detailPanelEstimatedWidth) - width/*screen.getMaxX()*/;
        double diffx = (point.getX() + detailPanelEstimatedWidth) - midRoot.getWidth()/*screen.getMaxX()*/;
        double diffy = (point.getY() + detailPanelEstimatedHeight) - midRoot.getHeight()/*screen.getMaxX()*/;
        log.debug("tile " + "point "+point.getX()+" / "+point.getY());
        log.debug("tile pt2 " + "point "+fixedPoint2.getX()+" / "+fixedPoint2.getY());
        log.debug("midroot " + +midRoot.getWidth()+" / "+midRoot.getHeight());
        log.debug("tileSceneBounds " + tileSceneBounds.getMinX() );

        log.debug("diff:" + diffx);
        log.debug("diffy:" + diffy);
        double decalRatio = -(Screen.getPrimary().getDpi()/100);

        if (diffx > 0)
            fixedPoint2 = fixedPoint2.add(decalRatio * diffx, 0);

        if (diffy> 0)
            fixedPoint2 = fixedPoint2.add(0, decalRatio*diffy);
       // return fixedPoint2;
        return fixedPoint2;
    }

    private Point2D caculatePosition(GameTile container) {
        int detailPanelEstimatedWidth = 550;
        int detailPanelEstimatedHeight = 200;
        Bounds tileSceneBounds = container.localToScene(container.getBoundsInLocal());
        detailPane.applyCss();
        detailPane.layout();
        double fixWidth = midRoot.getWidth() - ORIGINAL_WIDTH > 0 ? (midRoot.getWidth() - WIDTH) / 2 : 0;
        double fixHeight = fixWidth > 0 ? (midRoot.getHeight() - ORIGINAL_HEIGHT) / 2 : 0;

        Bounds screenBounds = container.localToScreen(container.getBoundsInLocal());


        Bounds tileParentBounds = midRoot.sceneToLocal(tileSceneBounds);
        Point2D point = container.getScene().getRoot().sceneToLocal(tileSceneBounds.getMinX(), tileSceneBounds.getMinY());
        //TODO recalculate midroot size
        Point2D fixedPoint2 = new Point2D(tileParentBounds.getMinX() - container.getWidth() - fixWidth - 40, tileParentBounds.getMinY() - container.getHeight() - fixHeight);
        double diffx1 = (fixedPoint2.getX() + detailPanelEstimatedWidth) - width/*screen.getMaxX()*/;
        double diffx = (point.getX() + detailPanelEstimatedWidth) - midRoot.getWidth()/*screen.getMaxX()*/;
        log.debug("tile " + "point "+point.getX()+"-"+point.getY());
        log.debug("midroot " + +midRoot.getWidth()+"-"+midRoot.getHeight());
        double diffy = (point.getY() + detailPanelEstimatedHeight) - midRoot.getHeight()/*screen.getMaxX()*/;
        log.debug("tile " + "point "+point.getX()+" / "+point.getY());
        log.debug("midroot " + +midRoot.getWidth()+" / "+midRoot.getHeight());
        log.debug("tileSceneBounds " + tileSceneBounds.getMinX() );

        log.debug("diff:" + diffx);
        log.debug("diffy:" + diffy);
        double decalRatio = -(Screen.getPrimary().getDpi()/100);
        log.debug("dpi:"+Screen.getPrimary().getDpi());
        if (diffx > 0)
            fixedPoint2 = fixedPoint2.add(decalRatio * diffx, 0);
        if (diffy> 0)
            fixedPoint2 = fixedPoint2.add(0, decalRatio*diffy);
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

    public void changeView(){
        if (view==TILES_VIEW.DEFAULT){
            view=TILES_VIEW.YEARS;
            gameCategories=categoryManager.getShownCategories("years");
        }else{
            view=TILES_VIEW.DEFAULT;
            gameCategories=categoryManager.getShownCategories(null);
        }
        updateList();
    }
}

