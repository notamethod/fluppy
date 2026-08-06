package com.notamethod.fluppy.gui.common;


import com.notamethod.fluppy.SafeLog;
import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.core.game.GameManagerException;
import com.notamethod.fluppy.core.game.Platform;
import com.notamethod.fluppy.util.ArchiveExtractor;
import com.notamethod.fluppy.util.Fat12ImageReader;
import com.notamethod.fluppy.util.FileWizard;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class FileActions {


    public FileActions() {

    }


    public GameApp addArchive(File file) throws ProcessFileException, OperationCanceledException {
        GameApp mgame;// = new GameApp();
        ArchiveExtractor extractor = new ArchiveExtractor(Configuration.tempFolder);

        try {
            File path = extractor.extractFile(file);
            mgame = addDirectory(path, true);
            return mgame;
        } catch (IOException e) {
            throw new ProcessFileException("exception.archive.extract", e);
        }
    }

    public GameApp addDirectory(File inFile, boolean fromArchive) throws ProcessFileException, OperationCanceledException {
        GameApp mgame = new GameApp();
        mgame.setFormat(fromArchive ? FileFormat.ARCHIVE : FileFormat.DIRECTORY);
        mgame.setGamePath(inFile.toPath());

        log.info("analyze directory {}", inFile.getAbsolutePath());
        FileWizard fw = new FileWizard();
        List<File> runners = fw.getRunners(inFile);
        int count = runners.size();
        if (count <= 0) {
            throw new ProcessFileException("exception.noexec", inFile.getAbsolutePath());
        } else {
            mgame.setPlatform(Platform.DOS);
        }


        count = 0;
        for (File f : runners) {
            if (!f.toString().toLowerCase().contains("setup") && !f.toString().toLowerCase().contains("install") && (f.toString().toLowerCase().endsWith("pif") || f.toString().toLowerCase().endsWith("exe") || f.toString().toLowerCase().endsWith("com") || f.toString().toLowerCase().endsWith("bat"))) {
                mgame.getExeFiles().add(f);
            } else if (f.toString().toLowerCase().contains("setup") || f.toString().toLowerCase().contains("install")) {
                mgame.getInstallers().add(f);
            }
        }
        if (mgame.getExeFiles().isEmpty()) {
            throw new ProcessFileException("exception.noexec", inFile.getAbsolutePath());
        }
        if (mgame.getExeFiles().size() > 1) {
            log.info("multiple exe found");
            return mgame;
        } else {
            log.info("one exe found");
            mgame.setExePath(mgame.getExeFiles().get(0).toPath());
            mgame.setGameExe(mgame.getExeFiles().get(0).getName());
        }
        if (!runners.isEmpty()) {
            mgame.setPlatform(Platform.DOS);
        }

        return mgame;
    }

    public GameApp addAmiga(File inFile, boolean copyFile) throws ProcessFileException {
        log.info("adding amiga file");
        GameApp mgame = new GameApp();
        mgame.setPlatform(Platform.AMIGA);
        mgame.setFormat(FileFormat.AMIGA_ADF);
        mgame.setGamePath(inFile.toPath());
        mgame.getExeFiles().add(inFile);
        log.info("analyze directory {}", inFile.getAbsolutePath());
        mgame.setExePath(mgame.getExeFiles().get(0).toPath());
        mgame.setGameExe(mgame.getExeFiles().get(0).getName());
        if (copyFile) {
            try {
                mgame.setGamePath(copyToTemp(inFile));
            } catch (IOException e) {
                throw new ProcessFileException("fail to moving file", e);
            }
        }
        return mgame;
    }

    private Path copyToTemp(File inFile) throws IOException {
        Path source = Paths.get(inFile.getAbsolutePath());
        Path repDest = Path.of(Configuration.tempFolder);
        Path destination = repDest.resolve(source.getFileName());
        return Files.copy(source, destination);

    }

    public GameApp addImage(File inFile) {
        log.info("adding Game: type: PC image file {}", inFile);
        GameApp mgame = new GameApp();
        mgame.setPlatform(Platform.DOS);
        mgame.setFormat(FileFormat.DOS_IMAGE);
        mgame.setGamePath(inFile.toPath());
        try (Fat12ImageReader reader = new Fat12ImageReader(mgame.getGamePath())) {
            Fat12ImageReader.DosFile best = reader.findBestExecutable(); // le candidat le plus probable
            List<Fat12ImageReader.DosFile> all = reader.findExecutablesSorted(); // tous, triés
            mgame.setGameExe(all.getFirst().fullPath());
            SafeLog.debug(log, "Found", all);
            List<File> files = all.stream().map(Fat12ImageReader.DosFile::fullPath)
                    .map(File::new)
                    .toList();
            //TODO: classes for diffrent sources files tpyes
            mgame.getExeFiles().addAll(files);
            mgame.setName(all.getFirst().name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mgame.setExePath(null);
        return mgame;
    }

    public GameApp buildGameInfo(File inFile, FileFormat fileFormat) throws ProcessFileException, OperationCanceledException {
        switch (fileFormat) {
            case DIRECTORY -> {
                return addDirectory(inFile, false);
            }
            case ARCHIVE -> {
                return addArchive(inFile);
            }
            case AMIGA_ADF -> {
                return addAmiga(inFile, true);
            }
            case DOS_IMAGE -> {
                return addImage(inFile);
            }
            default -> throw new ProcessFileException("format unknown");
        }

    }

    public FileFormat detect(File inFile) throws GameManagerException, OperationCanceledException {
        if (inFile.isDirectory()) {
            return FileFormat.DIRECTORY;
        } else if (ArchiveExtractor.isArchive(inFile)) {
            return FileFormat.ARCHIVE;

        } else if (inFile.getName().toLowerCase().endsWith("adf")) {
            return FileFormat.AMIGA_ADF;
        } else if (inFile.getName().toLowerCase().endsWith("img")) {
            return FileFormat.DOS_IMAGE;
        } else {
            log.error("unkown format for {}", inFile);
            return FileFormat.UNKNOWN;
        }
    }

    /**
     * A metod that tries to insert an application into
     * dbox' database using a file or directory
     *
     * @return
     */


}