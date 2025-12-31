package com.notamethod.fluppy.gui.common;


import com.notamethod.fluppy.api.ApiCalls;
import com.notamethod.fluppy.api.igdb.GameApiBean;
import com.notamethod.fluppy.core.game.GameApp;
import com.notamethod.fluppy.gui.AddGameDialog;
import com.notamethod.fluppy.gui.Messages;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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





    public void showErrors(List<String> errors) {
        da.showErrorDialog(errors);
    }
}