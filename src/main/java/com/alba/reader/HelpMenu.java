package com.alba.reader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HelpMenu {

    private JMenu helpMenu;

    public HelpMenu() {
        helpMenu = new JMenu("Help");
        JMenuItem controlsItem = new JMenuItem("Controls");

        controlsItem.addActionListener(this::showControlsDialog);
        helpMenu.add(controlsItem);
    }

    public JMenu getMenu() {
        return helpMenu;
    }

    private void showControlsDialog(ActionEvent e) {
        String controls = """
                Left Arrow: Previous Page
                Right Arrow: Next Page
                Up Arrow: Scroll Up
                Down Arrow: Scroll Down
                Mouse Wheel: Scroll Up/Down
                Control + Plus: Zoom In
                Control + Minus: Zoom Out
                Control + Mouse Wheel: Zoom In/Out
                """;

        JOptionPane.showMessageDialog(null, controls, "Help - Controls", JOptionPane.INFORMATION_MESSAGE);
    }
}
