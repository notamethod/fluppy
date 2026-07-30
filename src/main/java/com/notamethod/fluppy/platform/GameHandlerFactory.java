package com.notamethod.fluppy.platform;

import com.notamethod.fluppy.core.game.Platform;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.platform.amiga.AmigaGameHandler;
import com.notamethod.fluppy.platform.dosbox.DosGameHandler;


import java.util.EnumMap;
import java.util.Map;

public final class GameHandlerFactory {

    public static Map<Platform, PlatformGameHandler> createDefault(PreferencesBean config) {
        Map<Platform, PlatformGameHandler> map = new EnumMap<>(Platform.class);
        map.put(Platform.AMIGA, new AmigaGameHandler(config));
        map.put(Platform.DOS, new DosGameHandler(config));
        return map;
    }
}
