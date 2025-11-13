package com.notamethod.fluppy.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ArchiveExtractor {


    public static boolean isArchive(File exeFile) {
        return exeFile.getName().toLowerCase().endsWith("7z") || exeFile.getName().toLowerCase().endsWith("zip");
    }


    public File extractFile(File archiveFile, File outputDir) throws IOException {
        if (archiveFile.getName().toLowerCase().endsWith("7z")){
            return extract7z( archiveFile,  outputDir);
        }else{
            return extractZip( archiveFile.getAbsolutePath(),  outputDir.getAbsolutePath());
        }
    }

    protected File extract7z(File archiveFile, File outputDir) throws IOException{
        log.info("extract 7z archive...");
        File gameOutputDire=gameOutputDir(archiveFile, outputDir);
        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry entry;

            if (gameOutputDire.exists()){
                throw new IOException("target not empty");
            }else{
                gameOutputDire.mkdirs();
            }
            //for (SevenZArchiveEntry entry : sevenZFile.getEntries()) {
            while ((entry = sevenZFile.getNextEntry()) != null) {
                File outputFile = new File(gameOutputDire, entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(outputFile.toPath());
                    continue;
                }

                // Crée le dossier parent si nécessaire
                Path parent = outputFile.toPath().getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                // Écrit le fichier extrait
                try (OutputStream out = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long bytesRemaining = entry.getSize();
                    while (bytesRemaining > 0 && (bytesRead = sevenZFile.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                        out.write(buffer, 0, bytesRead);
                        bytesRemaining -= bytesRead;
                    }
                }
            }

            log.info("Successfully extracted in : " + gameOutputDire.getAbsolutePath());

        } catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return gameOutputDire;
    }



    protected File extractZip(String zipFilePath, String xoutputDir) {
        log.info("extract zip archive...");
        File gameOutputDire=gameOutputDir(new File(zipFilePath), new File(xoutputDir));
        try (InputStream fi = Files.newInputStream(Paths.get(zipFilePath));
             BufferedInputStream bi = new BufferedInputStream(fi);
             ArchiveInputStream i = new ArchiveStreamFactory()
                     .createArchiveInputStream(ArchiveStreamFactory.ZIP, bi)) {

            ArchiveEntry entry;
            while ((entry = i.getNextEntry()) != null) {
                Path outputPath = Paths.get(gameOutputDire.getAbsolutePath(), entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                } else {
                    Files.createDirectories(outputPath.getParent());
                    try (OutputStream o = Files.newOutputStream(outputPath)) {
                        IOUtils.copy(i, o);
                        //TODO replace by:Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return gameOutputDire;
    }



    private File gameOutputDir(File archiveFile, File outputDir) {
        boolean removeAllExtensions=false;
        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        String withoutExtension=archiveFile.getName().replaceAll(extPattern, "");
        return new File(outputDir.getAbsolutePath(), withoutExtension);
    }


}

