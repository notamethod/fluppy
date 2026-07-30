package com.notamethod.fluppy.util;

import lombok.Data;

@Data
public class FileType {

    public enum BASE_TYPE {PROTECTION, MANUAL, PUBLISHER, COVER, EXTRA_DISK}

    private BASE_TYPE baseType;
    private Integer diskNumber;

    public FileType(BASE_TYPE baseType) {
        this.baseType = baseType;
    }

}
