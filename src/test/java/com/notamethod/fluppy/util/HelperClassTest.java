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
    void matchingAmiga() {
        String m= HelperClass.regexGroup("Another World (1991)(U.S. Gold)(Fr)[cr CPY](Disk 1 of 2).adf",HelperClass.REGEX_AMIGA,1);
        System.out.println(m);
    }
    @Test
    void matchingAmiga2() {
        HelperClass.SearchInfo m= HelperClass.regexAmiga("Another World (1991)(U.S. Gold)(Fr)(Disk 2 of 2).adf");
        System.out.println(m);
    }
    @Test
    void matchingSimpleZip() {
        String m= HelperClass.guessTitleFromFilename("Wipeout.zip");

    }


}