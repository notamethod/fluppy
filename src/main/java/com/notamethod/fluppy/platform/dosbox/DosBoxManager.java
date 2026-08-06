package com.notamethod.fluppy.platform.dosbox;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.platform.BlocParam;
import com.notamethod.fluppy.platform.EmulatorManager;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

@Slf4j
public class DosBoxManager extends EmulatorManager {

    private static final String CONFIG_FILE = Configuration.dataFolder + File.separator+"dosbox.conf";
    private String runnerName = "dosbox";

    public DosBoxManager() {
        preferences = PreferencesIO.load();
    }

    public DosBoxManager(PreferencesBean config) {
        preferences = config != null ? config : PreferencesIO.load();
    }


    @Override
    protected boolean hasRunner() {
        if (preferences.getDosBoxPath().isEmpty()) {
            return HelperClass.isOnPath(runnerName);
        }
        return true;
    }

    protected String[] generateParams() {
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

    protected void generateConfiguration(String program, GameApp gameApp, String screenRez) {
        //Create HashMaps for preferences
        HashMap<String, HashMap<String, String>> allProps = new HashMap<>();
        HashMap<String, String> cpu = new HashMap<>();
        HashMap<String, String> renderer = new HashMap<>();
        HashMap<String, String> capture = new HashMap<>();
        HashMap<String, String> sdl = new HashMap<>();
        HashMap<String, String> dos = new HashMap<>();
        HashMap<String, String> serial = new HashMap<>();
        HashMap<String, String> ipx = new HashMap<>();
        HashMap<String, String> dosbox = new HashMap<>();
        HashMap<String, String> midi = new HashMap<>();
        HashMap<String, String> gus = new HashMap<>();
        HashMap<String, String> mixer = new HashMap<>();
        HashMap<String, String> speaker = new HashMap<>();


        BlocParam autoexec = new BlocParam("[AUTOEXEC]");



        String capturePath = Configuration.captureFolder;
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
        } else if (gameApp.getYear() != null && gameApp.getYear() > 1970) {
            cpu.put("cycles", getCyclesForYear(gameApp.getYear()) + "");
        }

        capture.put("capture_dir", Configuration.captureFolder);
        HelperClass.addOtherSettings(finito, "capture", capture);
        allProps.put("CAPTURE", capture);

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
        //FIXME
        if (gameApp.getGamePath().toString().toLowerCase().endsWith(".img")) {
            log.info("dos image identified for {}", gameApp.getGamePath());
            autoexec.add(number++, "imgmount a \"" + gameApp.getGamePath().toString() + "\"" + " -t floppy");
            autoexec.add(number++, "a:");
            autoexec.add(number++, program);
        } else {
            autoexec.add(number++, "mount c \"" + gameApp.getExePath() + "\"");
            autoexec.add(number++, "C:");
            autoexec.add(number++, program);
        }
        for (int i = 0; i < finito.length; i++) {
            if (finito[i][0].equalsIgnoreCase("autoexec")) {
                autoexec.add(finito[i][1]);

                // Write configfile

            }

        }
        writeConfig(CONFIG_FILE,
                allProps, autoexec);
    }

    @Override
    public String getlogPrefix() {
        return "[DOSBOX] ";
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
    public static int getCyclesForYear(int annee) {
        if (annee <= 1970) return 0;
        if (annee <= 1984) return 400;    // XT/8086
        if (annee <= 1986) return 500;
        if (annee <= 1990) return 2000;   // 286
        if (annee <= 1994) return 6000;   // 386
        if (annee <= 1997) return 15000;  // 486
        return 30000;                     // Pentium
    }

}
