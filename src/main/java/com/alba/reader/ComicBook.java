package com.alba.reader;

public record ComicBook(String title, ComicPage[] pages) {

    public int getPageCount() {
        return pages.length;
    }

    public ComicPage getPage(int index) {
        if (index < 0 || index >= pages.length) {
            throw new IndexOutOfBoundsException("Page index out of range.");
        }
        return pages[index];
    }
}
