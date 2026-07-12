package com.notamethod.fluppy;

import org.slf4j.Logger;

import java.util.List;

public class SafeLog {


    public static <T> void debug(Logger log, String s, List<T> unsafeList) {
        if (unsafeList == null || unsafeList.isEmpty()) {
            log.debug(s + " is empty");
        } else {
            log.debug(s + " {} elements. First is {}", unsafeList.size(), unsafeList.getFirst().toString());
        }
    }
}
