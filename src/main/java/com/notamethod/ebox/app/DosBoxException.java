package com.notamethod.ebox.app;

public class DosBoxException extends Exception {
    public DosBoxException(int dosboxErrorCode) {
        super("error code:"+dosboxErrorCode);

    }
}
