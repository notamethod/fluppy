package com.notamethod.fluppy.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
class ArchiveExtractorTest {

    static final String TEST_DIR="fluppytest";
    static final String ROOT_DIR=System.getProperty("java.io.tmpdir")+TEST_DIR;
    File rootDirFile =  new File(ROOT_DIR);
    @BeforeEach
    void tempDir() throws IOException {


        if (rootDirFile.exists()){
            Files.walk(rootDirFile.toPath()) // parcours récursif
                    .sorted((a, b) -> b.compareTo(a)) // supprime enfants avant parents
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
        rootDirFile.mkdirs();
    }

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
        ArchiveExtractor extractor = new ArchiveExtractor(rootDirFile.getCanonicalPath());

        try {
            File out=extractor.extractFile(file, true);
            assertTrue(out.exists());
            assertTrue(out.getAbsolutePath().contains(TEST_DIR));
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
        ArchiveExtractor extractor = new ArchiveExtractor(rootDirFile.getCanonicalPath());

        try {
            File out=extractor.extractFile(file, true);
            assertTrue(out.exists());
            assertTrue(out.getAbsolutePath().contains(TEST_DIR));
        } catch (IOException e) {
            e.printStackTrace();
            fail();

        }

    }

}