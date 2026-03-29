package com.notamethod.fluppy.core.game;

import com.notamethod.fluppy.gui.Messages;

import java.io.IOException;

public class GameAlreadyPresentException extends GameManagerException {

    public GameAlreadyPresentException(String key) {
        super(Messages.getString(key));
    }

    public GameAlreadyPresentException(String key, String value) {
        super(Messages.getString(key, value));
       // super(message);
    }

    public GameAlreadyPresentException(String key, IOException e) {
        super(Messages.getString(key), e);
    }
}
