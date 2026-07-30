package com.notamethod.fluppy.gui.common;

import lombok.Getter;

@Getter
public enum FileFormat {
    DIRECTORY("dir"), ARCHIVE("archive"), AMIGA_ADF("adf"), DOS_IMAGE("img"), UNKNOWN("unknown");

    private String dbValue;

    FileFormat(String value) {
        this.dbValue = value;
    }

    public static FileFormat fromValue(String value) {
        if (value == null) return UNKNOWN;
        for (FileFormat fileFormat : values()) {
            if (fileFormat.getDbValue().equalsIgnoreCase(value)) {
                return fileFormat;
            }
        }
        return UNKNOWN;
    }
}
