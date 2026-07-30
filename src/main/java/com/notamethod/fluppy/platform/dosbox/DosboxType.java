package com.notamethod.fluppy.platform.dosbox;

public enum DosboxType {
     UNKNOWN,X, STAGING,ECE, CLASSIC;

     public static DosboxType fromString(String dbtString) {
          for (DosboxType s: DosboxType.values()){
               if (s.toString().equalsIgnoreCase(dbtString)){
                    return s;
               }
          }
          return DosboxType.UNKNOWN;
     }
}
