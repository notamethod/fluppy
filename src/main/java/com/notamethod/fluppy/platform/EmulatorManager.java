package com.notamethod.fluppy.platform;

import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.gui.PanelListener;
import com.notamethod.fluppy.platform.dosbox.DosBoxResult;
import com.notamethod.fluppy.platform.dosbox.EmulatorException;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public abstract class EmulatorManager {

    protected static final int LAUNCHER_NOTFOUND = 404;
    protected static final int LAUNCHER_LAUNCH_ERROR = 400;
    protected PreferencesBean preferences;

    /**
     * Writes a  configuration file to a specific file
     *
     * @param filename The name of the config file
     */
    public void writeConfig(String filename, Map<String, HashMap<String, String>> pref, BlocParam blocParam) {

        StringBuilder ut = new StringBuilder();
        for (String s : pref.keySet()) {
            ut.append("[").append(s).append("]\n");
            HashMap<String, String> properties = pref.get(s);
            for (String p : properties.keySet()) {
                ut.append(p).append(" = ").append(properties.get(p)).append("\n");
            }
            ut.append("\n");
        }

        if (blocParam != null) {
            ut.append(blocParam.name).append("\n");
            for (String s : blocParam) {
                ut.append(s).append("\n");
            }
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
                               Consumer<DosBoxResult> onFinish) throws EmulatorException {

        if (!hasRunner()) {
            throw new EmulatorException(LAUNCHER_NOTFOUND);
        }
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
                error = e;
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

    protected abstract boolean hasRunner();

    protected abstract String[] generateParams();

    protected abstract void generateConfiguration(String program, GameApp gameApp, String screenRez);

    public abstract String getlogPrefix();


}

