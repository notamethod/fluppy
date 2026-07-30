package com.notamethod.fluppy.platform;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.util.FileType;
import com.notamethod.fluppy.util.HelperClass;
import com.notamethod.fluppy.util.SearchInfo;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PlatformGameHandler {

    public static final String REGEX_DISKS = "Disk\\s*(\\d+)(?:\\s+of\\s+(\\d+))?";
    protected PreferencesBean preferences;


    public abstract void importMetadata(GameApp game);

    public void validate(GameApp game) {
        // logique commune par défaut
    }

    public List<FileType> getExtraList(File file) {
        List<FileType> ftList = new ArrayList<>();

        ftList.add(new FileType(FileType.BASE_TYPE.COVER));
        ftList.add(new FileType(FileType.BASE_TYPE.PROTECTION));
        ftList.add(new FileType(FileType.BASE_TYPE.MANUAL));
        return ftList;
    }


    public Pair<Integer, Integer> getDiskNumber(String name) {
        Pattern pattern = Pattern.compile(REGEX_DISKS);
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            Integer disk = Integer.valueOf(matcher.group(1));
            Integer total = matcher.group(2) == null ? null : Integer.valueOf(matcher.group(2));
            return new Pair<>(disk, total);
        }
        pattern = Pattern.compile(".*\\s([A-Z])$");
        matcher = pattern.matcher(name);
        if (matcher.matches()) {
            int diskNumber = letterToDigit(matcher.group(1).charAt(0));
            return new Pair<>(diskNumber, diskNumber);

        }
        return null;
    }

    public SearchInfo calculateSearchInfo(GameApp metaGame, String sourceFileName) {
        File exeFile = null;
        String searchString = "";
        if (metaGame.getExePath() != null) {
            //provisionning value
            exeFile = metaGame.getExePath().toFile();
            metaGame.setGameExe(exeFile.getName());
            metaGame.setExePath(Paths.get(exeFile.getAbsolutePath().substring(0, exeFile.getAbsolutePath().lastIndexOf(File.separatorChar))));
            searchString = exeFile.getParentFile().getAbsolutePath().substring(exeFile.getParentFile().getAbsolutePath().lastIndexOf(File.separator) + 1);
        }
        //TODO: set intallers
        SearchInfo searchInfo = null;

        if (sourceFileName != null) {
            String title = null;
            searchInfo = parseFileName(sourceFileName);
            title = searchInfo.getTitle();
            if (title != null) {
                searchString = title;
            }
        }

        String textSearch = searchString.isEmpty() ? "" : HelperClass.fromCamelCase(searchString);
        metaGame.setSearchName(textSearch);

        metaGame.setDiskNumber(searchInfo.getDisk() == null ? 0 : searchInfo.getDisk());
        metaGame.setNumberOfDisks(searchInfo.getTotalDisk() == null ? 0 : searchInfo.getTotalDisk());
        //TODO get disk number info
        return searchInfo;
    }

    public static int letterToDigit(char letter) {
        letter = Character.toUpperCase(letter);
        if (letter < 'A' || letter > 'Z') {
            throw new IllegalArgumentException("Ce n'est pas une lettre : " + letter);
        }
        return letter - 'A' + 1;
    }

    public abstract SearchInfo parseFileName(String name);

    public Path moveGame(Path child) throws IOException {
        Path tempParent = Paths.get(Configuration.tempFolder);
        Path pathToMove = HelperClass.getDirToMove(tempParent, child);
        String endTarget = getPathGame() + pathToMove.getFileName().toString();
        Path target = Paths.get(Configuration.gamesFolder).resolve(endTarget);
        HelperClass.moveDirectory(pathToMove, target);
        return target;
    }

    protected String getPathGame() {
        return "";
    }
}

