package com.notamethod.ebox.util;

import com.notamethod.ebox.core.Configuration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.fail;

class HelperClassTest {

    @Test
    void matchingTest() {
       String m= HelperClass.regexGroup("Phantom-Fighter_DOS_EN",HelperClass.REGEX_SIMPLE,1);
        System.out.println(m);

    }


}