package com.notamethod.fluppy.dosbox;

public class DosBoxException extends Exception {
    public DosBoxException(int dosboxErrorCode) {
        super("error code:"+dosboxErrorCode);

    }
}
