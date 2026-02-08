package com.notamethod.fluppy.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class HelperClassTest {

    @Test
    void matchingTest() {
       String m= HelperClass.regexGroup("Phantom-Fighter_DOS_EN",HelperClass.REGEX_SIMPLE,1);


    }
    @Test
    void matchingZip() {
        String m= HelperClass.regexGroup("Phantom-Fighter_DOS_EN.zip",HelperClass.REGEX_SIMPLE,1);

    }

    @Test
    void matchingSimpleZip() {
        String m= HelperClass.guessTitleFromFilename("Wipeout.zip");

    }


}