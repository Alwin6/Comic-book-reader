package com.alba.reader;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class AnnotationsDialog {

    public static void AnnotationsDialog(File file, int page) {
        JSONObject lang;
        try {
            lang = LanguageManager.LoadLanguage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JDialog dialog = new JDialog();
        dialog.setTitle(lang.getString("annotations") + (page + 1));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout()); // Set layout to BorderLayout

        JPanel text = new JPanel();
        text.setLayout(new BorderLayout()); // Allow JTextArea to expand fully within this panel

        JPanel saveButton = new JPanel();
        saveButton.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Align button to the right

        JSONObject annotations = (JSONObject) ComicListManager.readField(file.getName(), "annotations");
        String annotation = "";
        if (annotations.has(String.valueOf(page))) {
            annotation = annotations.getString(String.valueOf(page));
        }

        JTextArea textArea = new JTextArea();
        textArea.setText(annotation);
        textArea.setColumns(72);
        textArea.setRows(32);
        textArea.setLineWrap(true); // Optional: wrap text in JTextArea
        textArea.setWrapStyleWord(true); // Wrap at word boundaries

        JButton save = new JButton(lang.getString("save"));
        saveButton.add(save);

        save.addActionListener(e -> {
            annotations.put(String.valueOf(page), textArea.getText());
            ComicListManager.updateField(file.getName(), "annotations", annotations);
        });

        text.add(new JScrollPane(textArea), BorderLayout.CENTER); // Add JTextArea in a scrollable view

        panel.add(text, BorderLayout.CENTER);      // Add text panel in the center (takes up most space)
        panel.add(saveButton, BorderLayout.SOUTH); // Add saveButton panel at the bottom

        dialog.setResizable(false);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

    }
}
