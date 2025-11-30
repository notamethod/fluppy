package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.GameApp;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.nio.file.Path;

public class ImageFactory {
    private static final int  THUMB_WIDTH=150;
    private static final int  THUMB_HEIGHT=200;
    public static StackPane getThumb(GameApp game) {
        Path imagePath = game.getImagePath();
        if (imagePath != null) {
            Image image = new Image(imagePath.toUri().toString());
            ImageView imageView = ImageUtils.resize(image,THUMB_WIDTH,THUMB_HEIGHT );
            Rectangle clip = new Rectangle(THUMB_WIDTH, THUMB_HEIGHT);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            imageView.setClip(clip);
            return new StackPane(imageView);
        } else{
            ImageView imageView = ImageUtils.getNoCoverImageView(THUMB_WIDTH,THUMB_HEIGHT);
            Rectangle clip = new Rectangle(THUMB_WIDTH, THUMB_HEIGHT);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            imageView.setClip(clip);
            return ImageUtils.getNoCoverGame(game, imageView, THUMB_WIDTH, THUMB_HEIGHT);
        }
    }
}
