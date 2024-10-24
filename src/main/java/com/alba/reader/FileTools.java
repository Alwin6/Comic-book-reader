package com.alba.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileTools {

    public static List<String> listDirectoryContents(String dirName, String targetPath) throws IOException {
        File dir = new File(targetPath, dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        List<String> contents = new ArrayList<>();
        Files.list(dir.toPath()).forEach(path -> contents.add(path.getFileName().toString()));
        return contents;
    }

    public static boolean isFileAvailable(File file) {
        try (FileInputStream ignored = new FileInputStream(file)) {
            return true;  // File can be opened and is not locked
        } catch (IOException e) {
            return false; // File is locked
        }
    }
}
