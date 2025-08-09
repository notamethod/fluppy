package com.notamethod.ebox.core;

public class DosBoxException extends Exception {
    public DosBoxException(int dosboxErrorCode) {
        super("error code:"+dosboxErrorCode);

    }
}
