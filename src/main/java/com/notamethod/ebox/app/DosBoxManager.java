package com.notamethod.ebox.app;

import com.notamethod.ebox.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class DosBoxManager {
    private static final int DOSBOX_NOTFOUND = 404;
    private static final int DOSBOX_LAUNCH_ERROR = 400;

    /**
     * Writes a DosBOX configuration file to a specific file
     *
     * @param filename     The name of the config file
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


    public int runApplication(String program, ApplicationBean di) throws DosBoxException {

        log.info("running {}", program);
        int returnOK=0;

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

        ArrayList<String> autoexec = new ArrayList<>();

        String capturePath = HelperClass.getCaptureDirectory(di);
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
        if (!di.getExtra().isEmpty()) {
            //   Parse
            properties = di.getExtra().substring(0, di.getExtra().length() - 1).split(";");
            finito = new String[properties.length][3];
            if (!di.getExtra().isEmpty()) {
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
        if (di.getCycles()>0){
            cpu.put("cycles", di.getCycles() + "");
        }

        HelperClass.addOtherSettings(finito, "cpu", cpu);
        allProps.put("CPU", cpu);

        renderer.put("frameskip", di.getFrameskip() + "");
        HelperClass.addOtherSettings(finito, "renderer", renderer);
        allProps.put("RENDER", renderer);

        sdl.put("fullscreen", Configuration.pref.isFullScreen() + "");
        HelperClass.addOtherSettings(finito, "sdl", sdl);
        allProps.put("SDL", sdl);

        dos.put("keyboardlayout", Configuration.pref.getKeyboardCode());
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

        int number = 0;
        if (!di.getCdrom().isEmpty()) { // If we should mount a CD ROM
            String cd = "mount " + di.getCdromLetter() + " \"" + di.getCdrom() + "\" -t cdrom ";
            if (!di.getCdromLabel().isEmpty()) {
                cd += "-label " + di.getCdromLabel();

            }
            autoexec.add(number++, cd);
        }
        autoexec.add(number++, "mount c \"" + di.getPath() + "\"");
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

        // Build execute command
        String[] par = new String[6];
        par[0] = Configuration.pref.getDosBoxPath();

        // If we should try to close the dosbox window or keep it open
        if (!Configuration.pref.isKeepOpen()) {
            par[1] = "-c";
            par[2] = "exit";
        } else {
            par[1] = "-c";
            par[2] = "@echo Keep on rockin' in the free world!";
        }

        par[3] = "-conf";
        par[4] = Configuration.appFolder + "dosbox.conf";

        if (Configuration.pref.isNoConcole()) {
            par[5] = "-noconsole";

        } else {
            par[5] = "";

            // try to execute from the path if no dosbox path is present

        }
        if (Configuration.pref.getDosBoxPath().isEmpty()) {
            par[0] = "dosbox";
        }

        // Try to execute
        try {
            log.info("executing dosbox with params");
            Runtime.getRuntime().exec(par);
        } catch (IOException ex) {
            // What to do if no dosbox path is available
            if (Configuration.pref.getDosBoxPath().isEmpty()) {
               throw new DosBoxException(DOSBOX_NOTFOUND);
            } else {
                log.error("error", ex);
                throw new DosBoxException(DOSBOX_NOTFOUND);
            }
        }
        return returnOK;
    }
}
