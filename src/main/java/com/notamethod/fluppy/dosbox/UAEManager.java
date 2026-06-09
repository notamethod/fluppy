package com.notamethod.fluppy.dosbox;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

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

        ut.append("[AUTOEXEC]\n");
        for (String s : autoexec) {
            ut.append(s).append("\n");
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
        String[] par = generateDosBoxParams();

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

    private String[] generateDosBoxParams() {
        String[] par = new String[6];
        par[0] = preferences.getDosBoxPath();

        // If we should try to close the dosbox window or keep it open
        if (!preferences.isKeepOpen()) {
            par[1] = "-c";
            par[2] = "exit";
        } else {
            par[1] = "-c";
            par[2] = "@echo Keep open";
        }

        par[3] = "-conf";
        par[4] = CONFIG_FILE;

        if (preferences.isNoConsole()) {
            par[5] = "-noconsole";

        } else {
            par[5] = "";

            // try to execute from the path if no dosbox path is present

        }
        if (preferences.getDosBoxPath().isEmpty()) {
            par[0] = "dosbox";
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

            allProps.put("config", config);
        }
        config.put("kickstart_file",    preferences.getKickstartPath());
        //FIXME
        config.put("floppy_drive_0",    gameApp.getGameExe());
        config.put("floppy_drive_speed",   "800");



        if (preferences.isFullScreen()) {
            config.put("fullscreen",   "1");
            config.put("fullscreen_mode",   "fullscreen-window");
        }else{
            config.put("fullscreen",   "0");
        }
   //     HelperClass.addOtherSettings(finito, "capture", capture);

        writeConfig(CONFIG_FILE,
                allProps, null);
    }

    public boolean isKickstart() {
        return !preferences.getKickstartPath().isEmpty();
    }




}
