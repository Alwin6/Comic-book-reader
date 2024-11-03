package com.alba.reader;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ComicPageTest {

    @Test
    void testGetWidthAndHeight() {
        BufferedImage image = new BufferedImage(100, 150, BufferedImage.TYPE_INT_ARGB);
        ComicPage comicPage = new ComicPage(image);

        assertEquals(100, comicPage.getWidth());
        assertEquals(150, comicPage.getHeight());
    }

    @Test
    void testNullImage() {
        ComicPage emptyPage = new ComicPage(null);
        assertEquals(0, emptyPage.getWidth());
        assertEquals(0, emptyPage.getHeight());
    }
}
