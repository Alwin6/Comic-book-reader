package com.alba.reader;

import javax.swing.*;

class Comic {
    String title;
    ImageIcon thumbnail;
    String lastOpened;
    int currentPage;
    int totalPages;

    public Comic(String title, ImageIcon thumbnail, String lastOpened, int currentPage, int totalPages) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.lastOpened = lastOpened;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }
}
