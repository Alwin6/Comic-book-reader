package com.alba.reader;

public class ComicBook {
    private String title;
    private ComicPage[] pages;

    public ComicBook(String title, ComicPage[] pages) {
        this.title = title;
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public ComicPage[] getPages() {
        return pages;
    }

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
