package com.notamethod.fluppy.util;

import org.junit.jupiter.api.Test;

import static com.notamethod.fluppy.util.HelperClass.REGEX_AMIGA;
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
        String m= HelperClass.regexGroup("Another World (1991)(U.S. Gold)(Fr)[cr CPY](Disk 1 of 2).adf", REGEX_AMIGA,1);
        System.out.println(m);

    }
    @Test
    void matchingAmiga2() {
        SearchInfo m= HelperClass.regexAmiga("Another World (1991)(U.S. Gold)(Fr)(Disk 2 of 2).adf");
        System.out.println(m);
        m= HelperClass.regexAmiga("Cobra (1991)(Bytec Software)[cr CPY](Disk 1 of 2).adf");
        System.out.println(m);
        System.out.println(REGEX_AMIGA);
    }
    @Test
    void matchingSimpleZip() {
        SearchInfo m= HelperClass.parseFileName("Wipeout.zip",null);
        System.out.println(m.title());

    }


}