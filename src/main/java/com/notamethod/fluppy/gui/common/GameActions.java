package com.notamethod.fluppy.gui.common;


import com.notamethod.fluppy.api.ApiCalls;
import com.notamethod.fluppy.api.ApiException;
import com.notamethod.fluppy.api.MappingException;
import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.api.igdb.Genre;
import com.notamethod.fluppy.core.*;
import com.notamethod.fluppy.gui.AddGameDialog;
import com.notamethod.fluppy.gui.Messages;
import com.notamethod.fluppy.util.ArchiveExtractor;
import com.notamethod.fluppy.util.FileWizard;
import com.notamethod.fluppy.util.HelperClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class GameActions {

    DialogActions da;

    public GameActions(DialogActions da) {
        this.da = da;
    }

    public GameApiBean chooseGame(List<GameApiBean> games) {
        Map<String, GameApiBean> map = new HashMap<>();
        List<String> choices = new ArrayList<>();
        for (GameApiBean game : games) {
            String info = game.getName() + " (" + game.getYear() + ")";
            map.put(info, game);
            choices.add(info);
        }
        choices.add("Something else...");
        String[] choiceArray = choices.toArray(String[]::new);
        Optional<String> chosen = da.showListInputDialog(
                "What's the name of the game?",
                "What is the title of the application? Select one of the proposals,\n" +
                        "or select \"Something else...\" to type your own.",
                choiceArray, choiceArray[0]);

        return map.get(chosen.orElse(""));
    }


    private List<GameApiBean> findGame(String name, ApiCalls apiCalls) {
        // searching game
        String response = "";
        final List<GameApiBean> games = new ArrayList<>();
        try {
            games.addAll(apiCalls.findGame(name));
        } catch (ApiException | MappingException e) {
            log.error("Internal Error", e);
        }
        if (!games.isEmpty()) {
            return games;
        }
        //no games found
        Optional<String> resp = Optional.empty();
        while ((resp = da.showInputDialog("No informations", "No informations for this games found with API, please entre another name to search or cancel to stay with the existing name")).isPresent()) {
            String valeur = resp.get();
            // Traitement avec la valeur
            System.out.println("Traitement de : " + valeur);
            try {
                games.addAll(apiCalls.findGame(valeur));
                if (!games.isEmpty()) {
                    break;
                }
            } catch (ApiException | MappingException e) {
                log.error("Internal Error", e);
            }
        }
//
        return games;
    }


    private GameApp addArchive(File file) throws GameManagerException, OperationCanceledException {
        GameApp mgame;// = new GameApp();
        ArchiveExtractor extractor = new ArchiveExtractor(Configuration.tempFolder);

        try {
            File path = extractor.extractFile(file);
            mgame = addDirectory(path);
            return mgame;
        } catch (IOException e) {
            throw new GameManagerException("exception.archive.extract");
        }
    }

    private GameApp addDirectory(File inFile) throws GameManagerException, OperationCanceledException {
        GameApp mgame = new GameApp();
        mgame.setGamePath(inFile.toPath());

        log.info("analyze directory {}", inFile.getAbsolutePath());
        FileWizard fw = new FileWizard();
        List<File> runners = fw.getRunners(inFile);
        int count = runners.size();
        if (count == 0) {
            throw new GameManagerException("exception.noexec", inFile.getAbsolutePath());
        }

        //File[] possible = new String[count];
        List<File> possible = new ArrayList<>();

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


    public List<GameApp> createFromFiles(List<File> inFiles) {
        List<GameApp> gameAppList = new ArrayList<>();
        boolean doAction = true;
        if (inFiles.size() > 1) {
            doAction = da.showConfirmDialog(Messages.getString("confirmation.import.header"),
                    Messages.getString("confirmation.import.multifiles"));
        }
        if (!doAction)
            return gameAppList;

        ApiCalls apiCalls = new ApiCalls();
        AddGameDialog dialog = new AddGameDialog(inFiles, apiCalls);
        gameAppList = dialog.showAndWaitForResult();


        return gameAppList;
    }



    private String calculateSearchString(GameApp metaGame, String sourceFileName) {
        String guessSource = null;
        File exeFile = null;
        String searchString="";
        if (metaGame.getExePath() != null) {
            exeFile = metaGame.getExePath().toFile();
            guessSource = exeFile.getName();
            metaGame.setGameExe(exeFile.getName());
            metaGame.setExePath(Paths.get(exeFile.getAbsolutePath().substring(0, exeFile.getAbsolutePath().lastIndexOf(File.separatorChar))));
             searchString = exeFile.getParentFile().getAbsolutePath().substring(exeFile.getParentFile().getAbsolutePath().lastIndexOf(File.separator) + 1);
        }


        //TODO: set intallers


        if (sourceFileName != null) {
            String title = HelperClass.guessTitleFromFilename(sourceFileName);
            if (title != null) {

                searchString = title;
            }
        }
        return HelperClass.fromCamelCase(searchString);
    }



    public void showErrors(List<String> errors) {
        da.showErrorDialog(errors);
    }
}