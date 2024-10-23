package com.alba.reader;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class HelpMenu {

    private JMenu helpMenu;

    public HelpMenu() {
        JSONObject lang;
        try {
            lang = LanguageManager.LoadLanguage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        helpMenu = new JMenu(lang.getString("help"));
        JMenuItem controlsItem = new JMenuItem(lang.getString("controls"));

        controlsItem.addActionListener(this::showControlsDialog);
        helpMenu.add(controlsItem);
    }

    public JMenu getMenu() {
        return helpMenu;
    }

    private void showControlsDialog(ActionEvent e) {
        JSONObject lang;
        try {
            lang = LanguageManager.LoadLanguage();
        } catch (IOException f) {
            throw new RuntimeException(f);
        }
        String controls = lang.getString("controlsBody");

        JOptionPane.showMessageDialog(null, controls, lang.getString("helpControls"), JOptionPane.INFORMATION_MESSAGE);
    }
}
