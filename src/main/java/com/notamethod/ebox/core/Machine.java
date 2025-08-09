package com.notamethod.ebox.core;

import lombok.val;

public enum Machine {
    hercules("Hercules"), cga("CGA"), ega("ega"), pcjr("pcjr"), tandy("tandy"), svga_s3("SVGA (default)");

    private String label;
    Machine(String label) {
        this.label=label;
    }
}
