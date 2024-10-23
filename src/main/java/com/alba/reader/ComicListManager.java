package com.alba.reader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.alba.reader.LocalAppDataUtil.*;

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

            writeStringToFile("/Alba/ComicReader/ComicList.json", comicList.toString(4));
        }
    }

    public void updateJSON(String filename, JSONObject metadata, String path) throws IOException {
        try (FileReader reader = LoadComicList()) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject comicList = new JSONObject(tokener);

            boolean metadataEdited = false;
            boolean read = false;
            int currentPage = 0;
            boolean favorite = false;
            JSONObject annotations = new JSONObject();

            // Remove the entry of the current comic if it exists, this will add the current comic to the end
            // Please make sure to load comics from the comic list in reverse order
            if (comicList.has(filename)) {
                // Do not change the information stored in an entry when a comic is loaded
                metadataEdited = comicList.getJSONObject(filename).getBoolean("metadataEdited");
                read = comicList.getJSONObject(filename).getBoolean("read");
                currentPage = comicList.getJSONObject(filename).getInt("currentPage");
                metadata = comicList.getJSONObject(filename).getJSONObject("metadata");
                favorite = comicList.getJSONObject(filename).getBoolean("favorite");
                annotations = comicList.getJSONObject(filename).getJSONObject("annotations");
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
            comicData.put("favorite", favorite);
            comicData.put("annotations", annotations);

            comicList.put(filename, comicData);

            writeStringToFile("/Alba/ComicReader/ComicList.json", comicList.toString(4));
        }
    }

    public FileReader LoadComicList() throws IOException {
        createFileInLocalAppData("ComicList.json", "/Alba/ComicReader");
        File comicL = readFromFile("ComicList.json", "/Alba/ComicReader");
        if (comicL.length() == 0) {
            writeStringToFile("/Alba/ComicReader/ComicList.json", "{}");
        }
        return new FileReader(readFromFile("ComicList.json", "/Alba/ComicReader"));
    }
}
