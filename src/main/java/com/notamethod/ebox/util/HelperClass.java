/**
 * @author Truben
 */

package com.notamethod.ebox.util;

import com.notamethod.ebox.api.igdb.Game;
import com.notamethod.ebox.app.ApplicationBean;
import com.notamethod.ebox.app.Configuration;
import com.notamethod.ebox.app.GameApp;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class HelperClass {

    public static final int LINUX   = 0;
    public static final int SOLARIS = 1;
    public static final int WINDOWS = 2;
    public static final int MACOS   = 3;

    /**
     * Determines the system's OS
     * @return the code for the current OS
     */
    public static int getOS() {
        String sysName = System.getProperty("os.name").toLowerCase();
        if(sysName.contains("linux"))
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
     * @param applicationName
     * @return the folder file
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
                    workingDirectory = new File(applicationData, "." + applicationName + '/');
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

        log.info("Working directory is "+workingDirectory.getAbsolutePath());
        return workingDirectory;
    }

    public static boolean isMac() {
        if(Configuration.pref.getTypeOfFileDialog() == 1)
            return false;
        if(Configuration.pref.getTypeOfFileDialog() == 2)
            return true;
        if(Configuration.pref.getTypeOfFileDialog() == 0 && "Mac OS X".equals(System.getProperty("os.name")))
            return true;
        return false;
    }

    /**
     *
     * Method that shows a file chooser to the user and use the last used path as
     * its starting point
     *
     * @param c the parent component
     * @param header The dialog's header
     * @param filter What should be shown
     * @param directories should we show directories
     * @return
     */
    public static String showFileChooser(Component c, String header,
                                         FileChooserFilter filter, boolean directories) {

        return showFileChooser(c,header,filter,directories, Configuration.pref.getLastUsedPath());
    }

    /**
     * Method that shows a file chooser to the user
     *
     * @param c the parent component
     * @param header The dialog's header
     * @param filter What should be shown
     * @param directories should we show directories
     * @param startDir the starting path
     * @return
     */
    public static String showFileChooser(Component c, String header,
                                         FileChooserFilter filter, boolean directories, String startDir) {
        if(isMac()) { // AWT
            FileDialog fd = new FileDialog((Frame)c.getParent(), header, FileDialog.LOAD);

            if(!startDir.equals(""))
                fd.setDirectory(startDir); // back to where we were

            fd.pack();
            fd.setVisible(true);

            if (fd.getFile() != null) {
                Configuration.pref.setLastUsedPath(fd.getDirectory());
                if(directories)
                    return fd.getDirectory();
                else
                    return fd.getDirectory() + fd.getFile();
            }
            else
                return null;
        }
        else {  // SWING
            final JFileChooser fc = new JFileChooser();
            if(directories) fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);

            if(!startDir.equals(""))
                fc.setCurrentDirectory(new File(startDir)); // back to where we were

            fc.addChoosableFileFilter(filter);
            int returnVal = fc.showOpenDialog(c);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                Configuration.pref.setLastUsedPath(file.getAbsolutePath());
                if(directories)
                    return file.getAbsolutePath();
                else
                    return file.getAbsolutePath();
            }
            else
                return null;
        }
    }
    /**
     * Copy a file
     * @param in the file you want to copy
     * @param out the new file
     * @throws IOException
     */
    public static void copyFile(File in, File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(),
                    outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public static String getGameDirectory(String appName) {
        return  getDirectory(appName, "games");
    }
    public static String getTempDirectory(String appName) {
        return  getDirectory(appName, "temp");
    }

    public static String getDirectory(String appName, String dir) {
        File subDirectory= new File(appName, dir);
        if (!subDirectory.exists()) {
            if (!subDirectory.mkdirs()) {
                throw new RuntimeException("The game directory could not be created: " + subDirectory);
            }
        }
        return  subDirectory.getAbsolutePath();
    }

    public static String getCoverDirectory(String appName) {
        return  getDirectory(appName, "covers");
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
        String endTarget= pathToMove.getFileName().toString();
        Path target = Paths.get(Configuration.gamesFolder).resolve(endTarget);
        moveDirectory(pathToMove, target);

        d.setGamePath(Paths.get(d.getGamePath().toString().replace(Configuration.tempFolder, Configuration.gamesFolder)));
        d.setExePath(Paths.get(d.getExePath().toString().replace(Configuration.tempFolder, Configuration.gamesFolder)));
    }

    private static Path getDirToMove(Path tempParent, Path child) {
        boolean sameName = tempParent.getFileName().equals(child.getParent().getFileName());
        if (sameName){
            return child;
        }else{
            return getDirToMove( tempParent,  child.getParent());
        }
    }

    public static void moveDirectory( Path sourceDir, Path targetDir) throws IOException {


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
    public boolean restartApplication( Object classInJarFile ) {
        String javaBin = System.getProperty("java.home") + "/bin/java";
        File jarFile;
        try{
            jarFile = new File
            (classInJarFile.getClass().getProtectionDomain()
            .getCodeSource().getLocation().toURI());
        } catch(Exception e) {
            return false;
        }

        /* is it a jar file? */
        if ( !jarFile.getName().endsWith(".jar") )
        return false;   //no, it's a .class probably

        String  toExec[] = new String[] { javaBin, "-jar", jarFile.getPath() };
        try{
            Process p = Runtime.getRuntime().exec( toExec );
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        System.exit(0);

        return true;
    }

    public static  Game regexArchive(String name){
        Pattern pattern = Pattern.compile("jeu-[0-9]{5}-.*");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()){

            String[] data=name.split("-");
            Game metaDataGame = new Game(data[2]);
            return metaDataGame;
        }
        return null;

    }

    public static void cleanDirectory(Path dir) throws IOException {
        log.info("cleaning {}", dir);
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

    public static String getCaptureDirectory(ApplicationBean di) {
        return Configuration.appFolder + "captures" + File.separator + di.getUniqueID() + File.separator;
    }

    public static void addOtherSettings(String[][] finito, String section, HashMap<String, String> props) {
        for (int i = 0; i < finito.length; i++) {
            if (finito[i][0].toLowerCase().equals(section.toLowerCase())) {
                props.put(finito[i][1], finito[i][2]);
            }
        }
    }
}
