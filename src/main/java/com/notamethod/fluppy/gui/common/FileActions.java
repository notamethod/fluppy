package com.notamethod.fluppy.gui.common;


import com.notamethod.fluppy.core.*;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameManagerException;
import com.notamethod.fluppy.util.ArchiveExtractor;
import com.notamethod.fluppy.util.FileWizard;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class FileActions {


    public FileActions() {

    }






    public GameApp addArchive(File file) throws GameManagerException, OperationCanceledException {
        GameApp mgame;// = new GameApp();
        ArchiveExtractor extractor = new ArchiveExtractor(Configuration.tempFolder);

        try {
            File path = extractor.extractFile(file);
            mgame = addDirectory(path, true);
            return mgame;
        } catch (IOException e) {
            throw new GameManagerException("exception.archive.extract", e);
        }
    }

    public GameApp addDirectory(File inFile, boolean fromArchive) throws GameManagerException, OperationCanceledException {
        GameApp mgame = new GameApp();
        mgame.setGamePath(inFile.toPath());

        log.info("analyze directory {}", inFile.getAbsolutePath());
        FileWizard fw = new FileWizard();
        List<File> runners = fw.getRunners(inFile);
        int count = runners.size();
        if (count == 0) {
            throw new GameManagerException("exception.noexec", inFile.getAbsolutePath());
        }


        count = 0;
        for (File f : runners) {
            if (!f.toString().toLowerCase().contains("setup") && !f.toString().toLowerCase().contains("install") && (f.toString().toLowerCase().endsWith("pif") || f.toString().toLowerCase().endsWith("exe") || f.toString().toLowerCase().endsWith("com") || f.toString().toLowerCase().endsWith("bat"))) {
                mgame.getExeFiles().add(f);
            } else if (f.toString().toLowerCase().contains("setup") || f.toString().toLowerCase().contains("install")) {
                mgame.getInstallers().add(f);
            }
        }
        if ( mgame.getExeFiles().isEmpty()) {
            throw new GameManagerException("exception.noexec", inFile.getAbsolutePath());
        }
        if (mgame.getExeFiles().size() > 1) {
            log.info("multiple exe found");
            return mgame;
        }
    else{
            log.info("one exe found");
            mgame.setExePath(mgame.getExeFiles().get(0).toPath());
            mgame.setGameExe(mgame.getExeFiles().get(0).getName());
        }

        return mgame;
    }






    public GameApp addAmiga(File inFile) {
        log.info("adding amiga file");
        GameApp mgame = new GameApp();
        mgame.setPlatform("amiga");
        mgame.setGamePath(inFile.toPath());
        mgame.getExeFiles().add(inFile);
        log.info("analyze directory {}", inFile.getAbsolutePath());
        mgame.setExePath(mgame.getExeFiles().get(0).toPath());
        mgame.setGameExe(mgame.getExeFiles().get(0).getName());
        return mgame;
    }

    /**
     * A metod that tries to insert an application into
     * dbox' database using a file or directory
     *
     * @return
     */



}