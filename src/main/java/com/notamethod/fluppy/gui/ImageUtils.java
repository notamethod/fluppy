package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.game.GameApp;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URISyntaxException;
import java.net.URL;


public class ImageUtils {

    static double STANDARD_WIDTH=150;
    static double STANDARD_HEIGHT=200;
    static double STANDARD_RATIO=STANDARD_WIDTH/STANDARD_HEIGHT;
    static double tolerance=.12;
    public static ImageView resize(Image image) {
        double height=image.getHeight();
        double width=image.getWidth();
        boolean preserveRatio=true;

        double minRatio=STANDARD_RATIO-(STANDARD_RATIO*tolerance);
        double maxRatio=STANDARD_RATIO+(STANDARD_RATIO*tolerance);
        double ratio=width/height;
        if (ratio<maxRatio && ratio>minRatio){
            preserveRatio=false;
        }
        ImageView imageView  = new ImageView(image);
        imageView.setFitWidth(STANDARD_WIDTH);
        imageView.setFitHeight(STANDARD_HEIGHT);
        imageView.setPreserveRatio(preserveRatio);
        return imageView;
    }
    public static ImageView resize(Image image, int fitWidth, int fitHeight) {
        double height=image.getHeight();
        double width=image.getWidth();
        boolean preserveRatio=true;

        double minRatio=STANDARD_RATIO-(STANDARD_RATIO*tolerance);
        double maxRatio=STANDARD_RATIO+(STANDARD_RATIO*tolerance);
        double ratio=width/height;
        if (ratio<maxRatio && ratio>minRatio){
            preserveRatio=false;
        }
        ImageView imageView  = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        imageView.setPreserveRatio(preserveRatio);
        return imageView;
    }

    public static StackPane getNoCoverGame(GameApp game, ImageView imageView, int fitWidth,int fitHeight) {

        // Créer le texte
        Label label = new Label(game.getName()+"\n"+game.getYear());

        label.setStyle("-fx-text-fill: white; -fx-font-size: 8px; -fx-background-color: rgba(0,0,0,0.5);");

        // Empiler l'image et le texte
        StackPane stackPane = new StackPane();
        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
        label.maxWidthProperty().bind(stackPane.widthProperty());
        label.setWrapText(true);
        stackPane.getChildren().addAll(imageView, label);
        return stackPane;
    }

    public static ImageView getNoCoverImageView( int fitWidth,int fitHeight) {
        ClassLoader classLoader = GamesWall.class.getClassLoader();
        URL unknownGame = classLoader.getResource("unknown.jpg");
        ImageView imageView = null;

        try {
            Image image = new Image(unknownGame.toURI().toString());
            imageView = new ImageView(image);
            imageView.resize(fitWidth, fitHeight);
            return imageView;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }
}
