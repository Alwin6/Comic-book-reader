package com.alba.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

class ComicRefinementTest {

    private List<Comic> comics;

    @BeforeEach
    void setUp() {
        comics = new ArrayList<>();
        comics.add(new Comic("Spider-Man", new ImageIcon(), true, false, System.currentTimeMillis(), 0, 100, "path/to/spiderman", new JSONObject(), "1"));
        comics.add(new Comic("Batman", new ImageIcon(), false, true, System.currentTimeMillis() - 10000, 0, 150, "path/to/batman", new JSONObject(), "2"));
        comics.add(new Comic("Superman: Red Son", new ImageIcon(), false, false, System.currentTimeMillis() - 5000, 50, 200, "path/to/superman", new JSONObject(), "3"));
        comics.add(new Comic("Wonder Woman", new ImageIcon(), false, true, System.currentTimeMillis() - 20000, 0, 120, "path/to/wonderwoman", new JSONObject(), "4"));
        comics.add(new Comic("Aquaman", new ImageIcon(), false, false, System.currentTimeMillis() - 15000, 30, 80, "path/to/aquaman", new JSONObject(), "5"));
    }

    // Search Tests

    @Test
    void testSearchComicsContainingMan() {
        List<Comic> searched = ComicRefinement.searchComics(comics, "man");
        assertEquals(5, searched.size());
    }

    @Test
    void testSearchComicsContainingWoman() {
        List<Comic> searched = ComicRefinement.searchComics(comics, "woman");
        assertEquals(1, searched.size());
    }

    @Test
    void testSearchComicsContainingRed() {
        List<Comic> searched = ComicRefinement.searchComics(comics, "Red");
        assertEquals(1, searched.size());
    }

    @Test
    void testSearchComicsEmptyResult() {
        List<Comic> searched = ComicRefinement.searchComics(comics, "Flash");
        assertTrue(searched.isEmpty());
    }

    @Test
    void testSearchComicsCaseInsensitive() {
        List<Comic> searched = ComicRefinement.searchComics(comics, "sUper");
        assertEquals(1, searched.size());
    }

    // Filter Tests
    @Test
    void testFilterComicsByRead() {
        List<Comic> filtered = ComicRefinement.filterComics(comics, "Read");
        assertEquals(1, filtered.size());
        assertEquals("Spider-Man", filtered.get(0).title);
    }

    @Test
    void testFilterComicsByFavorite() {
        List<Comic> filtered = ComicRefinement.filterComics(comics, "Favorite");
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(comic -> comic.title.equals("Batman")));
        assertTrue(filtered.stream().anyMatch(comic -> comic.title.equals("Wonder Woman")));
    }

    @Test
    void testFilterComicsByNotRead() {
        List<Comic> filtered = ComicRefinement.filterComics(comics, "Not read");
        assertEquals(4, filtered.size());
    }

    @Test
    void testFilterComicsByReading() {
        // Ensure the test comic is marked as currently reading (not read and not on page 1)
        List<Comic> filtered = ComicRefinement.filterComics(comics, "Reading");
        assertEquals(2, filtered.size());
    }

    @Test
    void testFilterComicsByNotFavorite() {
        List<Comic> filtered = ComicRefinement.filterComics(comics, "Not favorite");
        assertEquals(3, filtered.size());
    }
}
