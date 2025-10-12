package com.notamethod.ebox.gui.common;


import com.notamethod.ebox.api.ApiCalls;
import com.notamethod.ebox.api.ApiException;
import com.notamethod.ebox.api.ApiMapper;
import com.notamethod.ebox.api.MappingException;
import com.notamethod.ebox.api.igdb.Game;
import com.notamethod.ebox.api.igdb.Genre;
import com.notamethod.ebox.core.*;
import com.notamethod.ebox.gui.Messages;
import com.notamethod.ebox.util.ArchiveExtractor;
import com.notamethod.ebox.util.FileWizard;
import com.notamethod.ebox.util.HelperClass;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

    public Game chooseGame(List<Game> games) {
        Map<String, Game> map = new HashMap<>();
        List<String> choices = new ArrayList<>();
        for (Game game : games) {
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


    private List<Game> findGame(String name, ApiCalls apiCalls) {
        // searching game
        String response = "";
        final List<Game> games = new ArrayList<>();
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
        ArchiveExtractor extractor = new ArchiveExtractor();
        String output = Configuration.tempFolder;
        try {
            File path = extractor.extractFile(file, new File(output));
            mgame = addDirectory(path);
            return mgame;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GameApp addDirectory(File inFile) throws GameManagerException, OperationCanceledException {
        GameApp mgame = new GameApp();
        mgame.setGamePath(inFile.toPath());
        File exeFile;
        log.info("analyze directory {}", inFile.getAbsolutePath());
        FileWizard fw = new FileWizard();
        List<File> runners = fw.getRunners(inFile);
        int count = runners.size();
        if (count == 0) {
            throw new GameManagerException("exception.noexec",  inFile.getAbsolutePath());
        }

        String[] possible = new String[count];
        count = 0;
        for (File f : runners) {
            if (f.toString().toLowerCase().indexOf("setup") == -1 && f.toString().toLowerCase().indexOf("install") == -1 && (f.toString().toLowerCase().endsWith("pif") || f.toString().toLowerCase().endsWith("exe") || f.toString().toLowerCase().endsWith("com") || f.toString().toLowerCase().endsWith("bat"))) {
                possible[count++] = f.getName();
            }
        }
        String chosen = possible[0];
        if (count > 1) {
            Optional<String> o = da.showListInputDialog(
                    Messages.getString("dialog.select_executable.header"),
                    Messages.getString("dialog.select_executable.content", mgame.getGamePath()),
                    possible, possible[0]);
            if (o.isPresent()) {
                chosen = o.get();
            } else {
                throw new OperationCanceledException("Canceled");
            }
        }

        for (File f : runners) {
            if (f.toString().endsWith(chosen)) {
                exeFile = f;
                mgame.setExePath(exeFile.toPath());
            }
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
        for (File f : inFiles) {
            try {
               // GameApp beanGame = createFromFile(f.getAbsoluteFile());
                createFromFile(f.getAbsoluteFile()).ifPresent(gameAppList::add);
                //gameAppList.add(beanGame);
            } catch (GameManagerException e) {
                da.showMessageDialog("importation error", e.getMessage());
                log.error("importation error", e);
            } catch (OperationCanceledException e) {
                log.error("cancel game", e);
            }
        }
        return gameAppList;
    }

    /**
     * A metod that tries to insert an application into
     * dbox' database using a file or directory
     *
     * @return
     */
    public Optional<GameApp> createFromFile(File inFile) throws GameManagerException, OperationCanceledException {
        //File exeFile = inFile;
        //MetaDataGame metaDataGame;
        String guessSource = null;
        GameApp metaGame = null;
        if (inFile.isDirectory()) {
            metaGame = addDirectory(inFile);
        } else if (ArchiveExtractor.isArchive(inFile)) {
            metaGame = addArchive(inFile);
            File exeFile = metaGame.getExePath().toFile();
            guessSource = exeFile.getName();
        } else if (metaGame == null && (!inFile.getName().toLowerCase().endsWith("exe") && !inFile.getName().toLowerCase().endsWith("com") && !inFile.getName().toLowerCase().endsWith("bat") && !inFile.getName().toLowerCase().endsWith("pif"))) {

            if (!da.showConfirmDialog("You're almost there...", "This doesn't look like an executable file. Executable files normally ends with .bat, .exe or .com.\n\nDo you still want to continue?"))
                return null;
        } else {
            metaGame = new GameApp();
        }

        try {
            File exeFile = metaGame.getExePath().toFile();
            metaGame.setGameExe(exeFile.getName());
            metaGame.setExePath(Paths.get(exeFile.getAbsolutePath().substring(0, exeFile.getAbsolutePath().lastIndexOf(File.separatorChar))));
            File[] files = exeFile.getParentFile().listFiles();
            for (File f : files) {
                String s = f.getName().toLowerCase();
                if (s.endsWith("exe") || s.endsWith("bat") || s.endsWith("com")) {
                    if (s.indexOf("setup") != -1 || s.indexOf("install") != -1) {
                        metaGame.setInstaller(f.toString().substring(f.toString().lastIndexOf(File.separator) + File.separator.length()));
                    }
                }
            }
           // String[] choice = new String[2];
           // choice[0] = exeFile.getParentFile().getAbsolutePath().substring(exeFile.getParentFile().getAbsolutePath().lastIndexOf(File.separator) + 1);
            String searchString=exeFile.getParentFile().getAbsolutePath().substring(exeFile.getParentFile().getAbsolutePath().lastIndexOf(File.separator) + 1);

            if (guessSource != null) {
                String title=HelperClass.guessTitleFromFilename(inFile.getName());
                if (title != null) {

                    searchString= title;
                }
            }


            Optional<String> input = da.showInputDialog(
                    null, Messages.getString("dialog.select_appname.text"),
                    searchString);

            if (input.isEmpty()) {
                return Optional.empty();
            }else{
                metaGame.setName(searchString);
            }

//            if (input.equals(choice[1])) {
//                da.showInputDialog("name ?", "Type the name of the application", choice[1]).ifPresent(metaGame::setName);
//            } else {
//                metaGame.setName(input);
//            }

            ApiCalls apiCalls = new ApiCalls();
            List<Game> games = findGame(metaGame.getName(), apiCalls);

            if (games.isEmpty()) {
                log.info("no games found through API");
                if (!da.showConfirmDialog("Game not found", "Adding game " + metaGame.getName() + " to the list ?")) {
                    return Optional.empty();
                } else {
                    Game dummyGame = new Game();
                    dummyGame.setName(metaGame.getName());
                    games.add(dummyGame);
                }
            }


            Game game = games.size() > 1 ? chooseGame(games) : games.get(0);
            if (game != null) {
               // GameApp filledGame = ApiMapper.INSTANCE.toGameApp(game);
               // List<GenreApp> genres = ApiMapper.INSTANCE.toGenres(game.getGenres());
                metaGame.setName(game.getName());
                //FIXME
                if (game.getGenres()!=null){
                for (Genre genre:game.getGenres()) {
                    GenreApp genraApp = new GenreApp();
                    genraApp.setId(genre.getSlug());
                    genraApp.setName(genre.getName());
                    metaGame.getGenres().add(genraApp);
                }
                }
                //metaGame.getGenres().addAll(genres);
                metaGame.setYear(game.getYear() == null ? 1970 : Integer.valueOf(game.getYear()));
                if (game.getCover() != null) {
                    try {
                        String coverFilename = "cover_" + game.getName().replace(" ", "").toLowerCase() + game.getYear();
                        metaGame.setImagePath(Paths.get(apiCalls.getCover(Configuration.coverFolder, coverFilename, game.getCover(), 2)));
                    } catch (ApiException | MappingException e) {
                        throw new RuntimeException(e);
                    }
                }
                //TODO: cover
            }else{
                return Optional.empty();
            }

            if (!da.showConfirmDialog("null", "Adding game " + metaGame.getName() + " to the list ?" )) {
                return Optional.empty();
            }

            return Optional.of(metaGame);
        } catch (Exception e) {
            da.showMessageDialog("Something wrong happened. You have to add the application the hard way.", "Sorry...");
            log.error("error", e);
        }
        return Optional.empty();
    }


    public void showErrors(List<String> errors) {
        da.showErrorDialog(errors);
    }
}