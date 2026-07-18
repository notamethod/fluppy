package com.notamethod.fluppy.util;

public class DosUtils {

    public static int getCyclesForYear(int annee) {
        if (annee <= 1984) return 400;    // XT/8086
        if (annee <= 1990) return 2000;   // 286
        if (annee <= 1994) return 6000;   // 386
        if (annee <= 1997) return 15000;  // 486
        return 30000;                     // Pentium
    }
}
