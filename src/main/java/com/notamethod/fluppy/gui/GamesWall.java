package com.notamethod.fluppy.gui;


import com.notamethod.fluppy.core.ApplicationDatabase;
import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.category.Category;
import com.notamethod.fluppy.core.category.CategoryManager;
import com.notamethod.fluppy.core.category.CategoryType;
import com.notamethod.fluppy.core.game.GameAlreadyPresentException;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameManager;
import com.notamethod.fluppy.core.game.Statistics;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.gui.common.DialogActionsJfx;
import com.notamethod.fluppy.gui.common.GameActions;
import com.notamethod.fluppy.platform.EmuManagerFactory;
import com.notamethod.fluppy.platform.GameHandlerFactory;
import com.notamethod.fluppy.platform.dosbox.EmulatorException;
import com.notamethod.fluppy.util.HelperClass;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class GamesWall extends Application {

    static {
        String logPath = Configuration.logFolder;
        System.setProperty("LOG_FILE_PATH", logPath+"/app.log");
    }
    private static final Logger log = LoggerFactory.getLogger(GamesWall.class);
    private Stage stage;

    public enum TILES_VIEW {
        DEFAULT, YEARS, COMPANY
    }

    private static final int ORIGINAL_WIDTH = 900;
    private static final int ORIGINAL_HEIGHT = 700;
    private static final int ROW_SIZE = 9;
    double midWidth;
    ApplicationDatabase applicationDatabase;
    PreferencesBean preferences;
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
    private GameTile hoveredTile = null;
    LongProperty gameCounter = new SimpleLongProperty(0);
    StringProperty timePlayedProperty = new SimpleStringProperty("");
    private FFmpegVideoPlayer videoPlayer;

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

        String igdbUser = System.getenv("IGDB_USER");
        String igdbToken = System.getenv("IGDB_TOKEN");
        if (igdbUser!=null && !igdbUser.isEmpty()){
            log.info("found IGDB credentials: user API");
        }
        if (igdbToken!=null && !igdbToken.isEmpty()){
            log.info("found IGDB credentials: token API");
        }
        gameManager = new GameManager(applicationDatabase, preferences,
                EmuManagerFactory.createDefault(null),
                GameHandlerFactory.createDefault(null));
        categoryManager = new CategoryManager(applicationDatabase);
        detailPane = new GameDetailPanel(gameManager);
        gameCategories = categoryManager.getShownCategories(TILES_VIEW.DEFAULT);
        LinkedHashMap imageCache = new LinkedHashMap() {

            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > 100; // garde max 100 images en mémoire
            }
        };

        FontUtils.loadCustomFonts();
    }

    @Override
    public void start(Stage stage) throws MalformedURLException, URISyntaxException {

        stage.initStyle(StageStyle.UNDECORATED);
        this.stage = stage;
        HBox topRibbon = createTopRibbon(stage);
        Animation bordureAnim = effects.getBordureAnim(topRibbon);
        if (preferences.isVideoBackground()) {
            videoPlayer = new FFmpegVideoPlayer(getVideo(), 30, preferences.getFfmpegPath());
        }
        content = new VBox();
        Statistics  stats = gameManager.getStatistics();
        gameCounter.set(stats.count());

        timePlayedProperty.set(Statistics.getTimePlayed(stats.timeplayed(), true));

        if (gameCounter.get() > 0) {
            updateList();
        }

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

        detailPane.hoverProperty().addListener((obs, wasHover, isHover) -> {
            if (!isHover && (hoveredTile == null || !hoveredTile.isHover())) {
                detailPane.hide();
            }
        });
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
        if (preferences.isVideoBackground()){
            mainPane.getChildren().add(videoPlayer.getImageView());
            videoPlayer.getImageView().setOpacity(0.3);
            videoPlayer.getImageView().setStyle("-fx-background-color: red;");
        }

        SearchOverlay searchOverlay = new SearchOverlay();

        mainPane.getChildren().addAll(scrollPane, detailPane, searchOverlay);

        root.getChildren().addAll(/*titleBar, */topRibbon0, mainPane);



        backOverlay = initBackOverlay();
        backOverlay.prefWidthProperty().bind(root.widthProperty());
        backOverlay.prefHeightProperty().bind(root.heightProperty());
        mainPane.getChildren().add(backOverlay);

        Scene scene = new Scene(root, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);

        // Démarrage de la vidéo
        if (preferences.isVideoBackground())
            videoPlayer.start(ORIGINAL_WIDTH, ORIGINAL_HEIGHT);

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
            if (preferences.isVideoBackground())
                videoPlayer.scheduleResize((int) newV.doubleValue(), (int) scene.getHeight());
        });
//
        scene.heightProperty().addListener((obs, oldV, newV) ->{
            log.trace("Height = " + newV);
            if (preferences.isVideoBackground())
                videoPlayer.scheduleResize((int) scene.getWidth(), (int) newV.doubleValue());
    } );

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
        scene.setFill(Color.TRANSPARENT);           // fond de scène transparent
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
       // scrollPane.setStyle("-fx-background: #1A1E2E;"); // Fond du ScrollPane
        stage.initStyle(StageStyle.TRANSPARENT);
        mainPane.getStyleClass().add("main-pane");

        scrollPane.getStyleClass().add("scroll-pane");
       // scrollPane.getStyleClass().add("main-pane");
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setStyle("-fx-background: #1A1E2E;"); // Fond du ScrollPane

// Et son contenu (le viewport interne de ScrollPane)
        scrollPane.getContent().setStyle("-fx-background-color: transparent;");
        EventBus.subscribe("highlight-ribbon", () -> {
            System.out.println("A notifié via EventBus !");
            bordureAnim.play();
            dropLabel.setVisible(true);
            topRibbon.getStyleClass().add("ribbon-highlight");
        });

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/dosdog2.png")));
        stage.setScene(scene);

        stage.show();
        if (gameCounter.get() == 0) {
            Story story = new Story(stage, "firstrun", preferences);
            story.start();
        }
    }

    private String getVideo() {
        Path path = Paths.get(Configuration.videoFolder, "video0001.mp4");
        return path.toString();
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

        Button companyButton = createRibbonButton("/images/company1.png");
        companyButton.setOnAction(e -> changeView(TILES_VIEW.COMPANY));

        Button calendarButton = createRibbonButton("/images/calendar1.png");
        calendarButton.setOnAction(e -> changeView(TILES_VIEW.YEARS));
        VBox globalInfos = new VBox();
        Label param1 = new Label();
        String pattern = Messages.getString("game.count"); // "%d parties"
        param1.textProperty().bind(gameCounter.asString(pattern));
        Label param2 = new Label();
        param2.textProperty().bind(Bindings.concat("", timePlayedProperty, ""));
        globalInfos.getChildren().addAll(param1,param2);
        Region spacerRibbon = new Region();
        HBox.setHgrow(spacerRibbon, Priority.ALWAYS);
        topRibbon.setAlignment(Pos.CENTER_LEFT);
        topRibbon.getChildren().addAll(logoView, titleView, info, globalInfos, spacerRibbon, calendarButton, companyButton, gearButton, plusButton, quitButton);

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
        GameActions gameActions = new GameActions(new DialogActionsJfx(), gameManager.getHandlers());
        List<GameApp> gameApps = gameActions.createFromFiles(files);
        List<String> errors = new ArrayList<>();
        int added = 0;
        if (gameApps.size() == 1) {
            try {
                gameManager.addGame(gameApps.getFirst());
                added++;
            } catch (GameAlreadyPresentException e) {

                String returnValue=gameActions.doSameGame(gameApps.getFirst(), gameManager);
                if (returnValue==null) {
                    errors.add(e.getLocalizedMessage() + ": " + gameApps.getFirst().getGamePath());
                    log.error("import error", e);
                }

            } catch (IOException e) {
                errors.add(e.getLocalizedMessage() + ": " + gameApps.getFirst().getGamePath());
                log.error("import error", e);
            }
        } else {
            for (GameApp gameApp : gameApps) {
                try {
                    gameManager.addGame(gameApp);
                    added++;
                } catch (GameAlreadyPresentException | IOException e) {
                    errors.add(e.getLocalizedMessage() + ": " + gameApp.getGamePath());
                    log.error("import error", e);
                }
            }
        }
        if (!errors.isEmpty()) {
            gameActions.showErrors(errors);
        }

        updateList();
        if (added > 0) {
            EventBus.publish("game-added");
        }
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
                    category.setCount(gameManager.countFromGenre(category.getId(), gameIds));
                    if (category.getCount() > count)
                        category.setHasMore(true);
                    games = gameManager.getFromGenre(category.getId(), count, gameIds);
                }
                break;
            case YEAR:
                games = gameManager.getFromYear(Integer.valueOf(category.getId()), count);
                break;
            case COMPANY:
                games = gameManager.getFromPublisher(category.getId(), count);
                break;
            default:
                games = null;
        }
        if (games != null && !games.isEmpty()) {
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
        String labelBloc = category.getLabel();
        if (category.isExpanded())
            labelBloc = labelBloc + "<";
        if (!category.isExpanded() && category.isHasMore())
            labelBloc = labelBloc + ">>";
        Label label = new Label(labelBloc);
        label.getStyleClass().add("blockTitle");
        blockTitle.setAlignment(Pos.CENTER_LEFT);
        // Ajouter une action au clic
        blockTitle.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                editCategory(category, label);
            } else {
                activateCategory(category, label);
            }
        });
        if (category.getImage() != null) {
            int maxHeight = 120;
            Image image = category.getImage();
            ImageView iv = new ImageView(image);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            if (image.getHeight() > maxHeight) {
                iv.setFitHeight(maxHeight);
            }
            //iv.setFitHeight(100);
            blockTitle.getChildren().addAll(iv);
        } else {
            blockTitle.getChildren().addAll(label);
        }
        vBox.getChildren().addAll(blockTitle, tilePane);
        return vBox;
    }

    private void activateCategory(Category category, Label title) {

        if (category.getCategoryType().equals(CategoryType.GENRE) && "all".equals(category.getId()))
            return;
        if (expandCategory != null && expandCategory.getCategoryType().equals(category.getCategoryType())
                && category.getId().equals(expandCategory.getId())) {
            category.setExpanded(false);
            title.setText(category.getLabel() + ">");
            expandCategory = null;
        } else if (expandCategory == null || (expandCategory != null && !category.getId().equals(expandCategory.getId()))) {
            expandCategory = category;
            category.setExpanded(true);
            title.setText(category.getLabel() + "<");
        } else {
            expandCategory = null;
        }
        updateList();
    }

    private void editCategory(Category category, Label blockTitle) {

        if (category.getCategoryType().equals(CategoryType.COMPANY)) {
            CompanyEditorView companyEditorView = new CompanyEditorView();
            companyEditorView.setGameManager(gameManager);
            companyEditorView.setCompany(category.getId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setDialogPane(companyEditorView);
            dialog.showAndWait();

            Boolean editedCompany = companyEditorView.getResult();
            if (editedCompany) {
                //update
            }


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
            hoveredTile = container;
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
                //NOTHING
            } else if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 1) {
                    long returne = 0;
                    log.debug(game.getName());
                    try {

                        long duration = gameManager.runGame(game, getScreenRez(), null);
//                            if (duration>0){
//                                gameManager.updateTime(game, returne);
//                            }
                        //  returne = dosBoxManager.runApplication(game.getGameExe(), game, listener);

                        //   returne = dosBoxManager.runApplication(game.getGameExe(), game, null,null );
                    } catch (EmulatorException ex) {
                        throw new RuntimeException(ex);
                    }
                    // gameManager.updateTime(game, returne);
                }
            }
        });

//        WeakReference<GameTile> ref = new WeakReference<>(container);
//        ChangeListener<Boolean> hoverListener = new WeakChangeListener<>((obs, wasHover, isHover) -> {
//            GameTile c = ref.get();
//            if (c != null && !isHover && !c.isHover()) {
//                detailPane.hide();
//            }
//        });
//        detailPane.hoverProperty().addListener(hoverListener);
        return container;
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
        double diffx = (point.getX() + detailPanelEstimatedWidth) - mainPane.getWidth()/*screen.getMaxX()*/;
        log.trace("midroot " + +mainPane.getWidth() + "-" + mainPane.getHeight());
        double diffy = (point.getY() + detailPanelEstimatedHeight) - mainPane.getHeight()/*screen.getMaxX()*/;
        log.trace("tile " + "point " + point.getX() + " / " + point.getY());
        log.trace("tile " + "fixedpoint2 " + fixedPoint2.getX() + " / " + fixedPoint2.getY());
        log.trace("tile " + "container " + container.getWidth() + " / " + container.getHeight());
        log.trace("tileSceneBounds min " + tileSceneBounds.getMinX());

        double decalRatio = -(Screen.getPrimary().getDpi() / 100);
        log.trace("dpi:" + Screen.getPrimary().getDpi());
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

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
                log.error("Fluppy Uncaught [{}]", t.getName(), e));
        launch(args);

    }

    public void changeView(TILES_VIEW change) {
        if (view == TILES_VIEW.DEFAULT) {
            view = change;
            gameCategories = categoryManager.getShownCategories(change);
        } else {
            if (view == change) {
                view = TILES_VIEW.DEFAULT;
                gameCategories = categoryManager.getShownCategories(null);
            } else {
                view = change;
                gameCategories = categoryManager.getShownCategories(change);
            }
        }
        updateList();
    }

    private void fadeOverlay(double targetOpacity, int durationMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), backOverlay);
        ft.setToValue(targetOpacity);
        ft.play();
    }

    public void darkenUI() {
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
        Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()).getFirst();
        //exclude task bar: Rectangle2D visualBounds = screen.getVisualBounds();
        Rectangle2D bounds = screen.getBounds();
        double scaleX = screen.getOutputScaleX();
        double scaleY = screen.getOutputScaleY();
        double physicalWidth = bounds.getWidth() * scaleX;
        double physicalHeight = bounds.getHeight() * scaleY;
        return (int) bounds.getWidth() + "x" + (int) bounds.getHeight();
    }

    @Override
    public void stop() {
        // Arrêt propre délégué au player
        if (videoPlayer != null) {
            videoPlayer.stop();
        }
    }
}

