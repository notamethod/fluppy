package com.notamethod.fluppy.emulators.amiga;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.emulators.EmulatorManager;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class UAEManager extends EmulatorManager {

    private static final String CONFIG_FILE = Configuration.dataFolder + File.separator + "configuration.fs-uae";


    public UAEManager() {
        preferences = PreferencesIO.load();
    }

    public UAEManager(PreferencesBean config) {
        preferences = config != null ? config : PreferencesIO.load();
    }


    protected String[] generateParams() {
        String[] par = new String[2];
        par[0] = preferences.getFsuaePath();
        par[1] = CONFIG_FILE;
        if (preferences.getFsuaePath().isEmpty()) {
            par[0] = "fs-uae";
        }
        return par;
    }

    protected void generateConfiguration(String program, GameApp gameApp, String screenRez) {
        //Create HashMaps for preferences
        HashMap<String, HashMap<String, String>> allProps = new HashMap<>();
        HashMap<String, String> config = new HashMap<>();

        // Split the extras string
        String[] properties = new String[0];
        String[][] finito = new String[0][0];
        if (gameApp.getMachine() != null) {
            config.put("amiga_model", gameApp.getMachine());
        }

        config.put("kickstart_file", preferences.getKickstartPath());
        //FIXME
        config.put("floppy_drive_0", gameApp.getGamePath().toString());
        if (gameApp.getExtraDisks() != null) {
            configureDisks(gameApp.getGamePath().toString(), gameApp.getExtraDisks(), config);
        }
        config.put("floppy_drive_speed", "800");

        if (preferences.isFullScreen()) {
            config.put("fullscreen", "1");
            config.put("fullscreen_mode", "fullscreen-window");
        } else {
            config.put("fullscreen", "0");
        }
        //     HelperClass.addOtherSettings(finito, "capture", capture);
        allProps.put("config", config);
        writeConfig(CONFIG_FILE,
                allProps, null);
    }

    @Override
    public String getlogPrefix() {
        return "[FSUAE] ";
    }

    private void configureDisks(String gamePath, String extraDisks, HashMap<String, String> config) {
        Map<Integer, String> extradisks = buildDiskList(gamePath, extraDisks);
        config.put("floppy_image_0", gamePath);
        for (Map.Entry<Integer, String> entry : extradisks.entrySet()) {
            config.put("floppy_image_" + (entry.getKey() - 1), entry.getValue());
            if (entry.getKey().equals(2)) {
                config.put("floppy_drive_1", entry.getValue());
            }

        }
    }

    public boolean isKickstart() {
        return !preferences.getKickstartPath().isEmpty();
    }


    private Map<Integer, String> buildDiskList(String path, String disks) {
        List<String> diskList = new ArrayList<>();
        diskList.add(path);
        Map<Integer, String> extradisks = Arrays.stream(disks.split(";"))
                .map(part -> part.split("#", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(
                        arr -> Integer.parseInt(arr[0]),
                        arr -> arr[1]
                ));

        return extradisks;
    }


}
