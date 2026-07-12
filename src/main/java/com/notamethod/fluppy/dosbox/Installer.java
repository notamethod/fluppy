package com.notamethod.fluppy.dosbox;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.emulators.Emulator;
import com.notamethod.fluppy.gui.ProgressCallback;
import com.notamethod.fluppy.util.ArchiveExtractor;
import com.notamethod.fluppy.util.DownloadFiles;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class Installer {
    public static final Emulator emu1 = new Emulator("dosboxStaging", "https://github.com/dosbox-staging/dosbox-staging/releases/download/v0.82.2/dosbox-staging-windows-x64-v0.82.2.zip", "https://github.com/dosbox-staging/dosbox-staging/releases/download/v0.82.2/dosbox-staging-linux-x86_64-v0.82.2.tar.xz", "dosbox.exe", "dosbox");
    public static final Emulator emu2 = new Emulator("fs-uae", "https://github.com/FrodeSolheim/fs-uae/releases/download/v3.2.35/FS-UAE_3.2.35_Windows_x86-64.zip", "https://github.com/FrodeSolheim/fs-uae/releases/download/v3.2.35/FS-UAE_3.2.35_Linux_x86-64.tar.xz", "fs-uae.exe", "fs-uae");


    public String application(ProgressCallback callback, Emulator emulator) {
        boolean isLinux = Configuration.getOS()== Configuration.OS.LINUX;
        final String exeFile = isLinux ? emulator.linuxApp() : emulator.winExe();
        try {
            String url = isLinux ? emulator.installUrlLinux() : emulator.installUrlWindows();

            String outputFile = Configuration.tempFolder + "/" + emulator.name() + "." + HelperClass.getArchiveExtension(url);
            DownloadFiles.download(url, outputFile);
            callback.onProgress("Downloaded file: " + outputFile);
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
            callback.onProgress("Moved directory to: " + target);
            log.debug("done");


            if (isLinux){
                Optional<Path> path=findFile(target, exeFile);
                if (path.isPresent()) {
                    setExecutionPermissions(path.get());
                }
                return path
                        .map(Path::toAbsolutePath)
                        .map(Path::normalize)
                        .map(Path::toString)
                        .orElse(null);
            }else {
                return findFile(target, exeFile)
                        .map(Path::toAbsolutePath)
                        .map(Path::normalize)
                        .map(Path::toString)
                        .orElse(null);
            }

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

    private void setExecutionPermissions(Path path) throws IOException {
        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
//        perms.add(PosixFilePermission.GROUP_EXECUTE);
//        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(path, perms);
    }
}
