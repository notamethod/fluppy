
package com.notamethod.fluppy.util;

import com.notamethod.fluppy.core.Configuration;
import com.notamethod.fluppy.core.game.GameApp;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class HelperClass {

    public static final int LINUX = 0;
    public static final int SOLARIS = 1;
    public static final int WINDOWS = 2;
    public static final int MACOS = 3;
    public static final String REGEX_ABANDONWARE = "jeu-[0-9]{5}-.*";
    //public static final String REGEX_SIMPLE="*._DOS_??.zip";
    public static final String REGEX_SIMPLE = "(.*)_DOS_[A-Z][A-Z].*";
    private static final String FORBIDDEN_CHARS_NAME = "[\\\\/:*?\"<>|]";

    /**
     * Determines the system's OS
     *
     * @return the code for the current OS
     * @author Truben
     */
    public static int getOS() {
        String sysName = System.getProperty("os.name").toLowerCase();
        if (sysName.contains("linux"))
            return LINUX;
        else if (sysName.contains("windows"))
            return WINDOWS;
        else if (sysName.contains("solaris"))
            return SOLARIS;
        else if (sysName.contains("mac"))
            return MACOS;

        return -1; // if nothing's found
    }

    /**
     * Get and creates a app folder
     *
     * @param applicationName
     * @return the folder file
     * @author Truben
     */
    public static File getWorkingDirectory(final String applicationName) {

        final String userHome = System.getProperty("user.home", ".");
        final File workingDirectory;
        switch (getOS()) {
            case LINUX:
            case SOLARIS:
                workingDirectory = new File(userHome, '.' + applicationName + '/');
                break;
            case WINDOWS:
                final String applicationData = System.getenv("APPDATA");
                if (applicationData != null)
                    workingDirectory = new File(applicationData,  applicationName + '/');
                else
                    workingDirectory = new File(userHome, '.' + applicationName + '/');
                break;
            case MACOS:
                workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
                break;
            default:
                return new File(".");
        }
        if (!workingDirectory.exists())
            if (!workingDirectory.mkdirs())
                throw new RuntimeException("The working directory could not be created: " + workingDirectory);

        log.info("Working directory is " + workingDirectory.getAbsolutePath());
        return workingDirectory;
    }


    public static String getGameDirectory(String appName) {
        return getDirectory(appName, "games");
    }

    public static String getDataDirectory(String appName) {
        return getDirectory(appName, "data");
    }

    public static String getTempDirectory(String appName) {
        return getDirectory(appName, "temp");
    }

    public static String getDirectory(String appName, String dir) {
        File subDirectory = new File(appName, dir);
        if (!subDirectory.exists()) {
            if (!subDirectory.mkdirs()) {
                throw new RuntimeException("The game directory could not be created: " + subDirectory);
            }
        }
        return subDirectory.getAbsolutePath();
    }

    public static String getCoverDirectory(String appName) {

        return getDirectory(appName, "covers");
    }

    public static boolean gameIsInTempDir(GameApp d) {
        Path parent = Paths.get(Configuration.tempFolder);
        Path child = d.getGamePath();

        // Vérification
        return child.normalize().startsWith(parent.normalize());
    }

    public static void moveGameToGames(GameApp d) throws IOException {
        Path tempParent = Paths.get(Configuration.tempFolder);
        Path child = d.getGamePath();
        Path pathToMove = getDirToMove(tempParent, child);
        String endTarget = pathToMove.getFileName().toString();
        Path target = Paths.get(Configuration.gamesFolder).resolve(endTarget);
        moveDirectory(pathToMove, target);

        d.setGamePath(Paths.get(d.getGamePath().toString().replace(Configuration.tempFolder, Configuration.gamesFolder)));
        d.setExePath(Paths.get(d.getExePath().toString().replace(Configuration.tempFolder, Configuration.gamesFolder)));
    }

    private static Path getDirToMove(Path tempParent, Path child) {
        boolean sameName = tempParent.getFileName().equals(child.getParent().getFileName());
        if (sameName) {
            return child;
        } else {
            return getDirToMove(tempParent, child.getParent());
        }
    }

    public static void moveDirectory(Path sourceDir, Path targetDir) throws IOException {

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetDir.resolve(sourceDir.relativize(dir));
                Files.createDirectories(targetPath);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetPath = targetDir.resolve(sourceDir.relativize(file));
                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });

        // Optionnel : supprimer le dossier source une fois déplacé
        Files.walk(sourceDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static void deleteDirectory(Path sourceDir) throws IOException {
        Files.walk(sourceDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static String guessTitleFromFilename(String name) {
        if (name == null) {
            return null;
        }
        String title = regexArchive(name);
        if (title == null) {
            title = regexGroup(name, REGEX_SIMPLE, 1);
        }
        if (title == null) {
            int pos = name.lastIndexOf(".");
            title = pos > 0 ? name.substring(0, pos) : name;
        }
        return toTitleGame(title);

    }

    public static String fromCamelCase(String nameWithCamelCase) {
        String converted = nameWithCamelCase.replaceAll("([a-z])([A-Z])", "$1 $2");

        // Mettre la première lettre en majuscule si nécessaire
        if (!Character.isUpperCase(converted.charAt(0))) {
            converted = Character.toUpperCase(converted.charAt(0)) + converted.substring(1);
        }
        return converted;
    }

    public static String regexArchive(String name) {
        Pattern pattern = Pattern.compile(REGEX_ABANDONWARE);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            String[] data = name.split("-");
            return data[2];
        }
        return null;

    }

    public static String regexGroup(String name, String regx, int group) {
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(group);
        }
        return null;
    }

    public static String toTitleGame(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ");
    }

    public static void cleanDirectory(Path dir) throws IOException {
        log.debug("cleaning {}", dir);
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); // Supprime chaque fichier
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path subDir, IOException exc) throws IOException {
                if (!subDir.equals(dir)) {
                    Files.delete(subDir); // Supprime les sous-répertoires
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static String getCaptureDirectory(GameApp di) {
        return Configuration.appFolder + "captures" + File.separator + di.getId() + File.separator;
    }

    public static void addOtherSettings(String[][] finito, String section, HashMap<String, String> props) {
        for (int i = 0; i < finito.length; i++) {
            if (finito[i][0].toLowerCase().equals(section.toLowerCase())) {
                props.put(finito[i][1], finito[i][2]);
            }
        }
    }

    public static String sanitizeName(String name) {
        String sanitizedName = name.replaceAll(FORBIDDEN_CHARS_NAME, "");
        return sanitizedName.replace(" ", "").toLowerCase();

    }

    public static String getExtraDirectory(String appName) {

        return getDirectory(appName, "extras");
    }

    public static String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase();
    }
}
