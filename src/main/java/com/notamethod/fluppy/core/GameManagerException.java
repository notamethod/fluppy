package com.notamethod.fluppy.core;

import com.notamethod.fluppy.gui.Messages;

import java.io.IOException;

public class GameManagerException extends Throwable {


    public GameManagerException(String key) {
        super(Messages.getString(key));
    }

    public GameManagerException(String key, String value) {
        super(Messages.getString(key, value));
       // super(message);
    }

    public GameManagerException(String key, IOException e) {
        super(Messages.getString(key), e);
    }
}
