package com.notamethod.fluppy.platform.dosbox;

import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.preferences.PreferencesBean;
import com.notamethod.fluppy.core.preferences.PreferencesIO;
import com.notamethod.fluppy.gui.common.FileFormat;
import com.notamethod.fluppy.platform.PlatformGameHandler;
import com.notamethod.fluppy.util.HelperClass;
import com.notamethod.fluppy.util.SearchInfo;

import java.io.File;
import java.nio.file.Path;

import static com.notamethod.fluppy.util.HelperClass.regexArchive;
import static com.notamethod.fluppy.util.HelperClass.regexGroup;

public class DosGameHandler extends PlatformGameHandler {
    public static final String REGEX_SIMPLE = "(.*)_DOS_[A-Z][A-Z].*";
    protected PreferencesBean preferences;

    public DosGameHandler() {
        preferences = PreferencesIO.load();
    }

    public DosGameHandler(PreferencesBean config) {
        preferences = config != null ? config : PreferencesIO.load();
    }

    @Override
    public void importMetadata(GameApp game) {

    }

    public SearchInfo calculateSearchInfo(GameApp metaGame, String sourceFileName) {
        if (metaGame.getFormat().equals(FileFormat.DOS_IMAGE)) {
            return calculateSearchInfoImg(metaGame, sourceFileName);
        }
        return super.calculateSearchInfo(metaGame, sourceFileName);

    }

    @Override
    public SearchInfo parseFileName(String name) {
        if (name == null) {
            return null;
        }
        SearchInfo info = null;


        if (info != null && info.getTitle() != null) {
            return info;
        }
        String title = regexArchive(name);
        if (title == null) {
            title = regexGroup(name, REGEX_SIMPLE, 1);
        }

        if (title == null) {
            int pos = name.lastIndexOf(".");
            title = pos > 0 ? name.substring(0, pos) : name;
        }

        return new SearchInfo(HelperClass.toTitleGame(title));
    }

    private SearchInfo calculateSearchInfoImg(GameApp metaGame, String sourceFileName) {
        String guessSource = null;
        File exeFile = null;
        String searchString = "";
        if (metaGame.getExePath() != null) {
            //provisionning value
            exeFile = metaGame.getExePath().toFile();
            guessSource = exeFile.getName();
            metaGame.setGameExe(guessSource);
            metaGame.setExePath(Path.of(exeFile.getParentFile().getAbsolutePath()));
            searchString = guessSource;
        }
        //TODO: set intallers
        SearchInfo searchInfo = new SearchInfo(guessSource);


        String textSearch = searchString.isEmpty() ? "" : HelperClass.fromCamelCase(searchString);
        if (textSearch == null || textSearch.isEmpty()) {
            metaGame.setSearchName(metaGame.getName());
        } else {

            metaGame.setSearchName(textSearch);
        }
        metaGame.setDiskNumber(searchInfo.getDisk() == null ? 0 : searchInfo.getDisk());
        metaGame.setNumberOfDisks(searchInfo.getTotalDisk() == null ? 0 : searchInfo.getTotalDisk());
        //TODO get disk number info
        return searchInfo;
    }
}
