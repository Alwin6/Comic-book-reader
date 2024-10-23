package com.alba.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileTools {

    public static int getFileSizeMB(File file) {
        //checking if the file exists or the file specified is of the type file
        if ((file.exists()) && (file.isFile())) {
            return (int) (file.length() / (1024 * 1024));
        }
        return 0;
    }

    public static List<String> listDirectoryContents(String dirName, String targetPath) throws IOException {
        File dir = new File(targetPath, dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        List<String> contents = new ArrayList<>();
        Files.list(dir.toPath()).forEach(path -> contents.add(path.getFileName().toString()));
        return contents;
    }
}
