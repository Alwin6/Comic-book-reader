package com.alba.reader;

import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetadataDialog {



    public static void MetadataDialog(File file) {
        JSONObject metadata;

        // Attempt to load the metadata from the current comic
        ComicListManager cm = new ComicListManager();
        try {
            metadata = cm.getMetadata(file.getName());
        } catch (IOException e) {
            metadata = new JSONObject();
        }
        String md = "";
        List<JTextField> f = new ArrayList<JTextField>();

        JPanel metadataPanel = new JPanel();
        metadataPanel.setLayout(new GridLayout(0, 2));
        if (JSONObject.getNames(metadata) != null) {
            // Certain files have an object and not metadata we can use
            if (metadata.has("ComicInfo")) {
                metadata = metadata.getJSONObject("ComicInfo");
            }

            // Go through every field
            String[] fields = JSONObject.getNames(metadata);
            for (String field : fields) {
                // Exclude xml specific fields that remain from conversion to JSON and get the value of each field as a string
                if (!Objects.equals(field, "xmlns:xsd") & !Objects.equals(field, "xmlns:xsi")) {
                    Object obj = metadata.opt(field);
                    String value = (obj != null && !JSONObject.NULL.equals(obj)) ? obj.toString() : "";

                    md = md + field + ": " + value + "\n";
                    f.add(new JTextField(field, 15));
                    f.add(new JTextField(value, 15));


                }
            }

            for (JTextField field : f) {
                metadataPanel.add(field);
            }




        } else {
            JLabel l = new JLabel("This file has no metadata.");
            metadataPanel.add(l);
            JLabel h = new JLabel(" ");
            metadataPanel.add(h);
        }

        JButton Field = new JButton("Add Field");
        metadataPanel.add(Field);

        JButton Save = new JButton("Save");
        Save.addActionListener(e -> {
            List<String> entries = new ArrayList<String>();
            for (JTextField field : f) {
                entries.add(field.getText());
            }
            try {
                cm.updateMetadata(entries, file.getName());

            } catch (IOException ignored) {}

        });
        Field.addActionListener(e -> {

            metadataPanel.remove(Field);
            metadataPanel.remove(Save);
            f.add(new JTextField("", 15));
            metadataPanel.add(f.getLast());
            f.add(new JTextField("", 15));
            metadataPanel.add(f.getLast());


            if (f.size() > 160) {
                metadataPanel.setLayout(new GridLayout(0, 6));
            } else if (f.size() > 80) {
                metadataPanel.setLayout(new GridLayout(0, 4));

            }
            metadataPanel.add(Field);
            metadataPanel.add(Save);


            JOptionPane.getRootFrame().dispose();
            JOptionPane.showMessageDialog(null, metadataPanel, "Open - Metadata", JOptionPane.PLAIN_MESSAGE);

        });
        metadataPanel.add(Save);

        String controls = md;

        JOptionPane.showMessageDialog(null, metadataPanel, "Open - Metadata", JOptionPane.PLAIN_MESSAGE);
    }
}
