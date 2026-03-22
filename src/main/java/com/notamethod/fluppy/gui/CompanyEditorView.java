package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.game.CompanyEntity;
import com.notamethod.fluppy.core.game.GameManager;
import com.notamethod.fluppy.gui.common.DialogActionsJfx;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class CompanyEditorView extends DialogPane {



    // --- Champs UI ---
    private final TextField titleField = new TextField();

    private final ImageView imagePublisher = new ImageView();

    private final TextField nameField = new TextField();

    private final VBox dropZone = new VBox();
    private final Button okButton = new Button("OK");
    private final Button cancelButton = new Button("Annuler");

    private boolean result =false;

    // --- État métier ---

    private CompanyEntity editedCompany;

    private GameManager gameManager;

    private final DialogActionsJfx da = new DialogActionsJfx();
    private boolean isChanged=false;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    public CompanyEditorView() {
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

        StackPane pane = new StackPane();
        this.setContent(pane);


        pane.getChildren().addAll(buildInfoTab());

        HBox buttonBar = new HBox(10, okButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10));

        return new VBox(10, pane, buttonBar);
    }

    private VBox buildInfoTab() {
        imagePublisher.setFitHeight(150);
        imagePublisher.setFitWidth(200);
        imagePublisher.setPickOnBounds(true);
        imagePublisher.setPreserveRatio(true);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 20, 0));
        addRow(grid, 0, "name", nameField);
        addRow(grid, 1, "image", imagePublisher);

        dropZone.getStyleClass().add("dropZone");

        VBox vbox= new VBox(grid, dropZone);
        return vbox;
    }



    // -------------------------------------------------------------------------
    // Initialisation contrôles & actions
    // -------------------------------------------------------------------------

    private void initControls() {




        okButton.setOnAction(e -> {
            saveCompany();
            closeDialog();
        });
        cancelButton.setOnAction(e -> {
            //result = null;
            closeDialog();
        });


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

                    updateImage(file, "originalGame.getName()");

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




    public void saveCompany() {
        if (editedCompany.getImage()!=null && isChanged) {
            CompanyEntity company = gameManager.loadCompany(editedCompany.getId());
            company.setImage(editedCompany.getImage());
            gameManager.save(company);
            company.setEdition(1L);
            result=true;
        }



    }

    public void setCompany(String id) {
        CompanyEntity company = gameManager.loadCompany(id);
        this.editedCompany = company;
        this.nameField.setText(company.getName());
        if (company.getImage()!=null) {
            this.imagePublisher.setImage(ImageUtils.buildImageFromBytes(company.getImage()));
        }

    }



    private void updateImage(File f, String choice) {
        try (InputStream is = new FileInputStream(f)) {

                byte[] image = is.readAllBytes();
                imagePublisher.setImage(ImageUtils.buildImageFromBytes(image));
                editedCompany.setImage(image);
                ImageUtils.testImage(image);
                isChanged=true;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }






    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public Boolean getResult() {
        return result;
    }

    public static String hash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);

        // Convertir en chaîne hexadécimale
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Utilitaire
    // -------------------------------------------------------------------------

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
    }
}
