package com.notamethod.ebox.api;

public class ApiException extends Exception {
    public ApiException(String s, Exception e) {
        super(s, e);
    }

    public ApiException(String s) {
        super(s);
    }
}
