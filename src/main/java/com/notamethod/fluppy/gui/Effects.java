package com.notamethod.fluppy.gui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.DisplacementMap;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.FloatMap;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Effects {

    public Animation biosEffectAnim(Node node){

        FadeTransition clignotement = new FadeTransition(Duration.seconds(0.5), node);
        clignotement.setFromValue(1.0);
        clignotement.setToValue(0.9);
        clignotement.setCycleCount(Animation.INDEFINITE);
        clignotement.setAutoReverse(true);

        return clignotement;
    }

    public DisplacementMap distorsionEffect(){
        FloatMap map = new FloatMap();
        map.setWidth(64);
        map.setHeight(64);

    // Génère une onde sinusoïdale horizontale
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                double value = Math.sin(x / 5.0) * 0.05;
                map.setSamples(x, y, (float) value, 0f);
            }
        }

        DisplacementMap distortion = new DisplacementMap();
        distortion.setMapData(map);
        distortion.setWrap(true);

        return distortion;
    }

    public Animation distortionAnim(Node node){
        DisplacementMap distortion = distorsionEffect();
        Timeline anim = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> distortion.setScaleX(0.02)),
                new KeyFrame(Duration.seconds(0.2), e -> distortion.setScaleX(0.01)),
                new KeyFrame(Duration.seconds(0.4), e -> distortion.setScaleX(0.02))
        );
        anim.setCycleCount(Animation.INDEFINITE);
        node.setEffect(distortion);
        return anim;
    }

    public StackPane noiseEffectWrapper(Node node){
        Image noiseImg= new Image(getClass().getResourceAsStream("/images/noise.png"),80, 80, false, true);
        ImageView bruit = new ImageView(noiseImg);
        bruit.setOpacity(0.1);
        bruit.setMouseTransparent(true); // pour ne pas bloquer les interactions

        return new StackPane(node, bruit);

    }

    public static Animation getBordureAnim(Node topRibbon) {
        Timeline bordureAnim = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> topRibbon.setStyle("-fx-border-color: red; -fx-border-width: 2;")),
                new KeyFrame(Duration.seconds(0.5), e -> topRibbon.setStyle("-fx-border-color: orange; -fx-border-width: 2;")),
                new KeyFrame(Duration.seconds(1), e -> topRibbon.setStyle("-fx-border-color: yellow; -fx-border-width: 2;")),
                new KeyFrame(Duration.seconds(1.5), e -> topRibbon.setStyle("-fx-border-color: lime; -fx-border-width: 2;")),
                new KeyFrame(Duration.seconds(2), e -> topRibbon.setStyle("-fx-border-color: cyan; -fx-border-width: 2;")),
                new KeyFrame(Duration.seconds(2.5), e -> topRibbon.setStyle("-fx-border-color: magenta; -fx-border-width: 2;"))
        );
        bordureAnim.setCycleCount(Animation.INDEFINITE);
        return bordureAnim;
    }


    public static Effect getDropShadow1() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setColor(Color.web("#333"));
        return shadow;
    }

    public static  Effect getDropShadow2() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(10);
        shadow.setColor(Color.color(0.1, 0.1, 0.1, 0.7)); // ombre gris-noir transparente
        return shadow;
    }
}
