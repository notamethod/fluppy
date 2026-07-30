package com.notamethod.fluppy.platform.amiga;

import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.platform.PlatformGameHandler;
import com.notamethod.fluppy.util.FileType;
import com.notamethod.fluppy.util.HelperClass;
import com.notamethod.fluppy.util.SearchInfo;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmigaGameHandler extends PlatformGameHandler {
    static final String REGEX_AMIGA_ADF = "^(.+?)\\s*\\((\\d{4})\\)(?:\\((?!Disk)[^)]+\\)|(?:\\[[^\\]]+\\]))*(?:\\((Disk[^)]+)\\))?\\.adf$";
    static final String REGEX_AMIGA_SIMPLE = "^(.+?)\\s*(?:\\((\\d{4})\\))?\\s*((?:[(\\[].*)?)\\.adf$";
    protected PreferencesBean preferences;
    private Pattern patternAdf = Pattern.compile(REGEX_AMIGA_ADF);
    private Pattern patternAmiga = Pattern.compile(REGEX_AMIGA_SIMPLE);

    public AmigaGameHandler() {
        preferences = PreferencesIO.load();
    }

    public AmigaGameHandler(PreferencesBean config) {
        preferences = config != null ? config : PreferencesIO.load();
    }

    @Override
    public void importMetadata(GameApp game) {
        // parsing ADF, convention WHDLoad
    }

    public void detectWhdloadSlaveFile(GameApp game) {
        // action 100% Amiga
    }

    @Override
    public List<FileType> getExtraList(File file) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith("adf")) {
            return super.getExtraList(file);
        }

        List<FileType> ftList = new ArrayList<>();
        FileType fileType = new FileType(FileType.BASE_TYPE.EXTRA_DISK);
        SearchInfo searchInfo = parseFileName(name);
        Matcher matcher = patternAmiga.matcher(file.getName());
        if (matcher.matches()) {

            ftList.add(fileType);
            Matcher matcher2 = patternAmiga.matcher(file.getName());
//            Integer disk= Integer.valueOf(matcher.group(1));
//            Integer total= matcher.group(2)==null?null:Integer.valueOf(matcher.group(2));
//            return new Pair<>(disk, total);
        }
        if (ftList.isEmpty()) {
            return super.getExtraList(file);
        }
        return ftList;
    }


    public SearchInfo parseFileName(String name) {
        Matcher matcher = patternAmiga.matcher(name);
        if (matcher.matches()) {
            SearchInfo searchInfo = new SearchInfo(matcher.group(1), matcher.group(2));
            String diskInfo = matcher.group(3);
            if (diskInfo != null) {
                Pair<Integer, Integer> info = getDiskNumber(diskInfo);
                if (info != null) {
                    searchInfo.setDisk(info.getKey());
                    searchInfo.setTotalDisk(info.getValue());
                }
            }
            searchInfo.setTitle(HelperClass.toTitleGame(searchInfo.getTitle()));
            return searchInfo;
        }
        return null;
    }

    protected String getPathGame() {
        return "amiga/";
    }
}

