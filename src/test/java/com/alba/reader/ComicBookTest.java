package com.alba.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ComicBookTest {
    private ComicPage[] pages;
    private ComicBook comicBook;

    @BeforeEach
    void setUp() {
        // Create mock BufferedImages for testing
        BufferedImage image1 = new BufferedImage(100, 150, BufferedImage.TYPE_INT_ARGB);
        BufferedImage image2 = new BufferedImage(200, 250, BufferedImage.TYPE_INT_ARGB);
        BufferedImage image3 = new BufferedImage(300, 350, BufferedImage.TYPE_INT_ARGB);

        pages = new ComicPage[]{
                new ComicPage(image1),
                new ComicPage(image2),
                new ComicPage(image3)
        };
        comicBook = new ComicBook("My Comic Book", pages);
    }

    @Test
    void testGetTitle() {
        assertEquals("My Comic Book", comicBook.title());
    }

    @Test
    void testGetPages() {
        ComicPage[] retrievedPages = comicBook.pages();
        assertArrayEquals(pages, retrievedPages);
    }

    @Test
    void testGetPageCount() {
        assertEquals(3, comicBook.getPageCount());
    }

    @Test
    void testGetPageValidIndex() {
        assertEquals(pages[0], comicBook.getPage(0));
        assertEquals(pages[1], comicBook.getPage(1));
        assertEquals(pages[2], comicBook.getPage(2));
    }

    @Test
    void testGetPageInvalidIndexLow() {
        assertThrows(IndexOutOfBoundsException.class, () -> comicBook.getPage(-1));
    }

    @Test
    void testGetPageInvalidIndexHigh() {
        assertThrows(IndexOutOfBoundsException.class, () -> comicBook.getPage(3));
    }
}
