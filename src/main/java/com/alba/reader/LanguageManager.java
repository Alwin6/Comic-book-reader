package com.alba.reader;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

import static com.alba.reader.LocalAppDataUtil.readFromFile;

public class LanguageManager {
    public static JSONObject LoadLanguage() throws IOException {
        File file;
        try {
            File settingsFile = readFromFile("Settings.json", "/Alba/ComicReader");
            FileReader reader = new FileReader(settingsFile);
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject settings = new JSONObject(tokener);

            file = readFromFile(settings.getString("language") + ".json", "/Alba/ComicReader/Lang");
        } catch (IOException e) {
            file = new File("src/main/resources/reader/Lang/English.json");
        }

        // Wait until the file is no longer locked
        while (!isFileAvailable(file)) {
            try {
                Thread.sleep(100);  // Small delay before retrying
            } catch (InterruptedException e) {
                throw new IOException("Thread interrupted while waiting for file availability: " + file.getAbsolutePath());
            }
        }

        try (FileReader reader = new FileReader(file)) {
            JSONTokener tokener = new JSONTokener(reader);
            return new JSONObject(tokener);
        }
    }

    private static boolean isFileAvailable(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return true;  // File can be opened and is not locked
        } catch (IOException e) {
            return false; // File is locked
        }
    }

}
