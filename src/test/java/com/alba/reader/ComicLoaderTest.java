package com.alba.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ComicLoaderTest {

    private File nhlFile;
    private File rarFile;
    private File zipFile;
    private JProgressBar progressBar;
    private ComicLoader comicLoader;

    @BeforeEach
    public void setUp() {
        // Set up test files and progress bar
        zipFile = new File("src/test/resources/Atomic_Comic__Fudge_ca._1947___UK_.cbz");
        nhlFile = new File("src/test/resources/pepper&carrot_1.nhlcomic");
        rarFile = new File("src/test/resources/THEGREATEST.cbr");
        progressBar = new JProgressBar();
    }

    @Test
    public void testLoadNhlComic() throws Exception {
        comicLoader = new ComicLoader(nhlFile, progressBar);
        comicLoader.loadComicInBackground();

        Thread.sleep(1000);

        assertNotNull(comicLoader.getComicBook(), "ComicBook should be loaded.");
    }

    @Test
    public void testLoadRarComic() throws Exception {
        comicLoader = new ComicLoader(rarFile, progressBar);
        comicLoader.loadComicInBackground();

        // Wait for the loading to complete
        Thread.sleep(1000);

        assertNotNull(comicLoader.getComicBook(), "ComicBook should be loaded.");
    }

    @Test
    public void testLoadZipComic() throws Exception {
        comicLoader = new ComicLoader(zipFile, progressBar);
        comicLoader.loadComicInBackground();

        // Wait for the loading to complete
        Thread.sleep(1000);

        assertNotNull(comicLoader.getComicBook(), "ComicBook should be loaded.");
    }
}
