package com.notamethod.ebox.core;

import com.notamethod.ebox.gui.Messages;

public class GameManagerException extends Throwable {


    public GameManagerException(String s) {
        super(s);
    }

    public GameManagerException(String key, String value) {
        super(Messages.getString(key, value));
       // super(message);
    }
}
