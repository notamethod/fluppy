package com.notamethod.fluppy.emulators.dosbox;

public class DosBoxResult {
    public final boolean success;
    public final long durationMillis;
    public final int exitCode;
    public final Exception error;

    public DosBoxResult(boolean success, long durationMillis, int exitCode, Exception error) {
        this.success = success;
        this.durationMillis = durationMillis;
        this.exitCode = exitCode;
        this.error = error;
    }
}

