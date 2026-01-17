package com.notamethod.fluppy.gui;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;

public class StarNode extends StackPane {

    private final int index;
    private final RatingControl control;

    private final SVGPath emptyStar = new SVGPath();
    private final SVGPath fullStar = new SVGPath();
    private final Rectangle clipHalf = new Rectangle();

    public StarNode(int index, RatingControl control) {
        this.index = index;
        this.control = control;
        this.getStylesheets().add(getClass().getResource("/styles/rating.css").toExternalForm());
        emptyStar.setContent(StarShape.STAR_OUTLINE);
        //emptyStar.setStyle("-fx-fill: green;");
        fullStar.setContent(StarShape.STAR_FULL);
      //  fullStar.setStyle("-fx-fill: red;");
        fullStar.setClip(clipHalf);

        getStyleClass().add("star"); // SVG shapes
         emptyStar.getStyleClass().add("star-empty");
         fullStar.getStyleClass().add("star-full"); // Clip for half-star fullStar.setClip(clipHalf);
        getChildren().addAll(emptyStar, fullStar);

        setOnMouseMoved(e -> updateHover(e.getX()));
        setOnMouseClicked(e -> commitRating(e.getX()));

        control.ratingProperty().addListener((obs, oldV, newV) -> update());

        update();
    }

    private void updateHover(double x) {
        double half = getWidth() / 2;
        double value = index + (x < half ? 0.5 : 1.0);
        control.setRating(value);
    }

    private void commitRating(double x) {
        updateHover(x);
    }

    private void update() {
        double rating = control.getRating();
        double starValue = rating - index;

        if (starValue >= 1) {
            clipHalf.setWidth(getWidth());
        } else if (starValue >= 0.5) {
            clipHalf.setWidth(getWidth() / 2);
        } else {
            clipHalf.setWidth(0);
        }
    }
}
