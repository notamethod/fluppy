package com.notamethod.fluppy.gui.common;

import com.notamethod.fluppy.gui.Messages;

import java.io.IOException;

public class ProcessFileException extends Exception {
    public ProcessFileException(String key) {
        super(Messages.getString(key));
    }

    public ProcessFileException(String key, String value) {
        super(Messages.getString(key, value));
        // super(message);
    }

    public ProcessFileException(String key, IOException e) {
        super(Messages.getString(key), e);
    }
}
