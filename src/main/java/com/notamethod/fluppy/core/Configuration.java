package com.notamethod.fluppy.core;

import com.notamethod.fluppy.util.HelperClass;

import java.io.File;

public class Configuration {

    static final String APP_NAME="fluppy";

    public static final String appFolder = HelperClass.getWorkingDirectory(APP_NAME).getAbsolutePath() + File.separator;
    public static final String tempFolder = HelperClass.getTempDirectory(appFolder);
    public static final String coverFolder = HelperClass.getCoverDirectory(appFolder);
    public static final String gamesFolder = HelperClass.getGameDirectory(appFolder);
}
