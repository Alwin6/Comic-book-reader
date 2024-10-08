package com.alba.reader;

import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
                    String value;
                    try {
                        value = metadata.getString(field);
                    } catch (JSONException e) {
                        try {
                            value = String.valueOf(metadata.getInt(field));
                        } catch (JSONException e1) {
                            try {
                                value = String.valueOf(metadata.getFloat(field));
                            } catch (JSONException e2) {
                                try {
                                    value = String.valueOf(metadata.getDouble(field));
                                } catch (JSONException e3) {
                                    try {
                                        value = String.valueOf(metadata.getBoolean(field));
                                    } catch (JSONException e4) {
                                        value = "";
                                    }
                                }
                            }
                        }
                    }

                    md = md + field + ": " + value + "\n";
                }
            }
        } else {
            md = "This file has no metadata.";
        }

        String controls = md;

        JOptionPane.showMessageDialog(null, controls, "Open - Metadata", JOptionPane.INFORMATION_MESSAGE);
    }
}
