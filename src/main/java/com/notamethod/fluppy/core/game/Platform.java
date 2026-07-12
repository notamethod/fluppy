package com.notamethod.fluppy.core.game;

public enum Platform {
    DOS, AMIGA, UNKNOWN;

    public static Platform fromValue(String value) {
        if (value==null) return UNKNOWN;
        for (Platform platform : values()) {
            if (platform.name().equalsIgnoreCase(value)) {
                return platform;
            }
        }
        return UNKNOWN;
    }
}
