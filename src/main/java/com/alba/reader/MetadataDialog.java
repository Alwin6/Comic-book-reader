package com.alba.reader;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetadataDialog {

    public static void MetadataDialog(File file) {

        JSONObject metadataObject;

        JSONObject lang;
        try {
            lang = LanguageManager.LoadLanguage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject metadata;


        // Attempt to load the metadata from the current comic
        ComicListManager comicListManager = new ComicListManager();
        try {
            metadataObject = comicListManager.getMetadata(file.getName());
        } catch (IOException e) {
            metadataObject = new JSONObject();
        }
        String metadataString = "";
        List<JTextField> textFields = new ArrayList<>();

        JPanel metadataPanel = new JPanel();
        metadataPanel.setLayout(new GridLayout(0, 2));
        if (JSONObject.getNames(metadataObject) != null) {
            // Certain files have an object and not metadata we can use
            if (metadataObject.has("ComicInfo")) {
                metadataObject = metadataObject.getJSONObject("ComicInfo");
            }

            // Go through every field
            String[] fields = JSONObject.getNames(metadataObject);
            for (String field : fields) {
                // Exclude xml specific fields that remain from conversion to JSON and get the value of each field as a string
                if (!Objects.equals(field, "xmlns:xsd") & !Objects.equals(field, "xmlns:xsi")) {
                    Object obj = metadataObject.opt(field);
                    String value = (obj != null && !JSONObject.NULL.equals(obj)) ? obj.toString() : "";

                    metadataString = metadataString + field + ": " + value + "\n";
                    textFields.add(new JTextField(field, 15));
                    textFields.add(new JTextField(value, 15));
                }
            }

            for (JTextField field : textFields) {
                metadataPanel.add(field);
            }

        } else {
            JLabel l = new JLabel(lang.getString("noMetadata"));
            metadataPanel.add(l);
            JLabel h = new JLabel(" ");
            metadataPanel.add(h);
        }



        JButton addField = new JButton(lang.getString("addField"));
        metadataPanel.add(addField);

        JButton Save = new JButton(lang.getString("save"));
        Save.addActionListener(e -> {
            List<String> entries = new ArrayList<>();
            for (JTextField field : textFields) {
                entries.add(field.getText());
            }
            try {
                comicListManager.updateMetadata(entries, file.getName());

            } catch (IOException ignored) {}

        });
        addField.addActionListener(e -> {

            metadataPanel.remove(addField);
            metadataPanel.remove(Save);
            textFields.add(new JTextField("", 15));
            metadataPanel.add(textFields.getLast());
            textFields.add(new JTextField("", 15));
            metadataPanel.add(textFields.getLast());


            if (textFields.size() > 160) {
                metadataPanel.setLayout(new GridLayout(0, 6));
            } else if (textFields.size() > 80) {
                metadataPanel.setLayout(new GridLayout(0, 4));

            }
            metadataPanel.add(addField);
            metadataPanel.add(Save);


            JOptionPane.getRootFrame().dispose();
            JOptionPane.showMessageDialog(null, metadataPanel, lang.getString("openMetadataTitle"), JOptionPane.PLAIN_MESSAGE);

        });
        metadataPanel.add(Save);

        String controls = metadataString;

        JOptionPane.showMessageDialog(null, metadataPanel, lang.getString("openMetadataTitle"), JOptionPane.PLAIN_MESSAGE);
    }
}