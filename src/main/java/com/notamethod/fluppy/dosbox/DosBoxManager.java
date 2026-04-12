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
public class DosBoxManager {
    private static final int DOSBOX_NOTFOUND = 404;
    private static final int DOSBOX_LAUNCH_ERROR = 400;
    private PreferencesBean preferences;

    public DosBoxManager() {
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
        par[4] = Configuration.appFolder + "dosbox.conf";

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
        HashMap<String, String> cpu = new HashMap<>();
        HashMap<String, String> renderer = new HashMap<>();
        HashMap<String, String> sdl = new HashMap<>();
        HashMap<String, String> dos = new HashMap<>();
        HashMap<String, String> serial = new HashMap<>();
        HashMap<String, String> ipx = new HashMap<>();
        HashMap<String, String> dosbox = new HashMap<>();
        HashMap<String, String> midi = new HashMap<>();
        HashMap<String, String> gus = new HashMap<>();
        HashMap<String, String> mixer = new HashMap<>();
        HashMap<String, String> speaker = new HashMap<>();


        ArrayList<String> autoexec = new ArrayList<>();

        String capturePath = HelperClass.getCaptureDirectory(gameApp);
        File dir = new File(capturePath);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.warn("error creating directory");

            }
        }

        dosbox.put("captures", capturePath);

        // Split the extras string
        String[] properties = new String[0];
        String[][] finito = new String[0][0];
        if (gameApp.getExtra() != null && !gameApp.getExtra().isEmpty()) {
            //   Parse
            properties = gameApp.getExtra().substring(0, gameApp.getExtra().length() - 1).split(";");
            finito = new String[properties.length][3];
            if (!gameApp.getExtra().isEmpty()) {
                for (int i = 0; i < properties.length; i++) {
                    int first = properties[i].indexOf(" => ");
                    int second = properties[i].indexOf(" = ");
                    if (first <= 0) {
                        continue;
                    }
                    finito[i][0] = properties[i].substring(0, first);
                    if (finito[i][0].equalsIgnoreCase("autoexec")) {
                        finito[i][1] = properties[i].substring(first + 4);

                    } else {
                        finito[i][1] = properties[i].substring(first + 4, second);
                        finito[i][2] = properties[i].substring(second + 3);
                    }
                }
            }
        }

        // Add settings to the configuration file
        if (gameApp.getCycles() > 0) {
            cpu.put("cycles", gameApp.getCycles() + "");
        }

        HelperClass.addOtherSettings(finito, "cpu", cpu);
        allProps.put("CPU", cpu);

        renderer.put("frameskip", gameApp.getFrameskip() + "");
        HelperClass.addOtherSettings(finito, "renderer", renderer);
        allProps.put("RENDER", renderer);

        if (preferences.isFullScreen()) {
            if (DosboxType.fromString(preferences.getDosBoxType()).equals(DosboxType.CLASSIC)) {
                sdl.put("fullscreen", "true");
            } else {
                sdl.put("fullscreen", "false");
                sdl.put("windowborderless", "true");
                sdl.put("output", "opengl");
                sdl.put("aspect", "true");
                log.debug("screen resolution:"+screenRez);
                if (screenRez!=null){
                    sdl.put("windowresolution",screenRez);
                }else{
                    sdl.put("windowresolution", "desktop");
                }

            }
        }
        speaker.put("disney", "true");

        HelperClass.addOtherSettings(finito, "sdl", sdl);
        allProps.put("SDL", sdl);

        dos.put("keyboardlayout", preferences.getKeyboardCode());
        HelperClass.addOtherSettings(finito, "dos", dos);
        allProps.put("DOS", dos);

        HelperClass.addOtherSettings(finito, "serial", serial);
        allProps.put("SERIAL", serial);

        HelperClass.addOtherSettings(finito, "ipx", ipx);
        allProps.put("IPX", serial);

        HelperClass.addOtherSettings(finito, "dosbox", dosbox);
        allProps.put("DOSBOX", dosbox);

        HelperClass.addOtherSettings(finito, "mixer", mixer);
        allProps.put("MIXER", mixer);

        HelperClass.addOtherSettings(finito, "gus", gus);
        allProps.put("GUS", gus);

        HelperClass.addOtherSettings(finito, "midi", midi);
        allProps.put("MIDI", midi);

        HelperClass.addOtherSettings(finito, "speaker", speaker);
        allProps.put("SPEAKER", speaker);

        if (gameApp.getMachine() != null) {
            dosbox.put("machine", gameApp.getMachine() + "");
            HelperClass.addOtherSettings(finito, "dosbox", dosbox);
            allProps.put("DOSBOX", dosbox);
        }
        int number = 0;
        if (gameApp.getCdrom() != null && !gameApp.getCdrom().isEmpty()) { // If we should mount a CD ROM
            String cd = "mount " + gameApp.getCdromLetter() + " \"" + gameApp.getCdrom() + "\" -t cdrom ";
            if (!gameApp.getCdromLabel().isEmpty()) {
                cd += "-label " + gameApp.getCdromLabel();
            }
            autoexec.add(number++, cd);
        }
        autoexec.add(number++, "mount c \"" + gameApp.getExePath() + "\"");
        autoexec.add(number++, "C:");
        autoexec.add(number++, program);
        for (int i = 0; i < finito.length; i++) {
            if (finito[i][0].equalsIgnoreCase("autoexec")) {
                autoexec.add(finito[i][1]);

                // Write configfile

            }

        }
        writeConfig(Configuration.appFolder + "dosbox.conf",
                allProps, autoexec);
    }

    public boolean isDosboxPresent() {
        return !preferences.getDosBoxPath().isEmpty();
    }

    public static DosboxType detectDosboxType(Path exe) {
        try {
            Process process = new ProcessBuilder(exe.toString(), "--version").redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes());
            if (output.contains("DOSBox-X")) return DosboxType.X;
            if (output.contains("dosbox-staging")) return DosboxType.STAGING;
            if (output.contains("ECE")) return DosboxType.ECE;
            if (output.contains("DOSBox version")) return DosboxType.CLASSIC;
            return DosboxType.UNKNOWN;
        } catch (Exception e) {
            return DosboxType.UNKNOWN;
        }
    }


}
