package com.notamethod.ebox.api;

public class ApiException extends Throwable {
    public ApiException(String s, Exception e) {
        super(s, e);
    }
}
