package com.alba.reader;

import javax.swing.*;

public class OpenMenu {
    private JMenu openMenu;

    public OpenMenu(ComicReader comicReader) {
        openMenu = new JMenu("Open");

        JMenuItem openComicItem = new JMenuItem("Open Comic");
        openComicItem.addActionListener(e -> comicReader.openComic());
        openMenu.add(openComicItem);

        JMenuItem openMetadataItem = new JMenuItem("Open Metadata");
        openMetadataItem.addActionListener(e -> MetadataDialog.MetadataDialog(comicReader.getCurrentComic()));
        openMenu.add(openMetadataItem);
    }

    public JMenu getMenu() {
        return openMenu;
    }
}
