package com.alba.reader;

import javax.swing.*;

class Comic {
    String title;
    ImageIcon thumbnail;
    String lastOpened;
    int currentPage;
    int totalPages;
    String filePath;

    public Comic(String title, ImageIcon thumbnail, String lastOpened, int currentPage, int totalPages, String filePath) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.lastOpened = lastOpened;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.filePath = filePath;
    }
}
