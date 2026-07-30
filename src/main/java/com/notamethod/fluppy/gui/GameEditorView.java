package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.api.ApiCalls;
import com.notamethod.fluppy.api.ApiException;
import com.notamethod.fluppy.api.MappingException;
import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameManager;
import com.notamethod.fluppy.core.game.GameMapper;
import com.notamethod.fluppy.gui.common.DialogActionsJfx;
import com.notamethod.fluppy.gui.common.GameActions;
import com.notamethod.fluppy.util.FileType;
import com.notamethod.fluppy.util.HelperClass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GameEditorView extends DialogPane {


    // --- Champs UI ---
    private final TextField titleField = new TextField();
    private final TextField genreField = new TextField();
    private final TextField publisherField = new TextField();
    private final TextField yearField = new TextField();
    private final TextField ratingField = new TextField();
    private final CheckBox favoriteField = new CheckBox();
    private final CheckBox nsfwField = new CheckBox();
    private final TextArea commentField = new TextArea();
    private final ImageView imageGame = new ImageView();
    private final ImageView imagePublisher = new ImageView();
    private final Label cyclesLabel = new Label("Valeur : auto");
    private final Slider cyclesSpinner = new Slider(0, 3000, 0);
    private final ComboBox<String> comboMachines = new ComboBox<>();
    private final TextField exeFile = new TextField();
    private final Button exeButton = new Button("...");
    private final TextField protectionPathField = new TextField();
    private final TextField manualPathField = new TextField();
    private final TextField diskInfo = new TextField();
    private final ListView<String> extraDisks = new ListView();
    private final VBox dropZone = new VBox();
    private final Button okButton = new Button("OK");
    private final Button cancelButton = new Button("Annuler");
    private final Button syncButton = new Button("sync");

    // --- État métier ---
    private GameApp originalGame;
    private GameApp editedGame;
    private GameApp result;
    private GameManager gameManager;
    private Path exePath;
    private Path manualPath;
    private Path protectionPath;
    private final DialogActionsJfx da = new DialogActionsJfx();
    private boolean isChanged=false;
    private static final ObservableList diskList =
            FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    public GameEditorView() {
        getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
        getStyleClass().add("nightwish");
        setContent(buildContent());
        initControls();
        initDropZone();
    }

    // -------------------------------------------------------------------------
    // Construction de l'UI
    // -------------------------------------------------------------------------

    private VBox buildContent() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(buildInfoTab(), buildLocalTab());

        HBox buttonBar = new HBox(10, okButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10));

        return new VBox(10, tabPane, buttonBar);
    }

    private Tab buildInfoTab() {
        Tab tab = new Tab("Informations du jeu");
        tab.setClosable(false);

        // Grille champs texte
        GridPane fieldsGrid = new GridPane();
        fieldsGrid.setHgap(10);
        fieldsGrid.setVgap(10);
        fieldsGrid.setPadding(new Insets(20));
        addRow(fieldsGrid, 0, "Titre:", titleField);
        addRow(fieldsGrid, 1, "Genre:", genreField);
        addRow(fieldsGrid, 2, "Editeur:", publisherField);
        addRow(fieldsGrid, 3, "Année de sortie:", yearField);
        addRow(fieldsGrid, 4, "Note:", ratingField);
        addRow(fieldsGrid, 5, "Favorite:", favoriteField);
        addRow(fieldsGrid, 6, "NSFW:", nsfwField);
        addRow(fieldsGrid, 7, "Comment:", commentField);

        // Image + sync
        imageGame.setFitHeight(150);
        imageGame.setFitWidth(200);
        imageGame.setPickOnBounds(true);
        imageGame.setPreserveRatio(true);
        imagePublisher.setFitHeight(150);
        imagePublisher.setFitWidth(200);
        imagePublisher.setPickOnBounds(true);
        imagePublisher.setPreserveRatio(true);

        GridPane imageGrid = new GridPane();

        imageGrid.setHgap(10);
        imageGrid.setVgap(10);
        imageGrid.setPadding(new Insets(20));
        imageGrid.add(imageGame, 0, 0);
        imageGrid.add(syncButton, 0, 1);
        imageGrid.add(imagePublisher, 0, 2);
        // Grille principale
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(15);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(20));
        mainGrid.add(fieldsGrid, 0, 0);
        mainGrid.add(imageGrid, 1, 0);

        // Slider cycles
        cyclesSpinner.setBlockIncrement(100);
        cyclesSpinner.setMajorTickUnit(100);
        cyclesSpinner.setMinorTickCount(0);
        cyclesSpinner.setShowTickMarks(true);
        cyclesSpinner.setSnapToTicks(true);

        Label cyclesTitle = new Label("Cycles");
        cyclesTitle.setPadding(new Insets(0, 0, 0, 20));
        cyclesLabel.setPadding(new Insets(0, 0, 0, 20));
        Label machineLabel = new Label("Machine:");
        machineLabel.setPadding(new Insets(20, 0, 0, 20));

        GridPane cyclesGrid = new GridPane();
        cyclesGrid.setPadding(new Insets(20));
        ColumnConstraints col40 = new ColumnConstraints();
        col40.setPercentWidth(40);
        ColumnConstraints col50 = new ColumnConstraints();
        col50.setPercentWidth(50);
        cyclesGrid.getColumnConstraints().addAll(col40, col50);
        cyclesGrid.add(cyclesTitle, 0, 0);
        cyclesGrid.add(cyclesLabel, 1, 0);
        cyclesGrid.add(cyclesSpinner, 1, 1);
        cyclesGrid.add(machineLabel, 0, 2);
        cyclesGrid.add(comboMachines, 1, 2);

        tab.setContent(new VBox(mainGrid, cyclesGrid));
        return tab;
    }

    private Tab buildLocalTab() {
        Tab tab = new Tab("Informations locales");
        tab.setClosable(false);
        exeFile.setPrefWidth(460);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 20, 0));
        grid.add(new Label("Executable:"), 0, 0);
        grid.add(exeFile, 1, 0);
        grid.add(exeButton, 2, 0);
        grid.add(new Label("Protection:"), 0, 2);
        grid.add(protectionPathField, 1, 2);
        grid.add(new Label("Manual:"), 0, 3);
        grid.add(manualPathField, 1, 3);
        grid.add(new Label("Disk:"), 0, 4);
        grid.add(diskInfo, 1, 4);
        grid.add(new Label("Extra disks:"), 0, 5);
        grid.add(extraDisks, 1, 5);
        extraDisks.setItems(diskList);
        extraDisks.setPrefWidth(300);
        extraDisks.setPrefHeight(70);
        dropZone.getStyleClass().add("dropZone");

        tab.setContent(new VBox(grid, dropZone));
        return tab;
    }

    // -------------------------------------------------------------------------
    // Initialisation contrôles & actions
    // -------------------------------------------------------------------------

    private void initControls() {
        cyclesSpinner.valueProperty().addListener((obs, oldVal, newVal) ->
                cyclesLabel.setText(newVal.equals(0.0) ? "Valeur : auto" : "Valeur : " + newVal.intValue())
        );

        comboMachines.setItems(FXCollections.observableArrayList(
                "hercules", "cga", "ega", "pcjr", "tandy", "svga_s3","amstrad"
        ));
        comboMachines.setValue("svga_s3");

        okButton.setOnAction(e -> {
            saveGame();
            closeDialog();
        });
        cancelButton.setOnAction(e -> {
            result = null;
            closeDialog();
        });
        syncButton.setOnAction(e -> updateAPI());
        exeButton.setOnAction(e -> selectExe());

    }

    private void initDropZone() {
        dropZone.setPrefSize(300, 150);
        dropZone.setAlignment(Pos.CENTER);
        styleDropZone(false);

        Label label = new Label("Déposez un fichier ici");
        dropZone.getChildren().add(label);

        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles())
                event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });
        dropZone.setOnDragEntered(event -> {
            styleDropZone(true);
            event.consume();
        });
        dropZone.setOnDragExited(event -> {
            styleDropZone(false);
            event.consume();
        });
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                log.debug("Dropped file : {}", file.getAbsolutePath());
                label.setText("Fichier : " + file.getName());
                try {
                    addExtra(file, originalGame);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void styleDropZone(boolean active) {
        dropZone.setStyle(active ? """
                    -fx-border-color: #4CAF50;
                    -fx-border-width: 2;
                    -fx-border-style: solid;
                    -fx-background-color: rgba(76,175,80,0.1);
                """ : """
                    -fx-border-color: #888;
                    -fx-border-width: 2;
                    -fx-border-style: dashed;
                    -fx-background-color: rgba(255,255,255,0.05);
                """);
    }

    // -------------------------------------------------------------------------
    // Logique métier
    // -------------------------------------------------------------------------

    private void closeDialog() {
        ((Stage) getScene().getWindow()).close();
    }

    private Path addExtra(File file, GameApp gameApp) throws IOException {
        List<String> choices = gameManager.getExtraList(file, gameApp).stream().map(x -> x.getBaseType().toString()).toList();

        Optional<String> chosen = da.showListInputDialog(
                "Choose import type",
                "What is the title of the application? Select one of the proposals,\n" +
                        "or select \"Something else...\" to type your own.",
                choices.toArray(String[]::new), choices.get(0)
        );
        if (chosen.isEmpty()) return null;

        String choice = chosen.get();
        String extraFilename = "extra_" + HelperClass.sanitizeName(gameApp.getName()) + "_" + choice + "." + HelperClass.getExtension(file);
        Path outputPath = Paths.get(Configuration.extraFolder);
        Files.createDirectories(outputPath);
        Path target = outputPath.resolve(extraFilename);
        Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

        if (choice.equals(FileType.BASE_TYPE.PROTECTION.toString())) {
            protectionPath = target;
            protectionPathField.setText(target.toFile().getCanonicalPath());
        } else if (choice.equals(FileType.BASE_TYPE.MANUAL.toString())) {
            manualPath = target;
            manualPathField.setText(target.toFile().getCanonicalPath());
        } else if (choice.equals(FileType.BASE_TYPE.COVER.toString())) {
            updateImage(target.toFile(), choice);
        }
        return target;
    }


    public void saveGame() {
        editedGame.setName(titleField.getText());
        editedGame.setYear(Integer.parseInt(yearField.getText()));
        editedGame.setExePath(exePath);
        editedGame.setGameExe(exeFile.getText());
        editedGame.setCycles((int) cyclesSpinner.getValue());
        editedGame.setMachine(comboMachines.getValue());
        editedGame.setAgeRating(nsfwField.isSelected() ? 1 : 0);
        editedGame.setFavorite(favoriteField.isSelected());
        editedGame.setProtectionPath(protectionPath);
        editedGame.setManualPath(manualPath);
        editedGame.setComment(commentField.getText());
        editedGame.setId(originalGame.getId());


        if (!editedGame.equals(originalGame) || isChanged) {
            gameManager.save(editedGame);
            result = editedGame;
            log.debug("game saved");
        } else {
            log.debug("game unchanged: not saving");
            result = null;
        }
    }

    public void setGame(GameApp gameLight) {
        GameApp game = gameManager.loadFullGame(gameLight.getId());
        this.originalGame = game;

        commentField.setWrapText(true);
        commentField.setText(game.getComment());
        titleField.setText(game.getName());
        if (game.getPublisher() != null)
            publisherField.setText(game.getPublisher().getName());
        yearField.setText(game.getYear() != null ? String.valueOf(game.getYear()) : "?");
        exePath = game.getExePath();

        if (game.getCoverImage()!= null) {
            try {
                imageGame.setImage(ImageUtils.buildImageFromBytes(game.getCoverImage()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        if (game.getPublisher() != null && game.getPublisher().getImage() != null) {
            imagePublisher.setImage(ImageUtils.buildImageFromBytes(game.getPublisher().getImage()));
        }
        nsfwField.setSelected(game.getAgeRating() > 0);
        favoriteField.setSelected(game.isFavorite());
        cyclesSpinner.setValue(game.getCycles());
        genreField.setText(game.getGenres().stream().map(x -> x.getName()).collect(Collectors.joining(",")));
        exeFile.setText(game.getGameExe());
        comboMachines.setValue(game.getMachine());
        protectionPath = game.getProtectionPath();
        if (game.getProtectionPath() != null)
            protectionPathField.setText(game.getProtectionPath().toFile().getAbsolutePath());
        if (game.getManualPath() != null)
            manualPathField.setText(game.getManualPath().toFile().getAbsolutePath());
        diskInfo.setText(String.valueOf(1 + game.toExtraDiskList().size()));
        diskList.addAll(game.toExtraDiskList().values());
        editedGame = GameMapper.INSTANCE.copyGameApp(originalGame);
    }

    public boolean updateAPI() {
        ApiCalls apiCalls = new ApiCalls();
        GameActions gameActions = new GameActions(new DialogActionsJfx(), gameManager.getHandlers());
        List<GameApiBean> games;
        try {
            games = apiCalls.findGame(titleField.getText());
        } catch (MappingException e) {
            log.error("Internal Error", e);
            return false;
        } catch (ApiException e) {
            log.error("Internal Error", e);
            DialogActionsJfx.showErrorDialog(e.getLocalizedMessage());
            return false;
        }

        if (games.isEmpty()) {
            da.showMessageDialog("warning", "Can't update: no game found");
            return false;
        }

        GameApiBean game = games.size() > 1 ? gameActions.chooseGame(games) : games.get(0);
        if (game == null) {
            log.warn("game not found");
            return false;
        }

        try {
            apiCalls.findAndUpdateData(editedGame, 2, game);
            return true;
        } catch (ApiException | MappingException | IOException e) {
            log.error("Internal Error", e);
            return false;
        }
    }

    private void updateImage(File file, String choice) {
        try (InputStream is = new FileInputStream(file)) {
            byte[] image = is.readAllBytes();
            imageGame.setImage(ImageUtils.buildImageFromBytes(image));
            editedGame.setCoverImage(image);
            editedGame.setEdition(1L);
            isChanged=true;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void selectExe() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable", "*.exe", "*.bat", "*.com"));
        fileChooser.setInitialDirectory(exePath.toFile());
        fileChooser.setTitle("Choisir un fichier");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null && selectedFile.isFile()) {
            exeFile.setText(selectedFile.getAbsoluteFile().getName());
            exePath = selectedFile.getAbsoluteFile().getParentFile().toPath();
        }
    }



    public GameApp getResult() {
        return result;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // -------------------------------------------------------------------------
    // Utilitaire
    // -------------------------------------------------------------------------

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
    }
}
