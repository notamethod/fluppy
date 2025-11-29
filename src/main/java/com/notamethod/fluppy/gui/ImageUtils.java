package com.notamethod.fluppy.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


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
        imageView.setFitWidth(150);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(preserveRatio);
        return imageView;
    }
    public static ImageView resize(Image image, int fitHeight,int fitWidth) {
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
}
