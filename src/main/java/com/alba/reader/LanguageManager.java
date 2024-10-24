package com.alba.reader;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

import static com.alba.reader.LocalAppDataUtil.getFile;

public class LanguageManager {
    public static JSONObject LoadLanguage() throws IOException {
        File file;
        try {
            File settingsFile = getFile("Settings.json", "/Alba/ComicReader");
            FileReader reader = new FileReader(settingsFile);
            JSONTokener jsonTokener = new JSONTokener(reader);
            JSONObject settings = new JSONObject(jsonTokener);

            file = getFile(settings.getString("language") + ".json", "/Alba/ComicReader/Lang");
        } catch (IOException e) {
            file = new File("src/main/resources/reader/Lang/English.json");
        }

        // Wait until the file is no longer locked
        while (!FileTools.isFileAvailable(file)) {
            try {
                Thread.sleep(100);  // Small delay before retrying
            } catch (InterruptedException e) {
                throw new IOException("Thread interrupted while waiting for file availability: " + file.getAbsolutePath());
            }
        }

        try (FileReader reader = new FileReader(file)) {
            JSONTokener jsonTokener = new JSONTokener(reader);
            return new JSONObject(jsonTokener);
        }
    }
}
