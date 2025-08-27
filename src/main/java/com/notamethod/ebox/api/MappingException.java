package com.notamethod.ebox.api;

import java.io.IOException;

public class MappingException extends RuntimeException {
    public MappingException(IOException e) {
        super(e);
    }
}
