package com.notamethod.fluppy.gui;


public enum SameGameAction {
    REPLACE("samegame.replace"),
    ADD("samegame.add"),
    VARIANT("samegame.variant"),
    NOTHING("samegame.nothing");

    private final String label;

    SameGameAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
