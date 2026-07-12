package com.notamethod.fluppy.dosbox;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameEntity;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.gui.PanelListener;
import com.notamethod.fluppy.util.HelperClass;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class UAEManager {
    private static final int LAUNCHER_NOTFOUND = 404;
    private static final int LAUNCHER_LAUNCH_ERROR = 400;
    private static final String CONFIG_FILE = Configuration.dataFolder + File.separator+"configuration.fs-uae";
    private PreferencesBean preferences;

    public UAEManager() {
        preferences = PreferencesIO.load();
    }

    /**
     * Writes a DosBOX configuration file to a specific file
     *
     * @param filename The name of the config file
     */
    public void writeConfig(String filename, HashMap<String, HashMap<String, String>> pref, ArrayList<String> autoexec) {

        StringBuilder ut = new StringBuilder();
        for (String s : pref.keySet()) {
            ut.append("[").append(s).append("]\n");
            HashMap<String, String> properties = pref.get(s);
            for (String p : properties.keySet()) {
                ut.append(p).append(" = ").append(properties.get(p)).append("\n");
            }
            ut.append("\n");
        }


        try {
            java.io.FileWriter fw = new java.io.FileWriter(filename);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
            bw.write(ut.toString());
            bw.close();
            fw.close();
        } catch (IOException ex) {
            log.error("error writing configuration file", ex);
        }
    }


    public long runApplication(String program, GameApp gameApp, String screenRez, PanelListener listener,
                               Consumer<String> onStdout,
                               Consumer<String> onStderr,
                                Consumer<DosBoxResult> onFinish) throws DosBoxException {

        log.info("running {}", program);
        int returnOK = 0;

        //reload
        preferences = PreferencesIO.load();
        generateConfiguration(program, gameApp, screenRez);

        // Build execute command
        String[] par = generateParams();

        // Try to execute
        long now = java.time.Instant.now().toEpochMilli();
        Process process = null;
        long diff = 0;
        if (listener!=null)
            listener.onLaunchGame();
        Thread t = new Thread(() -> {
            long start = System.currentTimeMillis();
            int exitCode = -1;
            Exception error = null;
            try {
                Process p = new ProcessBuilder(par).start();

                Thread outThread = new Thread(() -> {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(p.getInputStream()))) {

                        String line;
                        while ((line = br.readLine()) != null) {
                            String finalLine = line;
                            Platform.runLater(() -> onStdout.accept(finalLine));
                        }
                    } catch (Exception e) {
                        log.error("error reading process output", e);
                    }
                });

                Thread errThread = new Thread(() -> {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(p.getErrorStream()))) {

                        String line;
                        while ((line = br.readLine()) != null) {
                            String finalLine = line;
                            Platform.runLater(() -> onStderr.accept(finalLine));
                        }
                    } catch (Exception e) {
                       log.error("error reading process output", e);
                    }
                });

                outThread.start();
                errThread.start();

                exitCode=p.waitFor();

                outThread.join();
                errThread.join();
            } catch (InterruptedException e) {
               error=e;
               log.warn("app interrupted", e);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                error=e;
                throw new RuntimeException(e);
            }
            long duration = System.currentTimeMillis() - start;
            DosBoxResult result = new DosBoxResult( error == null && exitCode == 0, duration, exitCode, error );
            Platform.runLater(() -> onFinish.accept(result));
        });

        t.setDaemon(true); // propre : le thread ne bloque pas la fermeture de l’app
        t.start();

        return diff;
    }

    private String[] generateParams() {
        String[] par = new String[2];
        par[0] = preferences.getFsuaePath();




        par[1] = CONFIG_FILE;


        if (preferences.getFsuaePath().isEmpty()) {
            par[0] = "fs-uae";
        }
        return par;
    }

    private void generateConfiguration(String program, GameApp gameApp, String screenRez) {
        //Create HashMaps for preferences
        HashMap<String, HashMap<String, String>> allProps = new HashMap<>();
        HashMap<String, String> config = new HashMap<>();
//        HashMap<String, String> memory = new HashMap<>();
//        HashMap<String, String> rom_path = new HashMap<>();
//        HashMap<String, String> display = new HashMap<>();
//        HashMap<String, String> controller = new HashMap<>();
//        HashMap<String, String> drives = new HashMap<>();
//        HashMap<String, String> drive_speed = new HashMap<>();
//        HashMap<String, String> keyboard_input = new HashMap<>();


        // Split the extras string
        String[] properties = new String[0];
        String[][] finito = new String[0][0];
        if (gameApp.getMachine() != null) {
            config.put("amiga_model", gameApp.getMachine());


        }
        config.put("kickstart_file",    preferences.getKickstartPath());
        //FIXME
        config.put("floppy_drive_0",    gameApp.getGamePath().toString());
       if (gameApp.getExtraDisks()!=null){
           configureDisks(gameApp.getGamePath().toString(), gameApp.getExtraDisks(), config);
       }
        config.put("floppy_drive_speed",   "800");



        if (preferences.isFullScreen()) {
            config.put("fullscreen",   "1");
            config.put("fullscreen_mode",   "fullscreen-window");
        }else{
            config.put("fullscreen",   "0");
        }
   //     HelperClass.addOtherSettings(finito, "capture", capture);
        allProps.put("config", config);
        writeConfig(CONFIG_FILE,
                allProps, null);
    }

    private void configureDisks(String gamePath, String extraDisks, HashMap<String, String> config) {
        Map<Integer, String> extradisks = buildDiskList(gamePath,extraDisks);
        config.put("floppy_image_0",gamePath);
        for (Map.Entry<Integer, String> entry : extradisks.entrySet()) {
            config.put("floppy_image_"+(entry.getKey()-1),entry.getValue());
            if (entry.getKey().equals(2)){
                config.put("floppy_drive_1",entry.getValue());
            }

        }
    }

    public boolean isKickstart() {
        return !preferences.getKickstartPath().isEmpty();
    }


    private Map<Integer, String> buildDiskList(String path, String disks){
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
