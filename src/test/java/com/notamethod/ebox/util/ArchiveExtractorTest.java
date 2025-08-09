package com.notamethod.ebox.util;

import com.notamethod.ebox.core.Configuration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
class ArchiveExtractorTest {

    @Test
    void extractFile() {
        ArchiveExtractor extractor = new ArchiveExtractor();
        String output = Configuration.tempFolder;
        try {
            extractor.extractFile(new File("C:\\Users\\Christophe\\Downloads\\jeu-01959-round42-PCDos.7z"),new File(output));
        } catch (IOException e) {
            e.printStackTrace();
            fail();

        }

    }


}