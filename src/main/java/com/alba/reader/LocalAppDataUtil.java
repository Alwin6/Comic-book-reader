package com.alba.reader;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class LocalAppDataUtil {

    private static final String LOCAL_APP_DATA = System.getenv("LOCALAPPDATA");

    public static void copyToLocalAppData(String sourcePath, String targetPath) throws IOException {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            throw new IOException("Source file or directory does not exist: " + sourcePath);
        }

        File destinationFile = new File(LOCAL_APP_DATA + targetPath, sourceFile.getName());

        // Wait if file is locked
        while (!FileTools.isFileAvailable(sourceFile)) {
            try {

                Thread.sleep(100);  // Small delay before retrying
            } catch (InterruptedException e) {
                throw new IOException("Thread interrupted while waiting for file availability: " + sourcePath);
            }
        }

        if (sourceFile.isDirectory()) {
            copyDirectory(sourceFile.toPath(), destinationFile.toPath());
        } else {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void copyDirectory(Path source, Path destination) throws IOException {
        if (!Files.exists(destination)) {
            Files.createDirectories(destination);
        }
        Files.list(source).forEach(path -> {
            try {
                Path targetPath = destination.resolve(path.getFileName());
                if (Files.isDirectory(path)) {
                    copyDirectory(path, targetPath);
                } else {
                    Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void createFileInLocalAppData(String fileName, String targetPath) throws IOException {
        File targetDir = new File(LOCAL_APP_DATA, targetPath);
        if (!targetDir.exists()) {
            Files.createDirectories(targetDir.toPath());
        }
        File file = new File(targetDir, fileName);
        if (!file.exists()){
            Files.createFile(file.toPath());
        } else{
            System.out.println("File already exists: " + file.getAbsolutePath());
        }
    }

    public static void createDirectoryInLocalAppData(String dirName, String targetPath) throws IOException {
        File newDir = new File(LOCAL_APP_DATA + targetPath, dirName);
        if (!newDir.exists()) {
            Files.createDirectories(newDir.toPath());
        } else {
            System.out.println("Directory already exists: " + newDir.getAbsolutePath());
        }
    }

    public static File getFile(String fileName, String targetPath) throws IOException {
        File file = new File(LOCAL_APP_DATA + targetPath, fileName);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        return file;
    }

    public static void writeFile(String targetFilePath, File sourceFile) throws IOException {
        File targetFile = new File(targetFilePath);
        if (!sourceFile.exists()) {
            throw new IOException("Source file does not exist: " + sourceFile.getAbsolutePath());
        }
        // Replace the content of the target file with the content of the source file
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void writeStringToFile(String targetFilePath, String Content) throws IOException {

        try (FileWriter file = new FileWriter(LOCAL_APP_DATA + targetFilePath)) {
            file.write(Content);
            file.flush();
        }
    }

    public static List<String> listDirectoryContents(String dirName, String targetPath) throws IOException {
        return FileTools.listDirectoryContents(dirName, LOCAL_APP_DATA + targetPath);
    }

    public static JSONObject getSettingsObject(){
        File settingsFile;
        FileReader reader;
        try {
            settingsFile = getFile("Settings.json", "/Alba/ComicReader");
            reader = new FileReader(settingsFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        JSONTokener jsonTokener = new JSONTokener(reader);
        return new JSONObject(jsonTokener);
    }

    public static void init() throws IOException {
        // Initialize directories and files here that always have to be there
        createDirectoryInLocalAppData("Alba", "");
        createDirectoryInLocalAppData("ComicReader", "/Alba");

        createFileInLocalAppData("Settings.json", "/Alba/ComicReader");
        File settings = getFile("Settings.json", "/Alba/ComicReader");
        if (settings.length() == 0) {
            writeStringToFile("/Alba/ComicReader/Settings.json", """
                    {
                        "darkMode": true,
                        "language": "English"
                    }
                    """);
        }

        createDirectoryInLocalAppData("Lang", "/Alba/ComicReader");
        List<String> languages = FileTools.listDirectoryContents("Lang", "src/main/resources/reader");
        for (String language : languages) {
            copyToLocalAppData("src/main/resources/reader/Lang/" + language, "/Alba/ComicReader/Lang");
        }
    }
}

