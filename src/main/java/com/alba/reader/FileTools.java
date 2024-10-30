package com.alba.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileTools {

    /**
     * @param dirName
     * @param targetPath the path to the directory which contains the directory dirName
     * @return a list of strings corresponding to the contents of a directory
     * @throws IOException
     */
    public static List<String> listDirectoryContents(String dirName, String targetPath) throws IOException {
        File dir = new File(targetPath, dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        List<String> contents = new ArrayList<>();
        Files.list(dir.toPath()).forEach(path -> contents.add(path.getFileName().toString()));
        return contents;
    }

    /**
     * @param file
     * @return a boolean value corresponding to if the file would result in an IOException
     */
    public static boolean isFileAvailable(File file) {
        try (FileInputStream ignored = new FileInputStream(file)) {
            return true;  // File can be opened and is not locked
        } catch (IOException e) {
            return false; // File is locked
        }
    }
}
