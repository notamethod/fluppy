package com.notamethod.fluppy.api;

import java.io.IOException;

public class MappingException extends RuntimeException {
    public MappingException(IOException e) {
        super(e);
    }
}
