package com.notamethod.fluppy.core.game;


public record Statistics(Long count, Long timeplayed) {

    public String formatTimePlayed(){
        long played = timeplayed / 60;
        if (played > 60) {
            return String.valueOf(played / 60);
        } else if (played > 1) {
            return String.valueOf(played);

        }else{
            return "-";
        }
    }
}
