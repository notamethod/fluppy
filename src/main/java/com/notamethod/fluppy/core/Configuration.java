package com.notamethod.fluppy.core;

import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

@Slf4j
public class Configuration {

    public enum OS {LINUX,WINDOWS,MACOS, UNKNOWN}



    static OS currentOS;
    static final String APP_NAME="fluppy";
    static final String HOME = System.getProperty("user.home");
    public static final String appFolder = getWorkingDirectory(APP_NAME).getAbsolutePath() + File.separator;

    public static final String configFolder = getConfigDirectory(APP_NAME);
    public static final String dataFolder = getDataDirectory(APP_NAME);
    public static final String tempFolder = getDirectory(dataFolder, "temp");
    public static final String dbFolder = getDirectory(dataFolder, "data");
    public static final String extraFolder = getDirectory(dataFolder, "extras");
    public static final String gamesFolder = getDirectory(dataFolder, "games");
    public static final String launcherFolder = getDirectory(dataFolder, "launcher");
    public static final String captureFolder = getDirectory(dataFolder,"captures");


public static OS getOS() {
        if (currentOS==null) {
            String sysName = System.getProperty("os.name").toLowerCase();
            if (sysName.contains("linux"))
                currentOS= OS.LINUX;
            else if (sysName.contains("windows"))
                currentOS= OS.WINDOWS;
            else if (sysName.contains("mac"))
                currentOS= OS.MACOS;
            else
            currentOS= OS.UNKNOWN; // if nothing's found
        }
            return currentOS;

}
    private static String getConfigDirectory(String appName) {
        if (getOS().equals(OS.LINUX)){
            String configDir = System.getenv("XDG_CONFIG_HOME") != null
                    ? System.getenv("XDG_CONFIG_HOME")
                    : HOME + "/.config";
            return getDirectory(configDir, appName);
        }else if (getOS().equals(OS.WINDOWS)){
            String configDir = System.getenv("APPDATA") != null
                    ? System.getenv("APPDATA")
                    : HOME + "/.config";
            return getDirectory(configDir, appName);
        }
        return appFolder;
    }
    private static String getDataDirectory(String appName) {
        if (getOS().equals(OS.LINUX)){
            String dataDir = System.getenv("XDG_DATA_HOME") != null
                    ? System.getenv("XDG_DATA_HOME")
                    : HOME + "/.local/share";
            return getDirectory(dataDir, appName);
        }
        if (getOS().equals(OS.WINDOWS)){
            String dataDir = System.getenv("LOCALAPPDATA") != null
                    ? System.getenv("LOCALAPPDATA")
                    : HOME + "/";
            return getDirectory(dataDir, appName);
        }
        return appFolder;
    }




    public static String getDirectory(String parent, String child) {
        File subDirectory = new File(parent, child);
        if (!subDirectory.exists()) {
            if (!subDirectory.mkdirs()) {
                throw new RuntimeException("The game directory could not be created: " + subDirectory);
            }
        }
        return subDirectory.getAbsolutePath();
    }


    /**
     * Get and creates a app folder
     *
     * @param applicationName
     * @return the folder file
     * @author Truben
     */
    public static File getWorkingDirectory(final String applicationName) {

        final String userHome = System.getProperty("user.home", ".");
        final File workingDirectory;
        switch (getOS()) {
            case LINUX:
                workingDirectory = new File(userHome, '.' + applicationName + '/');
                break;
            case WINDOWS:
                final String applicationData = System.getenv("APPDATA");
                if (applicationData != null)
                    workingDirectory = new File(applicationData,  applicationName + '/');
                else
                    workingDirectory = new File(userHome, '.' + applicationName + '/');
                break;
            case MACOS:
                workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
                break;
            default:
                return new File(".");
        }
        if (!workingDirectory.exists())
            if (!workingDirectory.mkdirs())
                throw new RuntimeException("The working directory could not be created: " + workingDirectory);

        log.info("Working directory is " + workingDirectory.getAbsolutePath());
        return workingDirectory;
    }
}
