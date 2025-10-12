package com.notamethod.ebox.core;

import com.notamethod.ebox.util.HelperClass;

import java.io.File;

public class Configuration {

    static final String APP_NAME="fluppy";
    public final static PreferencesBean pref = new PreferencesBean();
    public static String appFolder = HelperClass.getWorkingDirectory(APP_NAME).getAbsolutePath() + File.separator;
    public static String gameFile = appFolder + "gamelist.dat";
    public static String configFile = appFolder + "dbox.config";
    public static String tempFolder = HelperClass.getTempDirectory(appFolder);
    public static String coverFolder = HelperClass.getCoverDirectory(appFolder);
    public static String gamesFolder = HelperClass.getGameDirectory(appFolder);
}
