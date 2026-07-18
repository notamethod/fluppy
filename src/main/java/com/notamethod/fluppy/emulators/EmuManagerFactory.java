package com.notamethod.fluppy.emulators;

import com.notamethod.fluppy.core.game.Platform;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.emulators.amiga.UAEManager;
import com.notamethod.fluppy.emulators.dosbox.DosBoxManager;

import java.util.EnumMap;
import java.util.Map;

public final class EmuManagerFactory {

    public static Map<Platform, EmulatorManager> createDefault(PreferencesBean config) {
        Map<Platform, EmulatorManager> map = new EnumMap<>(Platform.class);
        map.put(Platform.AMIGA, new UAEManager(config));
        map.put(Platform.DOS, new DosBoxManager(config));
        return map;
    }
}
