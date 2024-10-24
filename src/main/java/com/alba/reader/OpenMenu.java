package com.alba.reader;

import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;

public class OpenMenu {
    private final JMenu openMenu;

    public OpenMenu(ComicReader comicReader) {
        JSONObject lang;
        try {
            lang = LanguageManager.LoadLanguage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        openMenu = new JMenu(lang.getString("open"));

        JMenuItem openComicItem = new JMenuItem(lang.getString("openComic"));
        openComicItem.addActionListener(e -> comicReader.openComic());
        openMenu.add(openComicItem);

        JMenuItem openMetadataItem = new JMenuItem(lang.getString("openMetadata"));
        openMetadataItem.addActionListener(e -> MetadataDialog.MetadataDialog(comicReader.getCurrentComic()));
        openMenu.add(openMetadataItem);
    }

    public JMenu getMenu() {
        return openMenu;
    }
}
