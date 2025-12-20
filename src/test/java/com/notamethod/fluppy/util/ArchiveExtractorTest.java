package com.notamethod.fluppy.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
class ArchiveExtractorTest {


    @TempDir
    Path tempDir;

    @Test
    void extract7zFile() throws IOException {
        URL resourceUrl = getClass().getResource("/jeu-01959-round42-PCDos.7z");
        assertNotNull(resourceUrl);
        File file=null;
        try {
             file = new File(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            fail(e);
        }
        ArchiveExtractor extractor = new ArchiveExtractor(tempDir.toFile().getCanonicalPath());

        try {
            File out=extractor.extractFile(file, true);
            assertTrue(out.exists());
            //assertTrue(out.getAbsolutePath().contains(TEST_DIR));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

    }
    @Test
    void extractZip() throws IOException {
        URL resourceUrl = getClass().getResource("/round42.zip");
        assertNotNull(resourceUrl);
        File file=null;
        try {
            file = new File(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            fail(e);
        }
        ArchiveExtractor extractor = new ArchiveExtractor(tempDir.toFile().getCanonicalPath());

        try {
            File out=extractor.extractFile(file, true);
            assertTrue(out.exists());
        } catch (IOException e) {
            e.printStackTrace();
            fail();

        }

    }

}