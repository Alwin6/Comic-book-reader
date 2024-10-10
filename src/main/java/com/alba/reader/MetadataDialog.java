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
        JSONObject metadataObject;

        // Attempt to load the metadata from the current comic
        ComicListManager cm = new ComicListManager();
        try {
            metadataObject = cm.getMetadata(file.getName());
        } catch (IOException e) {
            metadataObject = new JSONObject();
        }
        String metadata = "";

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
                    Object obj = metadata.opt(field);
                    String value = (obj != null && !JSONObject.NULL.equals(obj)) ? obj.toString() : "";

                    metadata += field + ": " + value + "\n";
                }
            }
        } else {
            metadata = "This file has no metadata.";
        }

        String controls = metadata;

        JOptionPane.showMessageDialog(null, controls, "Open - Metadata", JOptionPane.INFORMATION_MESSAGE);
    }
}
