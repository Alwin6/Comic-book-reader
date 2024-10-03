package com.alba.reader;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ViewMenu {
    private JMenu viewMenu;

    public ViewMenu(ComicReader comicReader) {
        viewMenu = new JMenu("View");

        JMenuItem fillWidthItem = new JMenuItem("Fill Width");
        fillWidthItem.addActionListener(e -> comicReader.fillWidth());
        viewMenu.add(fillWidthItem);

        JMenuItem fillHeightItem = new JMenuItem("Fill Height");
        fillHeightItem.addActionListener(e -> comicReader.fillHeight());
        viewMenu.add(fillHeightItem);

        JMenuItem zoomInButton = new JMenuItem("Zoom In");
        zoomInButton.addActionListener(e -> comicReader.zoom(1.2f));
        viewMenu.add(zoomInButton);

        JMenuItem zoomOutButton = new JMenuItem("Zoom Out");
        zoomOutButton.addActionListener(e -> comicReader.zoom(0.8f));
        viewMenu.add(zoomOutButton);

        JMenuItem toggleDarkModeItem = new JMenuItem("Toggle Dark Mode");
        toggleDarkModeItem.addActionListener(e -> comicReader.toggleDarkMode());
        viewMenu.add(toggleDarkModeItem);
    }

    public JMenu getMenu() {
        return viewMenu;
    }
}
