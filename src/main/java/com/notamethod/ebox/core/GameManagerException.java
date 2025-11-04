package com.notamethod.ebox.core;

import com.notamethod.ebox.gui.Messages;

public class GameManagerException extends Throwable {


    public GameManagerException(String key) {
        super(Messages.getString(key));
    }

    public GameManagerException(String key, String value) {
        super(Messages.getString(key, value));
       // super(message);
    }
}
