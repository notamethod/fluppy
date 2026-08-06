package com.notamethod.fluppy.core.game;


import com.notamethod.fluppy.gui.Messages;

public record Statistics(Long count, Long timeplayed) {

    public String formatTimePlayed(){
        if (timeplayed==null)
            return "";
        long played = timeplayed / 60;
        if (played > 60) {
            return String.valueOf(played / 60);
        } else if (played > 1) {
            return String.valueOf(played);

        }else{
            return "-";
        }
    }

    public static String getTimePlayed(Long time, boolean global) {
        long played = time / 60;
        if (played > 60) {
            return Messages.getString("game.timeplayed.hour", String.valueOf(played / 60), String.valueOf(played % 60));
        } else if (played > 1) {
            return Messages.getString("game.timeplayed.min", String.valueOf(played));

        } else if (global == false) {
            return Messages.getString("game.neverplayed");
        }
        return "-";
    }


}
