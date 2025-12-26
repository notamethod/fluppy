package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.game.GameApp;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.nio.file.Path;

public class ImageFactory {
    private static final int  THUMB_WIDTH=150;
    private static final int  THUMB_HEIGHT=200;
    private static final int  MEDIUM_WIDTH=202;
    private static final int  MEDIUM_HEIGHT=270;
    public static StackPane getThumb(GameApp game) {
       return getImage(game, THUMB_WIDTH, THUMB_HEIGHT);
    }
    public static StackPane getMedium(GameApp game) {
        return getImage(game, MEDIUM_WIDTH, MEDIUM_HEIGHT);
    }
    public static StackPane getImage(GameApp game, int width,int height) {
        Path imagePath = game.getImagePath();
        if (imagePath != null) {
            Image image = new Image(imagePath.toUri().toString());
            ImageView imageView = ImageUtils.resize(image,width,height );
            Rectangle clip = new Rectangle(width, height);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            imageView.setClip(clip);
            return new StackPane(imageView);
        } else{
            ImageView imageView = ImageUtils.getNoCoverImageView(width,height);
            Rectangle clip = new Rectangle(width, height);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            imageView.setClip(clip);
            return ImageUtils.getNoCoverGame(game, imageView, width, height);
        }
    }
}
