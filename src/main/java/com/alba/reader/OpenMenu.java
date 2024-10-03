package com.alba.reader;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenMenu {
    private JMenu openMenu;

    public OpenMenu(ComicReader comicReader) {
        openMenu = new JMenu("Open");

        JMenuItem openComicItem = new JMenuItem("Open Comic");
        openComicItem.addActionListener(e -> comicReader.openComic());
        openMenu.add(openComicItem);
    }

    public JMenu getMenu() {
        return openMenu;
    }
}
