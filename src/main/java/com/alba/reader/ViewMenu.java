package com.alba.reader;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static com.alba.reader.LocalAppDataUtil.*;

public class ViewMenu {
    private JMenu viewMenu;
    private JSONObject lang;

    public ViewMenu(ComicReader comicReader) {

        try {
            lang = LanguageManager.LoadLanguage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        viewMenu = new JMenu(lang.getString("view"));

        JMenuItem fillWidthItem = new JMenuItem(lang.getString("fillWidth"));
        fillWidthItem.addActionListener(e -> comicReader.fillWidth());
        viewMenu.add(fillWidthItem);

        JMenuItem fillHeightItem = new JMenuItem(lang.getString("fillHeight"));
        fillHeightItem.addActionListener(e -> comicReader.fillHeight());
        viewMenu.add(fillHeightItem);

        JMenuItem zoomInButton = new JMenuItem(lang.getString("zoomIn"));
        zoomInButton.addActionListener(e -> comicReader.zoom(1.2f));
        viewMenu.add(zoomInButton);

        JMenuItem zoomOutButton = new JMenuItem(lang.getString("zoomOut"));
        zoomOutButton.addActionListener(e -> comicReader.zoom(0.8f));
        viewMenu.add(zoomOutButton);

        JMenuItem toggleDarkModeItem = new JMenuItem(lang.getString("toggleDarkMode"));
        toggleDarkModeItem.addActionListener(e -> comicReader.toggleDarkMode());
        viewMenu.add(toggleDarkModeItem);

        JMenuItem language = new JMenuItem(lang.getString("language"));
        language.addActionListener(this::showLanguageDialog);
        viewMenu.add(language);
    }

    public JMenu getMenu() {
        return viewMenu;
    }


    private void showLanguageDialog(ActionEvent e) {
        JPanel panel = new JPanel();
        String controls = lang.getString("selectLanguage");
        JLabel label = new JLabel(controls);
        panel.add(label);
        JComboBox<String> picker = new JComboBox<>();
        JButton saveButton = new JButton(lang.getString("save"));

        List<String> languages;
        try {
            languages = listDirectoryContents("Lang", "/Alba/ComicReader");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        for (String language : languages) {
            picker.addItem(language.substring(0, language.length() - 5));
        }

        File settingsFile = null;
        FileReader reader;
        try {
            settingsFile = readFromFile("Settings.json", "/Alba/ComicReader");
            reader = new FileReader(settingsFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject settings = new JSONObject(tokener);
        picker.setSelectedItem(settings.getString("language"));


        saveButton.addActionListener(g -> {
            try {
                settings.remove("langauge");
                settings.put("language", picker.getSelectedItem());
                writeStringToFile("/Alba/ComicReader/Settings.json", settings.toString(4));
                Window[] windows = ComicReader.getWindows();
                for (Window window : windows) {
                    window.dispose();
                }
                ComicReader.main(null);
                SwingUtilities.invokeLater(() -> {
                    showLanguageDialog(null);
                });
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        panel.add(picker);
        panel.add(saveButton);

        JOptionPane.showMessageDialog(null, panel, lang.getString("viewLanguage"), JOptionPane.PLAIN_MESSAGE);

    }
}
