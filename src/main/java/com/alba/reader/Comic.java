package com.alba.reader;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comic {
    String title;
    ImageIcon thumbnail;
    boolean read;
    String lastOpened;
    int currentPage;
    int totalPages;
    String filePath;

    public Comic(String title, ImageIcon thumbnail, boolean read, long lastOpened, int currentPage, int totalPages, String filePath) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.read = read;
        this.lastOpened = convertTimestampToDate(lastOpened);
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.filePath = filePath;
    }

    // Convert timestamp to date string
    private String convertTimestampToDate(long timestamp) {
        Date date = new Date(timestamp * 1000); // Convert seconds to milliseconds
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
