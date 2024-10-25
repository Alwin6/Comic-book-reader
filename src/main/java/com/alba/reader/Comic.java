package com.alba.reader;

import org.json.JSONObject;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comic {
    String title;
    ImageIcon thumbnail;
    boolean read;
    boolean favorite;
    String lastOpened;
    int currentPage;
    int totalPages;
    String filePath;
    JSONObject metadata;

    public Comic(String title, ImageIcon thumbnail, boolean read, boolean favorite, long lastOpened, int currentPage, int totalPages, String filePath, JSONObject metadata) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.read = read;
        this.favorite = favorite;
        this.lastOpened = convertTimestampToDate(lastOpened);
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.filePath = filePath;
        this.metadata = metadata;
    }

    // Convert timestamp to date string
    private String convertTimestampToDate(long timestamp) {
        Date date = new Date(timestamp * 1000); // Convert seconds to milliseconds
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
