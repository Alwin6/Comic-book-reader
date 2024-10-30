package com.alba.reader;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OpenMenu {
    private final JMenu openMenu;

    /**Initializes the OpenMenu
     * @param comicReader
     */
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

        JMenuItem openLibrary = new JMenuItem(lang.getString("openLibrary"));
        openLibrary.addActionListener(e -> {
            // Show the ComicDisplay window with ComicReader
            List<Comic> comics;
            try {
                comics = ComicDisplay.parseComics();
            } catch (IOException f) {
                throw new RuntimeException(f);
            }
            ComicDisplay.showComicDisplay(comics, comicReader);
        });
        openMenu.add(openLibrary);

        JMenuItem openAnnotations = new JMenuItem(lang.getString("openAnnotations"));
        openAnnotations.addActionListener(e -> AnnotationsDialog.AnnotationsDialog(comicReader.getCurrentComic(), comicReader.getCurrentPageIndex()));
        openMenu.add(openAnnotations);

        JMenuItem openMetadataItem = new JMenuItem(lang.getString("openMetadata"));
        openMetadataItem.addActionListener(e -> MetadataDialog.MetadataDialog(comicReader.getCurrentComic()));
        openMenu.add(openMetadataItem);
    }

    /**Gets the OpenMenu
     * @return openMenu
     */
    public JMenu getMenu() {
        return openMenu;
    }
}
