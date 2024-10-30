package com.alba.reader;

public record ComicBook(String title, ComicPage[] pages) {

    /**
     * @return the amount of pages
     */
    public int getPageCount() {
        return pages.length;
    }

    /**
     * @param index
     * @return the specified page
     */
    public ComicPage getPage(int index) {
        if (index < 0 || index >= pages.length) {
            throw new IndexOutOfBoundsException("Page index out of range.");
        }
        return pages[index];
    }
}
