package com.alba.reader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ComicListManager {

    public ComicListManager() {

    }

    public JSONObject getMetadata(String filename) throws IOException {
        try (FileReader reader = LoadComicList()) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject comicList = new JSONObject(tokener);

            return comicList.getJSONObject(filename).getJSONObject("metadata");
        }
    }

    public void updateMetadata(List<String> entries, String filename) throws IOException {
        try (FileReader reader = LoadComicList()) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject comicList = new JSONObject(tokener);

            JSONObject thisComic = comicList.getJSONObject(filename);
            JSONObject metadata = new JSONObject();
            for (int i = 0; i < entries.size(); i+=2) {
                if (!Objects.equals(entries.get(i), "")) {
                    metadata.put(entries.get(i), entries.get(i + 1));
                }
            }
            thisComic.remove("metadata");
            thisComic.put("metadata", metadata);
            comicList.remove(filename);
            comicList.put(filename, thisComic);

            try (FileWriter file = new FileWriter("ComicList.json")) {
                file.write(comicList.toString(4));
                file.flush();
            }
        }
    }

    public void updateJSON(String filename, JSONObject metadata, String path) throws IOException {
        try (FileReader reader = LoadComicList()) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject comicList = new JSONObject(tokener);

            boolean metadataEdited = false;
            boolean read = false;
            int currentPage = 0;

            // Remove the entry of the current comic if it exists, this will add the current comic to the end
            // Please make sure to load comics from the comic list in reverse order
            if (comicList.has(filename)) {
                // Do not change the information stored in an entry when a comic is loaded
                metadataEdited = comicList.getJSONObject(filename).getBoolean("metadataEdited");
                read = comicList.getJSONObject(filename).getBoolean("read");
                currentPage = comicList.getJSONObject(filename).getInt("currentPage");
                metadata = comicList.getJSONObject(filename).getJSONObject("metadata");

                comicList.remove(filename);
            }

            Date d = new Date();
            long time = d.getTime() / 1000;

            // Create/Re-create the entry
            JSONObject comicData = new JSONObject();
            comicData.put("metadata", metadata);
            comicData.put("metadataEdited", metadataEdited);
            comicData.put("read", read);
            comicData.put("currentPage", currentPage);
            comicData.put("lastOpened", time);
            comicData.put("path", path);

            comicList.put(filename, comicData);

            try (FileWriter file = new FileWriter("ComicList.json")) {
                file.write(comicList.toString(4));
                file.flush();
            }
        }
    }

    public FileReader LoadComicList() throws IOException {
        File file = new File("ComicList.json");
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("{}");
                writer.flush();
            }
        }
        return new FileReader(file);
    }
}
