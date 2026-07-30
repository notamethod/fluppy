package com.notamethod.fluppy.platform.dosbox;

public class EmulatorException extends Exception {
    public EmulatorException(int errorCode) {
        super("error code:" + errorCode);

    }
}
