package com.notamethod.fluppy.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class RatingControl extends Control {

    private final DoubleProperty rating = new SimpleDoubleProperty(0);

    public RatingControl() {

    }

    public double getRating() {
        return rating.get();
    }

    public void setRating(double value) {
        rating.set(value);
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RatingSkin(this);
    }
}
