package com.alba.reader;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Date;

class ComicTest {

    private Comic comic;
    private long validTimestamp;
    private String expectedDateFormat;

    @BeforeEach
    void setUp() {
        // Set up a valid timestamp (e.g., current time)
        validTimestamp = System.currentTimeMillis() / 1000; // current time in seconds
        // Initialize the expected date format
        expectedDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(validTimestamp * 1000));
        // Create a sample JSONObject for metadata
        JSONObject metadata = new JSONObject();
        metadata.put("author", "John Doe");
        // Create a Comic instance
        comic = new Comic("Sample Comic", new ImageIcon(), false, true, validTimestamp, 1, 10, "/path/to/comic", metadata, "comicID123");
    }

    @Test
    void testComicInitialization() {
        assertEquals("Sample Comic", comic.title);
        assertNotNull(comic.thumbnail);
        assertFalse(comic.read);
        assertTrue(comic.favorite);
        assertEquals(expectedDateFormat, comic.lastOpened);
        assertEquals(1, comic.currentPage);
        assertEquals(10, comic.totalPages);
        assertEquals("/path/to/comic", comic.filePath);
        assertNotNull(comic.metadata);
        assertEquals("comicID123", comic.ID);
    }

    @Test
    void testConvertTimestampToDate() {
        String date = comic.convertTimestampToDate(validTimestamp);
        assertEquals(expectedDateFormat, date);
    }

    @Test
    void testSetThumbnail() {
        ImageIcon newThumbnail = new ImageIcon("/path/to/newThumbnail");
        comic.setThumbnail(newThumbnail);
        assertEquals(newThumbnail, comic.getThumbnail());
    }

    @Test
    void testGetFilePath() {
        assertEquals("/path/to/comic", comic.getFilePath());
    }
}
