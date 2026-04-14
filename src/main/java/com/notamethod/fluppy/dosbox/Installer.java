package com.notamethod.fluppy.dosbox;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.util.ArchiveExtractor;
import com.notamethod.fluppy.util.DownloadFiles;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
public class Installer {
    static final String DOSBOXSTAGING_URL_WIN=  "https://github.com/dosbox-staging/dosbox-staging/releases/download/v0.82.2/dosbox-staging-windows-x64-v0.82.2.zip";
    static final String DOSBOXSTAGING_URL_LINUX="https://github.com/dosbox-staging/dosbox-staging/releases/download/v0.82.2/dosbox-staging-linux-x86_64-v0.82.2.tar.xz";

    public String dosboxStaging(){
        final String exeFile="dosbox.exe";
        try {
            String url = Configuration.getOS()== Configuration.OS.LINUX?DOSBOXSTAGING_URL_LINUX:DOSBOXSTAGING_URL_WIN;

            String outputFile= Configuration.tempFolder+"/dosboxStaging."+HelperClass.getArchiveExtension(url);
            DownloadFiles.download(url, outputFile);
            log.debug("done");
            ArchiveExtractor extractor = new ArchiveExtractor(Configuration.tempFolder);
            File extracted=extractor.extractFile(new File(outputFile), true);
            Path pathToMove = extracted.toPath();
            Path target = Paths.get(Configuration.launcherFolder);
            HelperClass.moveDirectory(pathToMove, target);
            File f = new File(outputFile);
            if (f!=null && f.exists()){
                f.delete();
            }
            log.debug("done");


             return   findFile(target, exeFile)
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .map(Path::toString)
                    .orElse(null);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Optional<Path> findFile(Path startDir, String filename) throws IOException {
        try (var stream = Files.find(startDir, Integer.MAX_VALUE,
                (path, attrs) -> attrs.isRegularFile() && path.getFileName().toString().equals(filename))) {
            return stream.findFirst();
        }
    }
}
